package com.exam.seating.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {

    private Long id;
    private Long roomId;
    private Long examId;

    private String roomCode;
    private String roomName;
    private Integer capacity;
    private Integer rows;
    private Integer cols;
    private String location;

    private String examName;
    private String examDate;
    private String examTime;
    private String duration;

    private Integer studentCount;
    private Integer capacityPercentage;

    private String status;
}