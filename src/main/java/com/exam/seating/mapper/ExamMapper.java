package com.exam.seating.mapper;

import com.exam.seating.dto.ExamDTO;
import com.exam.seating.entity.Exam;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExamMapper {

    ExamDTO toDTO(Exam exam);

    Exam toEntity(ExamDTO dto);
}