package com.exam.seating.dto;

import com.exam.seating.enums.SeatingStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class SeatingGenerationDTO {

    private Long id;
    private Long examId;

    private String departments;
    private Integer year;
    private Integer semester;
    private String rooms;

    private SeatingStatus status;

    private boolean hallTicketsGenerated;
}