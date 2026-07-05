package com.exam.seating.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectDTO {

    private Long id;
    private String subjectName;
    private String subjectCode;
    
    private LocalDate examDate; 
    private LocalTime startTime;
    private LocalTime endTime;

    private Integer duration;
    private Integer totalMarks;

    private Long examId;
}