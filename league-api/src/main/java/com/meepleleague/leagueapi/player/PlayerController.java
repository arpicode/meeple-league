package com.meepleleague.leagueapi.player;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> create(
            @Valid @RequestBody PlayerCreateRequest playerCreateRequest) {

        PlayerResponse saved = playerService.create(playerCreateRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .replaceQuery(null)
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(saved);
    }

    @GetMapping
    public List<PlayerResponse> list() {
        return playerService.list();
    }

    @GetMapping("/{id}")
    public PlayerResponse getById(@PathVariable long id) {
        return playerService.getById(id);
    }

    @PutMapping("/{id}")
    public PlayerResponse update(@PathVariable long id, @Valid @RequestBody PlayerUpdateRequest playerUpdateRequest) {
        return playerService.update(id, playerUpdateRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        playerService.delete(id);
    }

}
