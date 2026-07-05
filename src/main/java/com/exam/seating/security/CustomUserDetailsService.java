package com.exam.seating.security;

import com.exam.seating.entity.AdminAccount;
import com.exam.seating.entity.Invigilator;
import com.exam.seating.entity.Student;
import com.exam.seating.repository.AdminAccountRepository;
import com.exam.seating.repository.InvigilatorRepository;
import com.exam.seating.repository.StudentRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminAccountRepository userRepo;
    private final InvigilatorRepository invRepo;
    private final StudentRepository studentRepo;

    public CustomUserDetailsService(
            AdminAccountRepository userRepo,
            InvigilatorRepository invRepo,
            StudentRepository studentRepo) {
        this.userRepo = userRepo;
        this.invRepo = invRepo;
        this.studentRepo = studentRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String normalizedEmail = normalizeEmail(email);
        System.out.println("loadUserByUsername called with: " + normalizedEmail);

        // 1. Check ADMIN
        AdminAccount admin = userRepo.findByEmailIgnoreCaseAndDeletedFalse(normalizedEmail).orElse(null);
        if (admin != null) {
            return new CustomUserDetails(
                    admin.getId(),
                    "ADMIN",
                    admin.getEmail(),
                    admin.getPassword(),
                    true,
                    List.of(new SimpleGrantedAuthority("ADMIN"))
            );
        }

        // 2. Check INVIGILATOR
        Invigilator inv = invRepo.findByEmailIgnoreCaseAndDeletedFalse(normalizedEmail).orElse(null);
        if (inv != null) {
            return new CustomUserDetails(
                    inv.getId(),
                    "INVIGILATOR",
                    inv.getEmail(),
                    inv.getPassword(),
                    true,
                    List.of(new SimpleGrantedAuthority("INVIGILATOR"))
            );
        }

        // 3. Check STUDENT
        Student student = studentRepo.findByEmailIgnoreCaseAndDeletedFalse(normalizedEmail).orElse(null);
        if (student != null) {
            return new CustomUserDetails(
                    student.getId(),
                    "STUDENT",
                    student.getEmail(),
                    student.getPassword(),
                    true,
                    List.of(new SimpleGrantedAuthority("STUDENT"))
            );
        }

        System.out.println("USER NOT FOUND for email: " + normalizedEmail);
        throw new UsernameNotFoundException("User not found");
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim();
    }
}