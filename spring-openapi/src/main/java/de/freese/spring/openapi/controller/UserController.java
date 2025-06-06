// Created: 28.10.2018
package de.freese.spring.openapi.controller;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("users")
@Tag(name = "UserController", description = "The User API with documentation annotations")
public class UserController {
    private final Map<String, String> userMap = new HashMap<>();

    public UserController() {
        super();

        userMap.put("UserA", "UserA");
        userMap.put("UserB", "UserB");
    }

    @DeleteMapping("delete/{username}")
    @Operation(summary = "Delete user", description = "Deletes specific user by username.")
    @ApiResponses(value = {@ApiResponse(responseCode = "400", description = "Something went wrong"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "The user doesn't exist", content = @Content)})
    public String delete(@PathVariable final String username) {
        userMap.remove(username);

        return username;
    }

    @GetMapping("list")
    @Operation(summary = "List users", description = "List all Users")
    public String list() {
        return String.join(", ", userMap.values());
    }
}
