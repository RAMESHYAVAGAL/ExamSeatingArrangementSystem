package com.exam.seating.service;

import java.util.List;

import com.exam.seating.dto.InvigilatorAssignDTO;
import com.exam.seating.dto.InvigilatorDTO;
import com.exam.seating.dto.ViewSeatingDTO;

public interface ViewSeatingService {

    ViewSeatingDTO getSeating(
            Long examId,
            Long roomId,
            Integer year,
            Integer semester
    );

    void assignInvigilator(InvigilatorAssignDTO dto);
    
    List<InvigilatorDTO> getAvailableInvigilatorsForExam(Long examId);
}