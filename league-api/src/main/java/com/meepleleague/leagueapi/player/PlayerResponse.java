package com.meepleleague.leagueapi.player;

import java.time.OffsetDateTime;

public record PlayerResponse(
        Long id,
        String username,
        String email,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
