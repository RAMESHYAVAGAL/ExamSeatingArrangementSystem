package com.exam.seating.repository;

import com.exam.seating.entity.InvigilatorAssignment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvigilatorAssignmentRepository
        extends JpaRepository<InvigilatorAssignment, Long> {

    List<InvigilatorAssignment> findByInvigilator_Id(Long invigilatorId);

    List<InvigilatorAssignment> findByExam_Id(Long examId);

    boolean existsByInvigilator_IdAndExam_IdAndRoom_Id(
            Long invigilatorId,
            Long examId,
            Long roomId
    );

    void deleteByExam_Id(Long examId);

    @Query("""
        SELECT ia
        FROM InvigilatorAssignment ia
        WHERE ia.exam.id = :examId
        AND ia.room.id = :roomId
        AND ia.deleted = false
    """)
    Optional<InvigilatorAssignment> findOptionalByExamIdAndRoomId(
            @Param("examId") Long examId,
            @Param("roomId") Long roomId
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE InvigilatorAssignment ia
        SET ia.deleted = true
        WHERE ia.exam.id = :examId
        AND ia.room.id = :roomId
        AND ia.year = :year
        AND ia.semester = :semester
        AND ia.deleted = false
    """)
    void softDeleteByExamIdAndRoomIdAndYearAndSemester(
            @Param("examId") Long examId,
            @Param("roomId") Long roomId,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE InvigilatorAssignment ia
        SET ia.deleted = true
        WHERE ia.exam.id = :examId
        AND ia.year = :year
        AND ia.semester = :semester
        AND ia.deleted = false
    """)
    void softDeleteByExamIdAndYearAndSemester(
            @Param("examId") Long examId,
            @Param("year") Integer year,
            @Param("semester") Integer semester
    );

    Optional<InvigilatorAssignment>
    findByExam_IdAndRoom_IdAndYearAndSemesterAndDeletedFalse(
            Long examId,
            Long roomId,
            Integer year,
            Integer semester
    );

    List<InvigilatorAssignment> findByInvigilator_IdAndDeletedFalse(
            Long invigilatorId
    );
    
    List<InvigilatorAssignment> findByExam_IdAndDeletedFalse(Long examId);
}