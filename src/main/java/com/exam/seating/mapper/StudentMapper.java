package com.exam.seating.mapper;

import com.exam.seating.dto.StudentDTO;
import com.exam.seating.dto.StudentProfileDTO;
import com.exam.seating.entity.Student;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDTO toDTO(Student student);

    Student toEntity(StudentDTO dto);

    StudentProfileDTO toProfileDTO(Student student);
}