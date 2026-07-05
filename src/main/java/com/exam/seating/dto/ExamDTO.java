package com.exam.seating.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamDTO {

    private Long id;
    private String examName;
    private String examCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer year;
    private Integer semester;
    private Integer subjectCount;
}