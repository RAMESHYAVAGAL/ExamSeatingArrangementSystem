package com.exam.seating.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvigilatorAssignDTO {

    @NotNull
    private Long examId;

    @NotNull
    private Long roomId;

    @NotNull
    private Long invigilatorId;

    @NotNull
    private Integer year;

    @NotNull
    private Integer semester;
}