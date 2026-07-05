package com.exam.seating.mapper;

import com.exam.seating.dto.SeatDTO;
import com.exam.seating.entity.SeatAllocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatAllocationMapper {

    @Mapping(source = "student.rollNo", target = "rollNo")
    @Mapping(source = "student.name", target = "studentName")
    @Mapping(source = "student.department", target = "department")
    @Mapping(source = "seatNumber", target = "seatNumber")
    SeatDTO toDTO(SeatAllocation allocation);
}