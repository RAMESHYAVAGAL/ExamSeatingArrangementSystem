package com.exam.seating.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamItemDTO {

    private Long examId;
    private Long roomId;

    private String examName;
    private String examDate;
    private String examTime;
    private String roomName;
}