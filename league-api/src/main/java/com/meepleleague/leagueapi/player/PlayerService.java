package com.meepleleague.leagueapi.player;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meepleleague.leagueapi.shared.error.BusinessException;
import com.meepleleague.leagueapi.shared.error.UserMessages;
import com.meepleleague.leagueapi.shared.error.ErrorCode;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public PlayerResponse create(PlayerCreateRequest playerCreateRequest) {
        Player player = new Player(playerCreateRequest.username(), playerCreateRequest.email());
        Player savedPlayer = playerRepository.save(player);

        return toResponse(savedPlayer);
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> list() {
        return playerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlayerResponse getById(long id) {
        return toResponse(retrievePlayerById(id));
    }

    @Transactional
    public PlayerResponse update(long id, PlayerUpdateRequest playerUpdateRequest) {
        Player player = retrievePlayerById(id);

        player.setUsername(playerUpdateRequest.username());
        player.setEmail(playerUpdateRequest.email());

        return toResponse(player);
    }

    @Transactional
    public void delete(long id) {
        Player player = retrievePlayerById(id);
        playerRepository.delete(player);
    }

    @SuppressWarnings("null") // orElseThrow ne rend jamais null
    @NonNull
    private Player retrievePlayerById(long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PLAYER_NOT_FOUND,
                        UserMessages.PLAYER_NOT_FOUND.formatted(id)));
    }

    private PlayerResponse toResponse(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getUsername(),
                player.getEmail(),
                player.getCreatedAt(),
                player.getUpdatedAt());
    }

}
