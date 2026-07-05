package com.exam.seating.controller;

import com.exam.seating.dto.PasswordUpdateDTO;
import com.exam.seating.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/update-password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @RequestBody PasswordUpdateDTO dto) {

        Map<String, String> response = new HashMap<>();

        try {
            authService.updatePassword(dto);
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("message", "Internal server error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @RequestBody Map<String, String> payload) {

        String email = payload.getOrDefault("email", "").trim();

        if (email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("exists", false));
        }

        boolean exists = authService.emailExists(email);

        return ResponseEntity.ok(Map.of("exists", exists));
    }
}