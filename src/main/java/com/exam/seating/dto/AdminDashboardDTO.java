package com.exam.seating.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {

    private long totalStudents;
    private long totalExams;
    private long totalRooms;
    private long totalInvigilators;

    private long todayExams;
    private long todayChange;
    private long upcomingExams;

    private boolean seatingGenerated;

    private long studentsAssigned;
    private int roomUtilization;
    private long freeInvigilators;

    private long pendingExams;
    private List<String> pendingExamNames;

    private int generatedPercentage;
    private int pendingPercentage;
}