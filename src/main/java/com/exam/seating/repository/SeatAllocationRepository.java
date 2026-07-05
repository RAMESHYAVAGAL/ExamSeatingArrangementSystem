package com.exam.seating.repository;

import com.exam.seating.entity.SeatAllocation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatAllocationRepository extends JpaRepository<SeatAllocation, Long> {

    List<SeatAllocation> findByExam_IdAndRoom_IdAndDeletedFalse(
            Long examId,
            Long roomId
    );

    boolean existsByExam_IdAndRoom_IdAndDeletedFalse(
            Long examId,
            Long roomId
    );

    List<SeatAllocation> findByExam_IdAndDeletedFalse(Long examId);

    Optional<SeatAllocation> findByExam_IdAndStudent_IdAndDeletedFalse(
            Long examId,
            Long studentId
    );

    List<SeatAllocation> findByExam_IdAndRoom_IdAndYearAndSemesterAndDeletedFalse(
            Long examId,
            Long roomId,
            Integer year,
            Integer semester
    );

    List<SeatAllocation> findByExam_IdAndYearAndSemesterAndDeletedFalse(
            Long examId,
            Integer year,
            Integer semester
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE SeatAllocation s
        SET s.deleted = true
        WHERE s.exam.id = :examId
    """)
    void markDeletedByExamId(@Param("examId") Long examId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE SeatAllocation s
        SET s.deleted = true
        WHERE s.exam.id = :examId
        AND s.room.id = :roomId
    """)
    void markDeletedByExamIdAndRoomId(
            @Param("examId") Long examId,
            @Param("roomId") Long roomId
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE SeatAllocation s
        SET s.invigilator = NULL
        WHERE s.exam.id = :examId
        AND s.room.id = :roomId
        AND s.year = :year
        AND s.semester = :semester
        AND s.deleted = false
    """)
    void clearInvigilatorByExamIdAndRoomIdAndYearAndSemester(
            @Param("examId") Long examId,
            @Param("roomId") Long roomId,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE SeatAllocation s
        SET s.deleted = true
        WHERE s.exam.id = :examId
        AND s.room.id = :roomId
        AND s.year = :year
        AND s.semester = :semester
        AND s.deleted = false
    """)
    void softDeleteByExamIdAndRoomIdAndYearAndSemester(
            @Param("examId") Long examId,
            @Param("roomId") Long roomId,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );
}