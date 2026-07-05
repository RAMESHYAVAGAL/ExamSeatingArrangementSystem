package com.exam.seating.repository;

import com.exam.seating.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByDeletedFalse();

    Optional<Exam> findByIdAndDeletedFalse(Long id);

    Optional<Exam> findByExamCodeAndDeletedFalse(String examCode);

    boolean existsByExamCodeAndDeletedFalse(String examCode);

    boolean existsByExamCodeAndDeletedFalseAndIdNot(String examCode, Long id);

    List<Exam> findByStartDateAndDeletedFalse(LocalDate startDate);
    
    @Query("SELECT e FROM Exam e WHERE e.deleted = false AND :date BETWEEN e.startDate AND e.endDate")
    List<Exam> findByDateRange(@Param("date") LocalDate date);

    List<Exam> findByYearAndSemesterAndDeletedFalse(Integer year, Integer semester);

    @Query("""
            SELECT e
            FROM Exam e
            WHERE e.deleted = false
            AND (
                LOWER(e.examName) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(e.examCode) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            """)
    List<Exam> searchExams(@Param("query") String query);
    
    Optional<Exam> findByExamCode(String examCode);
    
    boolean existsByExamCode(String examCode);
}