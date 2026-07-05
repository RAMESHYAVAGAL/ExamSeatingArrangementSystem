package com.exam.seating.repository;

import com.exam.seating.entity.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminAccountRepository
        extends JpaRepository<AdminAccount, Long> {

    Optional<AdminAccount> findByIdAndDeletedFalse(Long id);

    Optional<AdminAccount> findByEmailIgnoreCaseAndDeletedFalse(String email);

    boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);
}