package com.exam.seating.service.impl;

import com.exam.seating.dto.StudentDTO;
import com.exam.seating.entity.Student;
import com.exam.seating.enums.Department;
import com.exam.seating.exception.ResourceNotFoundException;
import com.exam.seating.repository.StudentRepository;
import com.exam.seating.service.StudentService;

import jakarta.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository,
                              PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public StudentDTO saveStudent(StudentDTO dto) {
        try {
            if (studentRepository.existsByRollNo(dto.getRollNo())) {
                throw new RuntimeException("Roll number '" + dto.getRollNo() + "' already exists.");
            }
            
            if (studentRepository.existsByEmailIgnoreCase(dto.getEmail())) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' already exists.");
            }
            
            if (studentRepository.existsByPhone(dto.getPhone())) {
                throw new RuntimeException("Phone number '" + dto.getPhone() + "' already exists.");
            }
            
            Student student = mapToEntity(dto);
            if (student.getPassword() == null || student.getPassword().isEmpty()) {
                student.setPassword(passwordEncoder.encode("default123"));
            }
            student.setDeleted(false);
            
            Student saved = studentRepository.save(student);
            return mapToDTO(saved);
            
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message.contains("UKdqf52k0x0xhaoxq9a3vqkd457") || message.contains("roll_no")) {
                throw new RuntimeException("Roll number '" + dto.getRollNo() + "' already exists. Please use a different roll number.");
            } else if (message.contains("UK_e2rndfrsx22acpq2ty1caeuyw") || message.contains("email")) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' already exists. Please use a different email.");
            } else if (message.contains("UK_4j48kma5fa3dcya13gd0l3gi") || message.contains("phone")) {
                throw new RuntimeException("Phone number '" + dto.getPhone() + "' already exists. Please use a different phone number.");
            }
            throw new RuntimeException("Failed to save student: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save student: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        try {
            Student student = studentRepository.findByIdAndDeletedFalse(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", id));

            if (!student.getRollNo().equals(dto.getRollNo())
                    && studentRepository.existsByRollNo(dto.getRollNo())) {
                throw new RuntimeException("Roll number '" + dto.getRollNo() + "' already exists.");
            }

            if (!student.getEmail().equalsIgnoreCase(dto.getEmail())
                    && studentRepository.existsByEmailIgnoreCase(dto.getEmail())) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' already exists.");
            }

            if (!student.getPhone().equals(dto.getPhone())
                    && studentRepository.existsByPhone(dto.getPhone())) {
                throw new RuntimeException("Phone number '" + dto.getPhone() + "' already exists.");
            }

            student.setRollNo(dto.getRollNo());
            student.setName(dto.getName());
            student.setEmail(dto.getEmail());
            student.setPhone(dto.getPhone());
            student.setGender(dto.getGender());
            student.setDepartment(dto.getDepartment());
            student.setYear(dto.getYear());
            student.setCurrentSemester(dto.getCurrentSemester());
            student.setDateOfBirth(dto.getDateOfBirth());
            student.setBloodGroup(dto.getBloodGroup());
            student.setEmergencyContact(dto.getEmergencyContact());
            student.setAddress(dto.getAddress());
            student.setEnrollmentDate(dto.getEnrollmentDate());

            Student updated = studentRepository.save(student);
            return mapToDTO(updated);
            
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message.contains("UKdqf52k0x0xhaoxq9a3vqkd457") || message.contains("roll_no")) {
                throw new RuntimeException("Roll number '" + dto.getRollNo() + "' already exists. Please use a different roll number.");
            } else if (message.contains("UK_e2rndfrsx22acpq2ty1caeuyw") || message.contains("email")) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' already exists. Please use a different email.");
            } else if (message.contains("UK_4j48kma5fa3dcya13gd0l3gi") || message.contains("phone")) {
                throw new RuntimeException("Phone number '" + dto.getPhone() + "' already exists. Please use a different phone number.");
            }
            throw new RuntimeException("Failed to update student: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update student: " + e.getMessage());
        }
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findByDeletedFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        return mapToDTO(student);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        student.setDeleted(true);
        studentRepository.save(student);
    }

    @Override
    public StudentDTO getStudentByRollNo(String rollNo) {
        Student student = studentRepository.findByRollNoAndDeletedFalse(rollNo)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "roll number", rollNo));
        return mapToDTO(student);
    }

    @Override
    public List<StudentDTO> getStudentsByDepartment(Department department) {
        return studentRepository.findByDepartmentAndDeletedFalse(department)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getStudentsByYear(Integer year) {
        return studentRepository.findByYearAndDeletedFalse(year)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getStudentsByDepartmentsAndYear(
            List<Department> departments,
            Integer year) {
        return studentRepository
                .findByDepartmentInAndYearAndDeletedFalse(departments, year)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Department> getAllDepartments() {
        return studentRepository.findDistinctDepartments();
    }

    @Override
    public boolean validateDefaultLogin(String email, String enteredPassword) {
        Student student = studentRepository
                .findByEmailIgnoreCaseAndDeletedFalse(email)
                .orElse(null);

        if (student == null) return false;

        return passwordEncoder.matches(enteredPassword, student.getPassword());
    }

    @Override
    @Transactional
    public void resetPassword(Long studentId, String newPassword) {
        Student student = studentRepository.findByIdAndDeletedFalse(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);
    }

    @Override
    @Transactional
    public void resetToDefaultPassword(Long studentId) {
        Student student = studentRepository.findByIdAndDeletedFalse(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        StudentDTO dto = mapToDTO(student);
        student.setPassword(passwordEncoder.encode(generateDefaultPassword(dto)));

        studentRepository.save(student);
    }

    @Override
    public String getDefaultPassword(StudentDTO dto) {
        return generateDefaultPassword(dto);
    }

    @Override
    public Student findStudentByEmail(String email) {
        return studentRepository.findByEmailIgnoreCaseAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", email));
    }

    private Student mapToEntity(StudentDTO dto) {
        Student student = new Student();
        student.setRollNo(dto.getRollNo());
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setPhone(dto.getPhone());
        student.setGender(dto.getGender());
        student.setDepartment(dto.getDepartment());
        student.setYear(dto.getYear());
        student.setCurrentSemester(dto.getCurrentSemester());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setBloodGroup(dto.getBloodGroup());
        student.setEmergencyContact(dto.getEmergencyContact());
        student.setAddress(dto.getAddress());
        student.setEnrollmentDate(dto.getEnrollmentDate());
        student.setDeleted(false);
        return student;
    }

    private StudentDTO mapToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setRollNo(student.getRollNo());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setGender(student.getGender());
        dto.setDepartment(student.getDepartment());
        dto.setYear(student.getYear());
        dto.setCurrentSemester(student.getCurrentSemester());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setBloodGroup(student.getBloodGroup());
        dto.setEmergencyContact(student.getEmergencyContact());
        dto.setAddress(student.getAddress());
        dto.setEnrollmentDate(student.getEnrollmentDate());
        return dto;
    }

    private String generateDefaultPassword(StudentDTO student) {
        String first3 = student.getName().length() >= 3
                ? student.getName().substring(0, 3).toUpperCase()
                : student.getName().toUpperCase();

        String phone = student.getPhone();
        String last3 = phone.substring(Math.max(phone.length() - 3, 0));

        return first3 + last3 + student.getYear() + "@123";
    }
	
}