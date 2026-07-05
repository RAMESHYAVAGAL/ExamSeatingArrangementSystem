package com.exam.seating.repository;

import com.exam.seating.entity.SeatingGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatingGenerationRepository
        extends JpaRepository<SeatingGeneration, Long> {

    List<SeatingGeneration> findByDeletedFalse();

    Optional<SeatingGeneration> findByIdAndDeletedFalse(Long id);

    Optional<SeatingGeneration> findByExam_IdAndDeletedFalse(Long examId);

    boolean existsByExam_IdAndDeletedFalse(Long examId);
}