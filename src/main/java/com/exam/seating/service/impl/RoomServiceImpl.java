package com.exam.seating.service.impl;

import com.exam.seating.dto.RoomDTO;
import com.exam.seating.entity.Room;
import com.exam.seating.repository.RoomRepository;
import com.exam.seating.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomDTO saveRoom(RoomDTO dto) {

        if (roomRepository.existsByRoomCodeAndDeletedFalse(dto.getRoomCode())) {
            throw new RuntimeException("Room code already exists");
        }

        validateCapacity(dto.getRows(), dto.getCols(), dto.getCapacity());

        Room room = mapToEntity(dto);
        room = roomRepository.save(room);

        return mapToDTO(room);
    }

    @Override
    public RoomDTO updateRoom(Long id, RoomDTO dto) {

        Room room = roomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        validateCapacity(dto.getRows(), dto.getCols(), dto.getCapacity());

        room.setRoomCode(dto.getRoomCode());
        room.setRoomName(dto.getRoomName());
        room.setCapacity(dto.getCapacity());
        room.setRows(dto.getRows());
        room.setCols(dto.getCols());
        room.setLocation(dto.getLocation());

        return mapToDTO(roomRepository.save(room));
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findByDeletedFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoomDTO getRoomById(Long id) {

        Room room = roomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return mapToDTO(room);
    }

    @Override
    public void deleteRoom(Long id) {

        Room room = roomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setDeleted(true);
        roomRepository.save(room);
    }

    private void validateCapacity(int rows, int cols, int capacity) {

        int maxSeats = rows * cols;

        if (capacity > maxSeats) {
            throw new RuntimeException(
                    "Capacity cannot exceed rows × columns (" + maxSeats + ")"
            );
        }
    }

    private Room mapToEntity(RoomDTO dto) {
        Room room = new Room();
        room.setRoomCode(dto.getRoomCode());
        room.setRoomName(dto.getRoomName());
        room.setCapacity(dto.getCapacity());
        room.setRows(dto.getRows());
        room.setCols(dto.getCols());
        room.setLocation(dto.getLocation());
        room.setDeleted(false);
        return room;
    }

    private RoomDTO mapToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setRoomCode(room.getRoomCode());
        dto.setRoomName(room.getRoomName());
        dto.setCapacity(room.getCapacity());
        dto.setRows(room.getRows());
        dto.setCols(room.getCols());
        dto.setLocation(room.getLocation());
        return dto;
    }
    
    @Override
    public List<RoomDTO> searchRooms(String query) {

        if (query == null || query.isBlank()) {
            return getAllRooms();
        }

        return roomRepository
                .findByRoomNameContainingIgnoreCaseAndDeletedFalse(query)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}