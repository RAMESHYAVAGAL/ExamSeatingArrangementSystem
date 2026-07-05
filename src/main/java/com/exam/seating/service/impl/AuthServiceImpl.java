package com.exam.seating.service.impl;

import com.exam.seating.dto.PasswordUpdateDTO;
import com.exam.seating.entity.AdminAccount;
import com.exam.seating.entity.Invigilator;
import com.exam.seating.repository.AdminAccountRepository;
import com.exam.seating.repository.InvigilatorRepository;
import com.exam.seating.repository.StudentRepository;
import com.exam.seating.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Consumer;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminAccountRepository adminRepo;
    private final InvigilatorRepository invigilatorRepo;
    private final StudentRepository studentRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void updatePassword(PasswordUpdateDTO dto) {

        String normalizedEmail = normalizeEmail(dto.getEmail());

        Optional<AdminAccount> adminOpt =
                adminRepo.findByEmailIgnoreCaseAndDeletedFalse(normalizedEmail);

        if (adminOpt.isPresent()) {
            updateUser(
                    adminOpt.get().getPassword(),
                    dto,
                    encoded -> {
                        AdminAccount admin = adminOpt.get();
                        admin.setPassword(encoded);
                        admin.setFirstLogin(false);
                        adminRepo.save(admin);
                    }
            );
            return;
        }

        Optional<Invigilator> invigilatorOpt =
                invigilatorRepo.findByEmailIgnoreCaseAndDeletedFalse(normalizedEmail);

        if (invigilatorOpt.isPresent()) {
            updateUser(
                    invigilatorOpt.get().getPassword(),
                    dto,
                    encoded -> {
                        Invigilator inv = invigilatorOpt.get();
                        inv.setPassword(encoded);
                        inv.setFirstLogin(false);
                        invigilatorRepo.save(inv);
                    }
            );
            return;
        }

        throw new RuntimeException("User not found");
    }

    private void updateUser(
            String dbPassword,
            PasswordUpdateDTO dto,
            Consumer<String> saveAction
    ) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), dbPassword)) {
            throw new RuntimeException("Current password is incorrect");
        }

        saveAction.accept(passwordEncoder.encode(dto.getNewPassword()));
    }

    @Override
    public boolean emailExists(String email) {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail.isBlank()) {
            return false;
        }

        if (adminRepo.existsByEmailIgnoreCaseAndDeletedFalse(normalizedEmail)) {
            return true;
        }

        if (studentRepo.existsByEmailIgnoreCaseAndDeletedFalse(normalizedEmail)) {
            return true;
        }

        return invigilatorRepo.existsByEmailIgnoreCaseAndDeletedFalse(normalizedEmail);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}