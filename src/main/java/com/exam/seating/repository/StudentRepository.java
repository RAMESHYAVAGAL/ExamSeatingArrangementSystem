package com.exam.seating.repository;

import com.exam.seating.entity.Student;
import com.exam.seating.enums.Department;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByDeletedFalse();

    Optional<Student> findByIdAndDeletedFalse(Long id);

    boolean existsByRollNo(String rollNo);

    boolean existsByRollNoAndDeletedFalse(String rollNo);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndDeletedFalse(String phone);

    Optional<Student> findByRollNoAndDeletedFalse(String rollNo);

    Optional<Student> findByEmailIgnoreCaseAndDeletedFalse(String email);

    List<Student> findByDepartmentAndDeletedFalse(Department department);

    List<Student> findByYearAndDeletedFalse(Integer year);

    @Query("SELECT s FROM Student s WHERE s.deleted = false AND (" +
           "LOWER(s.rollNo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Student> searchStudents(@Param("query") String query);

    List<Student> findByDepartmentInAndYearAndDeletedFalse(
            @Param("departments") List<Department> departments,
            @Param("year") Integer year
    );

    @Query("""
        SELECT DISTINCT s.department
        FROM Student s
        WHERE s.deleted = false
        ORDER BY s.department
    """)
    List<Department> findDistinctDepartments();

    @Query("""
        SELECT s
        FROM Student s
        WHERE s.department IN :departments
        AND s.year = :year
        AND s.currentSemester = :currentSemester
        AND s.deleted = false
    """)
    List<Student> findByDepartmentInAndYearAndCurrentSemester(
            @Param("departments") List<Department> departments,
            @Param("year") Integer year,
            @Param("currentSemester") Integer currentSemester
    );

    @Query("SELECT s FROM Student s WHERE s.deleted = false AND s.department = :department AND s.year = :year")
    List<Student> findByDepartmentAndYearAndDeletedFalse(
            @Param("department") Department department,
            @Param("year") Integer year
    );

    @Query("SELECT COUNT(s) FROM Student s WHERE s.deleted = false AND s.department = :department")
    long countByDepartmentAndDeletedFalse(@Param("department") Department department);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.deleted = false AND s.year = :year")
    long countByYearAndDeletedFalse(@Param("year") Integer year);
}