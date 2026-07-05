package com.exam.seating.dto;

import lombok.*;
import java.time.LocalDate;

import com.exam.seating.enums.Department;
import com.exam.seating.enums.Gender;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileDTO {

    private Long id;
    private String name;
    private String rollNo;
    private String email;
    private String phone;
    private Gender gender;
    private Department department;
    private Integer year;

    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String emergencyContact;
    private String address;
    private Integer currentSemester;
    private LocalDate enrollmentDate;
}