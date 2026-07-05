package com.exam.seating.service.impl;

import com.exam.seating.dto.SeatingGenerationDTO;
import com.exam.seating.entity.*;
import com.exam.seating.enums.Department;
import com.exam.seating.enums.SeatingStatus;
import com.exam.seating.repository.*;
import com.exam.seating.service.SeatingGenerationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeatingGenerationServiceImpl implements SeatingGenerationService {

    private final SeatingGenerationRepository seatingRepo;
    private final SeatAllocationRepository seatRepo;
    private final RoomRepository roomRepo;
    private final StudentRepository studentRepo;
    private final InvigilatorAssignmentRepository invigilatorAssignmentRepository;
    private final HallTicketRepository hallTicketRepository;
    private final ExamRepository examRepository;

    public SeatingGenerationServiceImpl(
            SeatingGenerationRepository seatingRepo,
            SeatAllocationRepository seatRepo,
            RoomRepository roomRepo,
            StudentRepository studentRepo,
            InvigilatorAssignmentRepository invigilatorAssignmentRepository,
            HallTicketRepository hallTicketRepository,
            ExamRepository examRepository) {

        this.seatingRepo = seatingRepo;
        this.seatRepo = seatRepo;
        this.roomRepo = roomRepo;
        this.studentRepo = studentRepo;
        this.invigilatorAssignmentRepository = invigilatorAssignmentRepository;
        this.hallTicketRepository = hallTicketRepository;
        this.examRepository = examRepository;
    }

    private List<Department> parseDepartments(String departmentsCsv) {
        if (departmentsCsv == null || departmentsCsv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(departmentsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Department::valueOf)
                .collect(Collectors.toList());
    }

    private List<Room> parseRooms(String roomCsv) {
        List<Room> rooms = new ArrayList<>();
        
        if (roomCsv == null || roomCsv.trim().isEmpty()) {
            return rooms;
        }

        for (String code : roomCsv.split(",")) {
            String trimmed = code.trim();
            if (!trimmed.isEmpty()) {
                Room room = roomRepo.findByRoomCodeAndDeletedFalse(trimmed)
                        .orElseThrow(() ->
                                new RuntimeException("Room not found: " + trimmed));
                rooms.add(room);
            }
        }

        return rooms;
    }

    private SeatingGenerationDTO mapToDTO(SeatingGeneration e) {
        SeatingGenerationDTO dto = new SeatingGenerationDTO();

        dto.setId(e.getId());
        dto.setExamId(e.getExam().getId());
        dto.setYear(e.getYear());
        dto.setSemester(e.getSemester());
        dto.setStatus(e.getStatus());

        if (e.getDepartments() != null && !e.getDepartments().isEmpty()) {
            dto.setDepartments(
                    e.getDepartments()
                            .stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(","))
            );
        }

        if (e.getRooms() != null && !e.getRooms().isEmpty()) {
            dto.setRooms(
                    e.getRooms()
                            .stream()
                            .map(Room::getRoomCode)
                            .collect(Collectors.joining(","))
            );
        }

        boolean hasHallTickets =
                hallTicketRepository.existsByExam_IdAndYearAndSemesterAndDeletedFalse(
                        e.getExam().getId(),
                        e.getYear(),
                        e.getSemester()
                );

        dto.setHallTicketsGenerated(hasHallTickets);

        return dto;
    }
    
    private void allocateSeats(
            Exam exam,
            List<Student> students,
            List<Room> rooms,
            Integer year,
            Integer semester
    ) {
        Collections.shuffle(students);

        int studentIndex = 0;
        List<SeatAllocation> allocations = new ArrayList<>();

        for (Room room : rooms) {
            int seatNo = 1;
            int rows = room.getRows() != null ? room.getRows() : 5;
            int cols = room.getCols() != null ? room.getCols() : 5;

            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {

                    if (studentIndex >= students.size()) {
                        if (!allocations.isEmpty()) {
                            seatRepo.saveAll(allocations);
                        }
                        return;
                    }

                    Student student = students.get(studentIndex++);

                    SeatAllocation seat = new SeatAllocation();
                    seat.setExam(exam);
                    seat.setRoom(room);
                    seat.setStudent(student);
                    seat.setSeatNumber(seatNo++);
                    seat.setRowNo(r);
                    seat.setColNo(c);
                    seat.setYear(year);
                    seat.setSemester(semester);
                    seat.setDeleted(false);

                    allocations.add(seat);
                    
                    if (allocations.size() >= 100) {
                        seatRepo.saveAll(allocations);
                        allocations.clear();
                    }
                }
            }
        }

        if (!allocations.isEmpty()) {
            seatRepo.saveAll(allocations);
        }
    }

    @Override
    public SeatingGenerationDTO generateSeating(SeatingGenerationDTO dto) {

        Exam exam = examRepository.findByIdAndDeletedFalse(dto.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        Integer year = dto.getYear();
        Integer semester = dto.getSemester();

        List<Department> departments = parseDepartments(dto.getDepartments());

        if (departments.isEmpty()) {
            throw new RuntimeException("No departments selected");
        }

        List<Student> students =
                studentRepo.findByDepartmentInAndYearAndCurrentSemester(
                        departments,
                        year,
                        semester
                );

        if (students.isEmpty()) {
            throw new RuntimeException("No students found for the selected criteria");
        }

        List<Room> rooms = parseRooms(dto.getRooms());

        if (rooms.isEmpty()) {
            throw new RuntimeException("No rooms selected");
        }

        boolean alreadyExists = seatingRepo.findByDeletedFalse()
                .stream()
                .anyMatch(s ->
                        s.getExam().getId().equals(dto.getExamId())
                                && s.getYear().equals(year)
                                && s.getSemester().equals(semester));

        if (alreadyExists) {
            throw new RuntimeException(
                    "Seating already exists for this exam/year/semester");
        }

        int totalCapacity = rooms.stream()
                .mapToInt(r -> (r.getRows() != null ? r.getRows() : 5) * 
                               (r.getCols() != null ? r.getCols() : 5))
                .sum();

        if (students.size() > totalCapacity) {
            throw new RuntimeException(
                    "Insufficient room capacity. Required: "
                            + students.size()
                            + ", Available: "
                            + totalCapacity
            );
        }

        for (Room room : rooms) {
            boolean occupied =
                    seatRepo.existsByExam_IdAndRoom_IdAndDeletedFalse(
                            exam.getId(),
                            room.getId()
                    );

            if (occupied) {
                throw new RuntimeException(
                        "Room already allocated: " + room.getRoomCode()
                );
            }
        }

        allocateSeats(exam, students, rooms, year, semester);

        SeatingGeneration entity = new SeatingGeneration();
        entity.setExam(exam);
        entity.setDepartments(departments);
        entity.setRooms(rooms);
        entity.setYear(year);
        entity.setSemester(semester);
        entity.setStatus(SeatingStatus.GENERATED);
        entity.setDeleted(false);

        return mapToDTO(seatingRepo.save(entity));
    }

    @Override
    @Transactional
    public SeatingGenerationDTO updateSeating(Long id, SeatingGenerationDTO dto) {
        try {
            SeatingGeneration entity = seatingRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Seating not found"));

            if (entity.isDeleted()) {
                throw new RuntimeException("Seating arrangement has been deleted");
            }

            boolean hasHallTickets = hallTicketRepository
                    .existsByExam_IdAndYearAndSemesterAndDeletedFalse(
                            entity.getExam().getId(),
                            entity.getYear(),
                            entity.getSemester()
                    );

            if (hasHallTickets) {
                throw new RuntimeException("Cannot update seating after hall tickets generated");
            }

            List<Department> departments = parseDepartments(dto.getDepartments());
            if (departments.isEmpty()) {
                throw new RuntimeException("No departments selected");
            }

            List<Room> rooms = parseRooms(dto.getRooms());
            if (rooms.isEmpty()) {
                throw new RuntimeException("No rooms selected");
            }

            Integer year = entity.getYear();
            Integer semester = entity.getSemester();
            
            List<Student> students = studentRepo
                    .findByDepartmentInAndYearAndCurrentSemester(
                            departments,
                            year,
                            semester
                    );

            if (students.isEmpty()) {
                throw new RuntimeException("No students found for the selected departments");
            }

            int totalCapacity = rooms.stream()
                    .mapToInt(r -> (r.getRows() != null ? r.getRows() : 5) * 
                                   (r.getCols() != null ? r.getCols() : 5))
                    .sum();

            if (students.size() > totalCapacity) {
                throw new RuntimeException(
                        "Insufficient room capacity. Required: " + students.size() +
                        ", Available: " + totalCapacity
                );
            }

            List<SeatAllocation> currentSeats = seatRepo
                    .findByExam_IdAndYearAndSemesterAndDeletedFalse(
                            entity.getExam().getId(),
                            year,
                            semester
                    );

            Collections.shuffle(students);
            int studentIndex = 0;
            List<SeatAllocation> updatedSeats = new ArrayList<>();
            List<SeatAllocation> newSeats = new ArrayList<>();

            for (SeatAllocation seat : currentSeats) {
                if (studentIndex >= students.size()) {
                    seat.setDeleted(true);
                    updatedSeats.add(seat);
                    continue;
                }
                
                seat.setStudent(students.get(studentIndex++));
                seat.setDeleted(false);
                updatedSeats.add(seat);
            }

            while (studentIndex < students.size()) {
                Room assignedRoom = null;
                int seatNo = 1;
                int row = 1;
                int col = 1;
                
                for (Room room : rooms) {
                    long seatsInRoom = currentSeats.stream()
                            .filter(s -> s.getRoom().getId().equals(room.getId()) && !s.isDeleted())
                            .count();
                    
                    int roomCapacity = (room.getRows() != null ? room.getRows() : 5) * 
                                       (room.getCols() != null ? room.getCols() : 5);
                    
                    if (seatsInRoom < roomCapacity) {
                        assignedRoom = room;
                        int currentSeatCount = (int) seatsInRoom;
                        seatNo = currentSeatCount + 1;
                        row = (currentSeatCount / (room.getCols() != null ? room.getCols() : 5)) + 1;
                        col = (currentSeatCount % (room.getCols() != null ? room.getCols() : 5)) + 1;
                        break;
                    }
                }
                
                if (assignedRoom == null) {
                    throw new RuntimeException("No available seats in any room");
                }
                
                SeatAllocation newSeat = new SeatAllocation();
                newSeat.setExam(entity.getExam());
                newSeat.setRoom(assignedRoom);
                newSeat.setStudent(students.get(studentIndex++));
                newSeat.setSeatNumber(seatNo);
                newSeat.setRowNo(row);
                newSeat.setColNo(col);
                newSeat.setYear(year);
                newSeat.setSemester(semester);
                newSeat.setDeleted(false);
                newSeats.add(newSeat);
            }

            if (!updatedSeats.isEmpty()) {
                seatRepo.saveAll(updatedSeats);
                System.out.println("Updated " + updatedSeats.size() + " seat allocations");
            }
            
            if (!newSeats.isEmpty()) {
                seatRepo.saveAll(newSeats);
                System.out.println("Created " + newSeats.size() + " new seat allocations");
            }

            invigilatorAssignmentRepository.softDeleteByExamIdAndYearAndSemester(
                    entity.getExam().getId(),
                    year,
                    semester
            );

            entity.setDepartments(departments);
            entity.setRooms(rooms);
            entity.setStatus(SeatingStatus.REGENERATED);
            entity.setDeleted(false);

            SeatingGeneration updated = seatingRepo.save(entity);
            return mapToDTO(updated);

        } catch (Exception e) {
            System.err.println("Error updating seating: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update seating arrangement: " + e.getMessage());
        }
    }

    @Override
    public List<SeatingGenerationDTO> getAllGeneratedSeating() {
        return seatingRepo.findByDeletedFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SeatingGenerationDTO getById(Long id) {

        SeatingGeneration entity = seatingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Seating not found"));

        if (entity.isDeleted()) {
            throw new RuntimeException("Seating deleted");
        }

        return mapToDTO(entity);
    }

    @Override
    public void deleteSeating(Long id) {

        SeatingGeneration entity = seatingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Seating not found"));

        if (entity.isDeleted()) {
            return;
        }

        Long examId = entity.getExam().getId();
        Integer year = entity.getYear();
        Integer semester = entity.getSemester();
        entity.setDeleted(true);
        seatingRepo.save(entity);

        invigilatorAssignmentRepository
                .softDeleteByExamIdAndYearAndSemester(
                        examId,
                        year,
                        semester
                );

        hallTicketRepository
                .softDeleteByExamIdAndYearAndSemester(
                        examId,
                        year,
                        semester
                );

        List<SeatAllocation> seats =
                seatRepo.findByExam_IdAndYearAndSemesterAndDeletedFalse(
                        examId,
                        year,
                        semester
                );

        if (!seats.isEmpty()) {
            for (SeatAllocation seat : seats) {
                seat.setDeleted(true);
            }
            seatRepo.saveAll(seats);
            seatRepo.flush();
        }
    }

    @Override
    public boolean canEditSeating(Long seatingId) {

        SeatingGeneration entity = seatingRepo.findById(seatingId)
                .orElse(null);

        if (entity == null || entity.isDeleted()) {
            return false;
        }

        boolean hasHallTickets =
                hallTicketRepository.existsByExam_IdAndYearAndSemesterAndDeletedFalse(
                        entity.getExam().getId(),
                        entity.getYear(),
                        entity.getSemester()
                );

        return !hasHallTickets;
    }
}