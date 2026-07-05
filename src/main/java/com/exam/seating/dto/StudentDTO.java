package com.exam.seating.dto;

import lombok.*;

import java.time.LocalDate;

import com.exam.seating.enums.Department;
import com.exam.seating.enums.Gender;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {

    private Long id;
    private String rollNo;
    private String name;
    private Integer year;
    private String email;
    private String phone;
    private Gender gender;
    private Department department;

    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String emergencyContact;
    private String address;

    private Integer currentSemester;
    private LocalDate enrollmentDate;
}