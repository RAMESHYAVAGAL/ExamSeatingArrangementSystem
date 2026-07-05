package com.exam.seating.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamReportDTO {

    private String examName;
    private LocalDate examDate;
    private long studentCount;
    private long roomCount;
}