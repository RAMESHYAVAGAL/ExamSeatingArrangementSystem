package com.exam.seating.controller;

import com.exam.seating.dto.AdminAccessRequestDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin-access")
@CrossOrigin(origins = "*")
public class AdminAccessController {

    @Value("${admin.secret.key}")
    private String adminKey;

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestBody AdminAccessRequestDTO request,
            HttpSession session
    ) {

        if (request == null || request.getKey() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "allowed", false,
                            "message", "Admin key is required"
                    ));
        }

        if (adminKey.equals(request.getKey().trim())) {
            session.setAttribute("ADMIN_REGISTER_ALLOWED", true);

            return ResponseEntity.ok(
                    Map.of(
                            "allowed", true,
                            "message", "Access granted"
                    )
            );
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "allowed", false,
                        "message", "Invalid admin key"
                ));
    }
}