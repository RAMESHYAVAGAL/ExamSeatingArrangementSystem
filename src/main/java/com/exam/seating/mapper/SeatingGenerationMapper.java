package com.exam.seating.mapper;

import com.exam.seating.dto.SeatingGenerationDTO;
import com.exam.seating.entity.Room;
import com.exam.seating.entity.SeatingGeneration;
import com.exam.seating.enums.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SeatingGenerationMapper {

    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "rooms", target = "rooms", qualifiedByName = "roomsToString")
    @Mapping(source = "departments", target = "departments", qualifiedByName = "departmentsToString")
    SeatingGenerationDTO toDTO(SeatingGeneration entity);

    @Named("roomsToString")
    default String roomsToString(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return "";
        }
        return rooms.stream()
                .map(Room::getRoomCode)
                .collect(Collectors.joining(","));
    }

    @Named("departmentsToString")
    default String departmentsToString(List<Department> departments) {
        if (departments == null || departments.isEmpty()) {
            return "";
        }
        return departments.stream()
                .map(Department::name)
                .collect(Collectors.joining(","));
    }
}