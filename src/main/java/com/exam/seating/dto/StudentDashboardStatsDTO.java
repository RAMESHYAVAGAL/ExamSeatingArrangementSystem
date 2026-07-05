package com.exam.seating.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDashboardStatsDTO {

    private Long studentId;
    private String studentName;
    private String rollNo;

    private Long totalExams;
    private Long hallTicketsCount;
    private Long upcomingExamsCount;
    private Long completedExamsCount;
    private Long currentExamsCount;

    private Double attendancePercentage;
    private Double currentCGPA;

    private Boolean hasExamsToday;
}