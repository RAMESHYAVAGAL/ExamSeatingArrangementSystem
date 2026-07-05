package com.exam.seating.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {

    private Integer seatNumber;
    private String rollNo;
    private String hallTicketNo;
    private String studentName;
    private String department;
    private Integer year;
}