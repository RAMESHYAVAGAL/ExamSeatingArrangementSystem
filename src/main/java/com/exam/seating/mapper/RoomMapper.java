package com.exam.seating.mapper;

import com.exam.seating.dto.RoomDTO;
import com.exam.seating.entity.Room;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomDTO toDTO(Room room);

    Room toEntity(RoomDTO dto);
}