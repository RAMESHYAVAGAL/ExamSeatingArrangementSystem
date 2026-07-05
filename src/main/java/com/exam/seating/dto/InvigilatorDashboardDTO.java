package com.exam.seating.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvigilatorDashboardDTO {

    private String invigilatorName;

    private long assignedExams;
    private long activeRooms;
    private long totalStudents;

    private String avgDuration;
    private long todayExams;

    private List<ExamItemDTO> todayExamsList;
    private List<ExamItemDTO> upcomingExamsList;
}