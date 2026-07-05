package com.exam.seating.mapper;

import com.exam.seating.dto.SeatDTO;
import com.exam.seating.dto.ViewSeatingDTO;
import com.exam.seating.entity.Exam;
import com.exam.seating.entity.Invigilator;
import com.exam.seating.entity.Room;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ViewSeatingMapper {

    public ViewSeatingDTO map(
            Exam exam,
            Room room,
            Invigilator invigilator,
            List<SeatDTO> seats
    ) {
        return ViewSeatingDTO.builder()
                .examId(exam.getId())
                .examName(exam.getExamName())
                .roomId(room.getId())
                .roomName(room.getRoomName())
                .year(exam.getYear())
                .semester(exam.getSemester())
                .invigilatorId(invigilator.getId())
                .invigilatorName(invigilator.getName())
                .seats(seats)
                .build();
    }
}