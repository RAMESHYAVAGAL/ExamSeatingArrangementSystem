package com.exam.seating.repository;

import com.exam.seating.entity.Invigilator;
import com.exam.seating.enums.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvigilatorRepository extends JpaRepository<Invigilator, Long> {

    List<Invigilator> findByDeletedFalse();

    Optional<Invigilator> findByIdAndDeletedFalse(Long id);

    boolean existsByEmployeeIdAndDeletedFalse(String employeeId);

    boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

    Optional<Invigilator> findByEmailIgnoreCaseAndDeletedFalse(String email);
    
    Optional<Invigilator> findByEmailIgnoreCase(String email);
    
    List<Invigilator> findByDepartmentAndDeletedFalse(Department department);
    
    boolean existsByEmployeeId(String employeeId);
    
    boolean existsByEmailIgnoreCase(String email);
    
    boolean existsByPhone(String phone);
    
    boolean existsByPhoneAndDeletedFalse(String phone);
    
    Optional<Invigilator> findByEmployeeId(String employeeId);
    
    Optional<Invigilator> findByEmail(String email);
    
    boolean existsByEmail(String email);
}