package com.exam.seating.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedExamDTO {

    private Long examId;
    private Long roomId;

    private Integer year;
    private Integer semester;

    private String examName;
    private String roomName;

    private LocalDate examDate;
    private String examDateDisplay;
    private String examTime;         
    private String capacity;    
}