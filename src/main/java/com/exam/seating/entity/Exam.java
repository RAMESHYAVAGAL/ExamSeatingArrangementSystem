package com.exam.seating.entity;

import com.exam.seating.entity.base.BaseEntity;
import com.exam.seating.enums.ExamStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam extends BaseEntity {

    @Column(nullable = false)
    private String examName;

    @Column(nullable = false, unique = true)
    private String examCode;

    @Column(nullable = false)
    private LocalDate startDate; 

    @Column(nullable = false)
    private LocalDate endDate;    

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer semester;

    @Enumerated(EnumType.STRING)
    private ExamStatus status;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();
}