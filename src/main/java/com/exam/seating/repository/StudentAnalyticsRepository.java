package com.exam.seating.repository;

import com.exam.seating.entity.Student;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface StudentAnalyticsRepository extends JpaRepository<Student, Long> {

    @Query("""
        SELECT COUNT(s)
        FROM Student s
        WHERE s.department = :department
        AND s.deleted = false
    """)
    Long countByDepartment(@Param("department") String department);
}