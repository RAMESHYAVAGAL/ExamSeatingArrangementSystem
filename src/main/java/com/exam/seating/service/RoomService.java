package com.exam.seating.service;

import com.exam.seating.dto.RoomDTO;
import java.util.List;

public interface RoomService {

    RoomDTO saveRoom(RoomDTO dto);

    RoomDTO updateRoom(Long id, RoomDTO dto);

    List<RoomDTO> getAllRooms();

    RoomDTO getRoomById(Long id);

    void deleteRoom(Long id);

    List<RoomDTO> searchRooms(String query);
}