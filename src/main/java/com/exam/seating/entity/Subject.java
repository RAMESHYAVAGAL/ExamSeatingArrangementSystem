package com.exam.seating.entity;

import com.exam.seating.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "subjects",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "subjectCode")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String subjectName;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String subjectCode;

    @NotNull
    @Column(nullable = false)
    private LocalDate examDate;  
    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @Positive
    @Column(nullable = false)
    private Integer duration; 

    @Positive
    @Column(nullable = false)
    private Integer totalMarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
}