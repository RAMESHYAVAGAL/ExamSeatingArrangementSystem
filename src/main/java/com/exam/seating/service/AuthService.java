package com.exam.seating.service;

import com.exam.seating.dto.PasswordUpdateDTO;

public interface AuthService {

    void updatePassword(PasswordUpdateDTO dto);

    boolean emailExists(String email);
}