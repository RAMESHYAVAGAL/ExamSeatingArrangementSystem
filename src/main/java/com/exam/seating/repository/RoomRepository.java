package com.exam.seating.repository;

import com.exam.seating.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByDeletedFalse();

    Optional<Room> findByIdAndDeletedFalse(Long id);

    boolean existsByRoomCodeAndDeletedFalse(String roomCode);

    Optional<Room> findByRoomCodeAndDeletedFalse(String roomCode);
    
    List<Room> findByCapacityGreaterThanEqualAndDeletedFalse(Integer capacity);
    
    List<Room> findByRoomNameContainingIgnoreCaseAndDeletedFalse(String roomName);
    
    Optional<Room> findByRoomCode(String roomCode);
    boolean existsByRoomCode(String roomCode);
}