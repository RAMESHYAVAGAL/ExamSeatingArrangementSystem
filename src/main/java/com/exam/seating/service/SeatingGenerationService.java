package com.exam.seating.service;

import com.exam.seating.dto.SeatingGenerationDTO;
import java.util.List;

public interface SeatingGenerationService {

    SeatingGenerationDTO generateSeating(SeatingGenerationDTO dto);

    SeatingGenerationDTO updateSeating(Long id, SeatingGenerationDTO dto);

    List<SeatingGenerationDTO> getAllGeneratedSeating();

    SeatingGenerationDTO getById(Long id);

    void deleteSeating(Long id);

    boolean canEditSeating(Long seatingId);
}