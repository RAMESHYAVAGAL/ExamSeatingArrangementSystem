package com.exam.seating.service;

import com.exam.seating.dto.StudentDTO;
import com.exam.seating.entity.Student;
import com.exam.seating.enums.Department;

import java.util.List;

public interface StudentService {

    StudentDTO saveStudent(StudentDTO dto);

    StudentDTO updateStudent(Long id, StudentDTO dto);

    List<StudentDTO> getAllStudents();

    StudentDTO getStudentById(Long id);

    void deleteStudent(Long id);

    StudentDTO getStudentByRollNo(String rollNo);

    List<StudentDTO> getStudentsByDepartment(Department department);

    List<StudentDTO> getStudentsByYear(Integer year);

    List<StudentDTO> getStudentsByDepartmentsAndYear(
            List<Department> departments,
            Integer year
    );

    List<Department> getAllDepartments();

    boolean validateDefaultLogin(String email, String enteredPassword);

    void resetPassword(Long studentId, String newPassword);

    void resetToDefaultPassword(Long studentId);

    String getDefaultPassword(StudentDTO dto);

    Student findStudentByEmail(String email);
}