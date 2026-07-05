package com.exam.seating.entity;

import com.exam.seating.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "hall_tickets",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "hall_ticket_number")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallTicket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invigilator_id")
    private Invigilator invigilator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @NotBlank
    @Column(name = "hall_ticket_number", nullable = false, unique = true)
    private String hallTicketNumber;

    @Column(name = "row_no")
    private Integer rowNo;

    @Column(name = "col_no")
    private Integer colNo;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    private Integer year;

    private Integer semester;

    @PrePersist
    public void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}