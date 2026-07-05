package com.exam.seating.mapper;

import com.exam.seating.dto.InvigilatorDTO;
import com.exam.seating.entity.Invigilator;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvigilatorMapper {

    InvigilatorDTO toDTO(Invigilator invigilator);

    Invigilator toEntity(InvigilatorDTO dto);
}