package com.meepleleague.leagueapi.player;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayerCreateRequest(
        @NotBlank @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,
        @NotBlank @Size(max = 255, message = "Email must be less than 255 characters") @Email String email) {

    public PlayerCreateRequest {
        username = username == null ? null : username.strip();
        email = email == null ? null : email.strip();
    }

}