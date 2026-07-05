package com.exam.seating.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {

    private long totalStudents;
    private long totalExams;
    private long totalRooms;
    private long totalInvigilators;
}