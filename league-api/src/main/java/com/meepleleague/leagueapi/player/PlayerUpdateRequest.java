package com.meepleleague.leagueapi.player;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayerUpdateRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(max = 255) @Email String email) {

    public PlayerUpdateRequest {
        username = username == null ? null : username.strip();
        email = email == null ? null : email.strip();
    }

}