package com.exam.seating.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearSemesterChartDTO {

    private Integer year;
    private Integer semester;
    private long studentCount;
    
    public String getYearSemester() {
        if (year == null || semester == null) {
            return "Unknown";
        }
        return "Year " + year + " - Semester " + semester;
    }
}