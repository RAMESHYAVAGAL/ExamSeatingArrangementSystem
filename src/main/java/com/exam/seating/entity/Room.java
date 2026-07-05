package com.exam.seating.entity;

import com.exam.seating.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(
    name = "rooms",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "room_code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room extends BaseEntity {

    @NotBlank
    @Column(name = "room_code", nullable = false, unique = true, length = 30)
    private String roomCode;

    @NotBlank
    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer capacity;

    @NotNull
    @Positive
    @Column(name = "row_count", nullable = false)
    private Integer rows;

    @NotNull
    @Positive
    @Column(name = "col_count", nullable = false)
    private Integer cols;

    @Column(length = 100)
    private String location;
}