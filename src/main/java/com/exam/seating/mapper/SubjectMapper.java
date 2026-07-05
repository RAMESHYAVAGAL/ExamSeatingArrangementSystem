package com.exam.seating.mapper;

import com.exam.seating.dto.SubjectDTO;
import com.exam.seating.entity.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    @Mapping(source = "exam.id", target = "examId")
    SubjectDTO toDTO(Subject subject);

    @Mapping(target = "exam", ignore = true)
    Subject toEntity(SubjectDTO dto);
}