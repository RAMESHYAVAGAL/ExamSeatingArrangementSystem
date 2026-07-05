package com.exam.seating.repository;

import com.exam.seating.entity.HallTicket;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HallTicketRepository extends JpaRepository<HallTicket, Long> {
    
    boolean existsByStudent_IdAndExam_Id(Long studentId, Long examId);
    
    boolean existsByStudent_IdAndExam_IdAndDeletedFalse(Long studentId, Long examId);
    
    Optional<HallTicket> findByStudent_IdAndExam_Id(Long studentId, Long examId);
    
    Optional<HallTicket> findByStudent_IdAndExam_IdAndDeletedFalse(Long studentId, Long examId);
    
    List<HallTicket> findByExam_Id(Long examId);
    
    List<HallTicket> findByExam_IdAndDeletedFalse(Long examId);
    
    List<HallTicket> findByRoom_IdAndDeletedFalse(Long roomId);
    
    @EntityGraph(attributePaths = {"student", "exam", "invigilator"})
    Optional<HallTicket> findByIdAndDeletedFalse(Long id);
    
    @Query("""
        SELECT ht
        FROM HallTicket ht
        JOIN FETCH ht.exam
        JOIN FETCH ht.student
        LEFT JOIN FETCH ht.invigilator
        WHERE ht.student.id = :studentId
        AND ht.deleted = false
        ORDER BY ht.exam.startDate
    """)
    List<HallTicket> findByStudentUsingId(@Param("studentId") Long studentId);

    @Query("""
        SELECT ht
        FROM HallTicket ht
        WHERE ht.student.id = :studentId
        AND ht.exam.id = :examId
        AND ht.deleted = false
    """)
    Optional<HallTicket> findByStudentAndExam(
            @Param("studentId") Long studentId,
            @Param("examId") Long examId
    );
    
    @Query("""
        SELECT COUNT(ht)
        FROM HallTicket ht
        WHERE ht.student.id = :studentId
        AND ht.deleted = false
        AND ht.exam.endDate < CURRENT_DATE
    """)
    Long countCompletedExams(@Param("studentId") Long studentId);

    @Query("""
        SELECT COUNT(ht)
        FROM HallTicket ht
        WHERE ht.student.id = :studentId
        AND ht.deleted = false
        AND :currentDate BETWEEN ht.exam.startDate AND ht.exam.endDate
    """)
    Long countCurrentExams(@Param("studentId") Long studentId, @Param("currentDate") LocalDate currentDate);

    @Query("""
        SELECT COUNT(ht)
        FROM HallTicket ht
        WHERE ht.student.id = :studentId
        AND ht.deleted = false
        AND ht.exam.startDate > CURRENT_DATE
    """)
    Long countUpcomingExams(@Param("studentId") Long studentId);
    
    @Modifying
    @Transactional
    @Query("""
        UPDATE HallTicket ht
        SET ht.deleted = true
        WHERE ht.exam.id = :examId
        AND ht.year = :year
        AND ht.semester = :semester
        AND ht.deleted = false
    """)
    void softDeleteByExamIdAndYearAndSemester(
            @Param("examId") Long examId,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );
    
    List<HallTicket> findByExam_IdAndYearAndSemesterAndDeletedFalse(
            Long examId,
            Integer year,
            Integer semester
    );

    boolean existsByExam_IdAndYearAndSemesterAndDeletedFalse(
            Long examId,
            Integer year,
            Integer semester
    );
    
    @Query("""
        SELECT ht
        FROM HallTicket ht
        WHERE ht.exam.id = :examId
        AND ht.room.id = :roomId
        AND ht.year = :year
        AND ht.semester = :semester
        AND ht.deleted = false
    """)
    List<HallTicket> findByExamAndRoomAndYearAndSemester(
            @Param("examId") Long examId,
            @Param("roomId") Long roomId,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );
    
    @Query("""
        SELECT ht
        FROM HallTicket ht
        WHERE ht.student.id = :studentId
        AND ht.exam.id = :examId
        AND ht.year = :year
        AND ht.semester = :semester
        AND ht.room.id = :roomId
        AND ht.deleted = false
    """)
    Optional<HallTicket> findByStudentAndExamAndYearAndSemesterAndRoom(
            @Param("studentId") Long studentId,
            @Param("examId") Long examId,
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            @Param("roomId") Long roomId
    );
}