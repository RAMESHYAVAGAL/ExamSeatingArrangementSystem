package com.exam.seating.dto;

import com.exam.seating.enums.Department;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvigilatorDTO {

    private Long id;
    private String employeeId;
    private String name;
    private String phone;
    private String email;
    private Department department;
}