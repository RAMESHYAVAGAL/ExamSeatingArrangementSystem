package com.exam.seating.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class AdminDashboardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public long countStudents() {
        return entityManager.createQuery("""
                SELECT COUNT(s)
                FROM Student s
                WHERE s.deleted = false
                """, Long.class).getSingleResult();
    }

    public long countExams() {
        return entityManager.createQuery("""
                SELECT COUNT(e)
                FROM Exam e
                WHERE e.deleted = false
                """, Long.class).getSingleResult();
    }

    public long countRooms() {
        return entityManager.createQuery("""
                SELECT COUNT(r)
                FROM Room r
                WHERE r.deleted = false
                """, Long.class).getSingleResult();
    }

    public long countInvigilators() {
        return entityManager.createQuery("""
                SELECT COUNT(i)
                FROM Invigilator i
                WHERE i.deleted = false
                """, Long.class).getSingleResult();
    }

    public long countTodayExams(LocalDate today) {
        return entityManager.createQuery("""
                SELECT COUNT(e)
                FROM Exam e
                WHERE :today BETWEEN e.startDate AND e.endDate
                AND e.deleted = false
                """, Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }

    public long countUpcomingExams(LocalDate today) {
        return entityManager.createQuery("""
                SELECT COUNT(e)
                FROM Exam e
                WHERE e.startDate > :today
                AND e.deleted = false
                """, Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }

    public long countStudentsAssigned(LocalDate today) {
        return entityManager.createQuery("""
                SELECT COUNT(DISTINCT sa.student.id)
                FROM SeatAllocation sa
                WHERE sa.exam.id IN (
                    SELECT e.id
                    FROM Exam e
                    WHERE :today BETWEEN e.startDate AND e.endDate
                    AND e.deleted = false
                )
                AND sa.deleted = false
                """, Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }
    
    public long countFreeInvigilators(LocalDate today) {
        return entityManager.createQuery("""
                SELECT COUNT(i)
                FROM Invigilator i
                WHERE i.deleted = false
                AND i.id NOT IN (
                    SELECT ia.invigilator.id
                    FROM InvigilatorAssignment ia
                    WHERE :today BETWEEN ia.exam.startDate AND ia.exam.endDate
                    AND ia.deleted = false
                )
                """, Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }

    public long countPendingExams(LocalDate today) {
        return entityManager.createQuery("""
                SELECT COUNT(e)
                FROM Exam e
                WHERE :today BETWEEN e.startDate AND e.endDate
                AND e.deleted = false
                AND NOT EXISTS (
                    SELECT sa
                    FROM SeatAllocation sa
                    WHERE sa.exam.id = e.id
                    AND sa.deleted = false
                )
                """, Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }

    public List<String> findPendingExamNames(LocalDate today) {
        return entityManager.createQuery("""
                SELECT e.examName
                FROM Exam e
                WHERE :today BETWEEN e.startDate AND e.endDate
                AND e.deleted = false
                AND NOT EXISTS (
                    SELECT sa
                    FROM SeatAllocation sa
                    WHERE sa.exam.id = e.id
                    AND sa.deleted = false
                )
                ORDER BY e.examName
                """, String.class)
                .setParameter("today", today)
                .getResultList();
    }
    
    public long countGeneratedExams(LocalDate today) {
        return entityManager.createQuery("""
                SELECT COUNT(DISTINCT e.id)
                FROM Exam e
                WHERE :today BETWEEN e.startDate AND e.endDate
                AND e.deleted = false
                AND EXISTS (
                    SELECT sa
                    FROM SeatAllocation sa
                    WHERE sa.exam.id = e.id
                    AND sa.deleted = false
                )
                """, Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }
    
    public long totalRoomCapacity() {
        Long total = entityManager.createQuery("""
                SELECT COALESCE(SUM(r.capacity),0)
                FROM Room r
                WHERE r.deleted = false
                """, Long.class)
                .getSingleResult();

        return total != null ? total : 0L;
    }
    
    public long usedRoomCapacity(LocalDate today) {
        return entityManager.createQuery("""
                SELECT COUNT(sa.id)
                FROM SeatAllocation sa
                WHERE sa.exam.id IN (
                    SELECT e.id
                    FROM Exam e
                    WHERE :today BETWEEN e.startDate AND e.endDate
                    AND e.deleted = false
                )
                AND sa.deleted = false
                """, Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }
    
    public long countUpcomingExams(LocalDate today, LocalDate nextDate) {
        return entityManager.createQuery("""
                SELECT COUNT(e)
                FROM Exam e
                WHERE e.startDate > :today
                AND e.startDate <= :nextDate
                AND e.deleted = false
                """, Long.class)
                .setParameter("today", today)
                .setParameter("nextDate", nextDate)
                .getSingleResult();
    }
}