package com.exam.seating.entity;

import com.exam.seating.entity.base.BaseEntity;
import com.exam.seating.enums.Department;
import com.exam.seating.enums.SeatingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seating_generation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatingGeneration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ElementCollection(targetClass = Department.class)
    @CollectionTable(
        name = "seating_generation_departments",
        joinColumns = @JoinColumn(name = "generation_id")
    )
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<Department> departments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "seating_generation_rooms",
        joinColumns = @JoinColumn(name = "generation_id"),
        inverseJoinColumns = @JoinColumn(name = "room_id")
    )
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer semester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatingStatus status;
}