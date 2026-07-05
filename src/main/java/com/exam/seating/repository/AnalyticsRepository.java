package com.exam.seating.repository;

import com.exam.seating.dto.DepartmentChartDTO;
import com.exam.seating.dto.ExamReportDTO;
import com.exam.seating.dto.YearSemesterChartDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AnalyticsRepository {

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

    public List<DepartmentChartDTO> studentsByDepartment() {
        return entityManager.createQuery("""
                SELECT new com.exam.seating.dto.DepartmentChartDTO(
                    s.department,
                    COUNT(s)
                )
                FROM Student s
                WHERE s.deleted = false
                GROUP BY s.department
                ORDER BY COUNT(s) DESC
                """, DepartmentChartDTO.class)
                .getResultList();
    }

    public List<YearSemesterChartDTO> studentsByYearSemester() {
        return entityManager.createQuery("""
                SELECT new com.exam.seating.dto.YearSemesterChartDTO(
                    s.year,
                    s.currentSemester,
                    COUNT(s)
                )
                FROM Student s
                WHERE s.deleted = false
                AND s.year IS NOT NULL
                AND s.currentSemester IS NOT NULL
                GROUP BY s.year, s.currentSemester
                ORDER BY s.year, s.currentSemester
                """, YearSemesterChartDTO.class)
                .getResultList();
    }

    public List<ExamReportDTO> examReport() {
        return entityManager.createQuery("""
                SELECT new com.exam.seating.dto.ExamReportDTO(
                    e.id,
                    e.examName,
                    e.examCode,
                    e.examDate,
                    e.year,
                    e.semester,
                    COUNT(s)
                )
                FROM Exam e
                LEFT JOIN e.subjects s
                WHERE e.deleted = false
                GROUP BY e.id
                ORDER BY e.examDate DESC
                """, ExamReportDTO.class)
                .getResultList();
    }
}