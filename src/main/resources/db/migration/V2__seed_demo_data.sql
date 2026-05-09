INSERT INTO app_users (full_name, phone_number)
VALUES
    ('Ahmed Ali', '+201000000000'),
    ('Mona Hassan', '+201111111111');

INSERT INTO device_tokens (user_id, token, platform)
VALUES
    (1, 'fake-android-token-ahmed', 'ANDROID'),
    (2, 'fake-ios-token-mona', 'IOS');

INSERT INTO payments (user_id, amount, currency, due_date, status)
VALUES
    (1, 450.00, 'EGP', CURRENT_DATE + INTERVAL '2 days', 'PENDING'),
    (1, 300.00, 'EGP', CURRENT_DATE - INTERVAL '1 day', 'PENDING'),
    (2, 700.00, 'EGP', CURRENT_DATE + INTERVAL '3 days', 'PENDING'),
    (2, 250.00, 'EGP', CURRENT_DATE - INTERVAL '2 days', 'PENDING');
