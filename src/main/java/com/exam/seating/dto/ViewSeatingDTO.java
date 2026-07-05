package com.exam.seating.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewSeatingDTO {

    private Long examId;
    private String examName;
    private String examDate;
    private String examTime;
    private String duration;

    private Integer year;
    private Integer semester;

    private Long roomId;
    private String roomName;

    private Long invigilatorId;
    private String invigilatorName;

    private List<SeatDTO> seats;
}