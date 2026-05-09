package com.example.installmentreminder.user;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank String fullName, String phoneNumber) {
}
