package com.exam.seating.dto;

import com.exam.seating.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private boolean success;
    private Role role;
    private String message;
    private boolean firstLogin;
}