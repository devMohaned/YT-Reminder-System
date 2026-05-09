# Fintech Installment Reminder System

Spring Boot backend for installment reminder workflows in a fintech-style payment domain.

It handles:

- Upcoming payment reminders
- Missed payment alerts
- Outbox-based notification retries
- Scheduled + manual job execution
- Distributed scheduler locking (ShedLock)

## Table of Contents

- Overview
- Architecture
- Tech Stack
- Project Structure
- Configuration
- Running the Project
- API Documentation
- Job Behavior
- Data Model
- Observability
- Troubleshooting

## Overview

The system tracks users, device tokens, and installment payments. It generates notifications for:

- Payments that are due soon
- Payments that are already overdue

Notifications are written to an outbox table and delivered by a retry job via a push provider:

- `mock` provider (default, no external dependency)
- `firebase` provider (optional)

## Architecture

High-level flow:

1. Create user and register device token.
2. Create payment(s) with due dates.
3. Scheduler or admin endpoint triggers payment jobs.
4. Payment jobs create idempotent outbox entries.
5. Retry job sends notifications and updates outbox state.
6. Every run is recorded in `cron_job_executions`.

Core patterns:

- Outbox pattern for reliable delivery tracking
- Idempotency key to avoid duplicate notifications
- Exponential backoff for retry attempts
- Defensive logging around integration failures
- ShedLock for multi-instance scheduler safety

## Tech Stack

- Java 17
- Spring Boot 4.0.5
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring Scheduler
- ShedLock (JDBC provider)
- Lombok
- Docker Compose
- Actuator
- Optional Firebase Admin SDK

## Project Structure

```text
src/main/java/com/example/installmentreminder
|-- admin          # Admin/manual trigger APIs
|-- common         # API wrapper + exception handling + utilities
|-- config         # ShedLock configuration
|-- job            # Cron job execution tracking
|-- notification   # Outbox + push providers + retry logic
|-- payment        # Payment APIs + payment processing logic
|-- scheduler      # Scheduled job orchestration
`-- user           # User and device token APIs
```

## Configuration

Configuration file: `src/main/resources/application.yml`

### Core Runtime

| Key | Default |
|---|---|
| `server.port` | `8080` |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/installment_reminder_db` |
| `spring.datasource.username` | `installment_user` |
| `spring.datasource.password` | `installment_password` |
| `spring.jpa.hibernate.ddl-auto` | `validate` |
| `spring.flyway.enabled` | `true` |

### Scheduler & Jobs

| Key | Default |
|---|---|
| `payment-reminder.jobs.zone` | `Africa/Cairo` |
| `payment-reminder.jobs.upcoming-payment-reminder.cron` | `0 0 9 * * *` |
| `payment-reminder.jobs.upcoming-payment-reminder.days-before-due-date` | `3` |
| `payment-reminder.jobs.upcoming-payment-reminder.batch-size` | `500` |
| `payment-reminder.jobs.missed-payment.cron` | `0 0 * * * *` |
| `payment-reminder.jobs.missed-payment.batch-size` | `500` |
| `payment-reminder.jobs.notification-retry.cron` | `0 */5 * * * *` |
| `payment-reminder.jobs.notification-retry.batch-size` | `500` |
| `payment-reminder.jobs.notification-retry.max-retry-count` | `3` |

### Push Provider

| Key | Default |
|---|---|
| `payment-reminder.push.provider` | `mock` |
| `payment-reminder.firebase.service-account-path` | empty |

### Environment Variables

You can override these via env vars:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `PAYMENT_REMINDER_PUSH_PROVIDER`
- `PAYMENT_REMINDER_FIREBASE_SERVICE_ACCOUNT_PATH`

## Running the Project

### Prerequisites

- JDK 17
- Maven (`mvn`) installed
- Docker + Docker Compose

Note: this repository currently does not include Maven Wrapper (`mvnw`).

### Option A: Run App Locally + DB in Docker

1. Start PostgreSQL and pgAdmin:

```bash
docker compose up -d postgres pgadmin
```

2. Run Spring Boot app:

```bash
mvn spring-boot:run
```

3. Verify:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/scheduledtasks`

Postgres connection:

- Host: `localhost`
- Port: `5432`
- Database: `installment_reminder_db`
- Username: `installment_user`
- Password: `installment_password`

pgAdmin:

- URL: `http://localhost:5050`
- Email: `admin@installmentreminder.com`
- Password: `admin`

### Option B: Run Everything with Docker

1. Build jar:

```bash
mvn clean package
```

2. Run app + db:

```bash
docker compose up
```

App URL: `http://localhost:8080`

## API Documentation

All responses are wrapped as:

```json
{
  "message": "string",
  "data": {}
}
```

### Validation

- Request-body validation uses `@Valid` on controllers.
- DTO constraints are enforced via Jakarta Validation annotations.
- Path-variable validation errors are handled and returned as `400`.

### Endpoints

#### Health & Actuator

```http
GET /actuator/health
GET /actuator/scheduledtasks
```

#### Users

Create user:

```http
POST /api/users
Content-Type: application/json

{
  "fullName": "Ahmed Ali",
  "phoneNumber": "+201000000000"
}
```

Register device token:

```http
POST /api/users/{userId}/device-tokens
Content-Type: application/json

{
  "token": "fake-device-token-123",
  "platform": "ANDROID"
}
```

`platform` enum:

- `ANDROID`
- `IOS`

#### Payments

Create payment:

```http
POST /api/payments
Content-Type: application/json

{
  "userId": 1,
  "amount": 450.00,
  "currency": "EGP",
  "dueDate": "2026-05-10"
}
```

List payments:

```http
GET /api/payments
```

Mark payment as paid:

```http
POST /api/payments/{paymentId}/mark-paid
```

`status` enum:

- `PENDING`
- `PAID`
- `OVERDUE`
- `CANCELLED`

#### Admin Jobs

Run upcoming reminder job:

```http
POST /api/admin/jobs/upcoming-payment-reminders/run
```

Run missed payment job:

```http
POST /api/admin/jobs/missed-payments/run
```

Run notification retry job:

```http
POST /api/admin/jobs/notifications/retry
```

View recent job executions:

```http
GET /api/admin/jobs/executions
```

#### Admin Notifications

View latest outbox notifications (first 50):

```http
GET /api/admin/notifications
```

### Handy Request Collection

Use `requests.http` in the project root for ready-to-run API calls.

## Job Behavior

### Upcoming Payment Reminder Job

- Finds `PENDING` payments with due date in `[today, today + daysBeforeDueDate]`
- Creates notification outbox rows with type `UPCOMING_PAYMENT_REMINDER`

### Missed Payment Job

- Finds `PENDING` payments with due date before today
- Marks each payment as `OVERDUE`
- Creates outbox rows with type `MISSED_PAYMENT_ALERT`

### Notification Retry Job

- Pulls notifications where:
  - `status` in `PENDING`, `FAILED`
  - `retry_count < maxRetryCount`
  - `next_attempt_at` is null or due
- Attempts push send
- On success:
  - `status = SENT`
  - sets `sent_at`
  - clears failure fields
- On failure:
  - `status = FAILED`
  - increments `retry_count`
  - sets `failure_reason` (trimmed to 1000 chars)
  - sets `next_attempt_at` using exponential backoff capped at 60 minutes

### Cron Execution Tracking

Every job run is persisted with:

- `job_name`
- `status` (`RUNNING`, `SUCCESS`, `FAILED`)
- `started_at`, `finished_at`
- `total_processed`
- `error_message` (for failed runs)

## Data Model

Flyway migrations:

- `V1__init_schema.sql`
- `V2__seed_demo_data.sql`

Main tables:

- `app_users`
- `device_tokens`
- `payments`
- `notification_outbox`
- `cron_job_executions`
- `shedlock`

### Idempotency Key Format

Outbox uniqueness is enforced by `uk_notification_idempotency_key`.

Examples:

- `UPCOMING_PAYMENT_REMINDER:{paymentId}:{userId}:{dueDate}`
- `MISSED_PAYMENT_ALERT:{paymentId}:{userId}`

## Push Providers

### Mock (Default)

No external integration required.

Provider config:

```yaml
payment-reminder:
  push:
    provider: mock
```

Expected logs:

```text
Mock push notification sent to userId=1, title=..., body=...
```

### Firebase (Optional)

Provider config:

```yaml
payment-reminder:
  push:
    provider: firebase
  firebase:
    service-account-path: /absolute/or/relative/path/to/service-account.json
```

Or env vars:

```bash
PAYMENT_REMINDER_PUSH_PROVIDER=firebase
PAYMENT_REMINDER_FIREBASE_SERVICE_ACCOUNT_PATH=/path/to/firebase-service-account.json
```

If Firebase is enabled and path is missing/blank, app startup fails fast with a clear error.

## Observability

Actuator exposure:

- `health`
- `info`
- `metrics`
- `scheduledtasks`

Key operational logs include:

- Job success/failure with processed counts
- Notification retry integration failures
- Firebase initialization/send failures
- Idempotency duplicate insert race handling

## Troubleshooting

### `mvn` is not recognized

Install Maven and ensure it is on your `PATH`, then run:

```bash
mvn -v
```

### App cannot connect to DB

- Ensure DB container is running:

```bash
docker compose ps
```

- Validate credentials/URL match `application.yml` or env overrides.

### Port already in use

- `5432`: PostgreSQL conflict
- `5050`: pgAdmin conflict
- `8080`: app conflict

Change host port mappings in `docker-compose.yml` if needed.

### Clean reset

```bash
docker compose down -v
docker compose up -d postgres pgadmin
mvn spring-boot:run
```

## Development Notes

- Uses Lombok (`@RequiredArgsConstructor`, `@Builder`, `@Slf4j`) to reduce boilerplate.
- Controllers handle input validation with `@Valid`.
- Service layer contains business rules and integration logic.
- Outbox + retries are designed for resilience and replay safety.

