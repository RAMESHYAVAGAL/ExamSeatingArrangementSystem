package com.exam.seating.mapper;

import com.exam.seating.dto.HallTicketDTO;
import com.exam.seating.entity.HallTicket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HallTicketMapper {

    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "student.name", target = "studentName")
    @Mapping(source = "student.rollNo", target = "rollNo")
    @Mapping(source = "hallTicketNumber", target = "hallTicketNo")
    @Mapping(source = "student.department", target = "department")
    @Mapping(source = "student.email", target = "studentEmail")
    @Mapping(source = "student.phone", target = "studentPhone")
    @Mapping(source = "student.year", target = "year")
    @Mapping(source = "student.currentSemester", target = "semester")
    HallTicketDTO toDTO(HallTicket hallTicket);
}