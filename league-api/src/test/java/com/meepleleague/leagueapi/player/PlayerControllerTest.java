package com.meepleleague.leagueapi.player;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.meepleleague.leagueapi.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
public class PlayerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("should create a new player when valid data is provided")
    void createPlayer() throws Exception {
        mockMvc.perform(post("/players")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"username":"testuser","email":"testuser@example.com"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("should return 409 Conflict when trying to create a player with duplicate username")
    void createPlayerWithDuplicateUsername() throws Exception {

        // First creation should succeed
        mockMvc.perform(post("/players")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"username":"testuser","email":"testuser@example.com"}
                        """))
                .andExpect(status().isCreated());

        // Second creation should fail with 409 Conflict due to duplicate username
        mockMvc.perform(post("/players")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"username":"testuser","email":"testuser_unique@example.com"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.code").value("USERNAME_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.detail").value("Username already exists."));
    }

    @Test
    @DisplayName("should return 409 Conflict when trying to create a player with duplicate email")
    void createPlayerWithDuplicateEmail() throws Exception {

        // First creation should succeed
        mockMvc.perform(post("/players")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"username":"testuser","email":"testuser@example.com"}
                        """))
                .andExpect(status().isCreated());

        // Second creation should fail with 409 Conflict due to duplicate email
        mockMvc.perform(post("/players")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"username":"testuser_unique","email":"testuser@example.com"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.detail").value("Email already exists."));
    }

    @Test
    @DisplayName("should return 400 Bad Request when trying to create a player with username that is too short")
    void createPlayerWithInvalidData() throws Exception {
        mockMvc.perform(post("/players").contentType(APPLICATION_JSON)
                .content("""
                        {"username":"no","email":"testuser@example.com"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("username"))
                .andExpect(jsonPath("$.errors[0].message").value("Username must be between 3 and 50 characters"));
    }

    @Test
    @DisplayName("should return 404 Not Found when trying to get a player that does not exist")
    void getPlayerNotFound() throws Exception {
        mockMvc.perform(get("/players/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("PLAYER_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Player of ID 999 not found."))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }
}
