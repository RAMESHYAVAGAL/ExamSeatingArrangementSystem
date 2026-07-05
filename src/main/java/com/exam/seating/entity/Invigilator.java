package com.exam.seating.entity;

import com.exam.seating.entity.base.BaseEntity;
import com.exam.seating.enums.Department;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(
    name = "invigilators",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "employee_id"),
        @UniqueConstraint(columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invigilator extends BaseEntity {

    @NotBlank
    @Column(name = "employee_id", nullable = false, unique = true, length = 30)
    private String employeeId;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 15)
    private String phone;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "first_login", nullable = false)
    private boolean firstLogin = true;
}