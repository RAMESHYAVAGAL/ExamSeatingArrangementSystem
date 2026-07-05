package com.exam.seating.dto;

import com.exam.seating.enums.Department;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentChartDTO {

    private Department department;
    private long studentCount;
    
    public String getDepartmentName() {
        return department != null ? department.name() : "Unknown";
    }
}