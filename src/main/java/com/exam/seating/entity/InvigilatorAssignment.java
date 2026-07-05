package com.exam.seating.entity;

import com.exam.seating.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "invigilator_assignment",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UKhf269mgw53lbpv3xbjxdeuksn",
            columnNames = {"exam_id", "room_id", "year", "semester"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvigilatorAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invigilator_id", nullable = false)
    private Invigilator invigilator;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "semester", nullable = false)
    private Integer semester;
}