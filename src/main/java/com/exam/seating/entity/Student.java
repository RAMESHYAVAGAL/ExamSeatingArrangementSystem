package com.exam.seating.entity;

import com.exam.seating.entity.base.BaseEntity;
import com.exam.seating.enums.Department;
import com.exam.seating.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "students",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "roll_no"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {

    @NotBlank
    @Column(name = "roll_no", nullable = false, unique = true, length = 30)
    private String rollNo;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @NotNull
    @Column(nullable = false)
    private Integer year;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "current_semester")
    private Integer currentSemester;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;
}