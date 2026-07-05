package com.exam.seating.service.impl;

import com.exam.seating.dto.*;
import com.exam.seating.entity.*;
import com.exam.seating.repository.*;
import com.exam.seating.service.ViewSeatingService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ViewSeatingServiceImpl implements ViewSeatingService {

    private final SeatAllocationRepository seatRepo;
    private final InvigilatorRepository invRepo;
    private final RoomRepository roomRepo;
    private final StudentRepository studentRepo;
    private final InvigilatorAssignmentRepository invAssignmentRepo;
    private final ExamRepository examRepo;
    private final HallTicketRepository hallTicketRepository;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("hh:mm a");
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public ViewSeatingServiceImpl(
            SeatAllocationRepository seatRepo,
            InvigilatorRepository invRepo,
            RoomRepository roomRepo,
            StudentRepository studentRepo,
            InvigilatorAssignmentRepository invAssignmentRepo,
            ExamRepository examRepo,
            HallTicketRepository hallTicketRepository
    ) {
        this.seatRepo = seatRepo;
        this.invRepo = invRepo;
        this.roomRepo = roomRepo;
        this.studentRepo = studentRepo;
        this.invAssignmentRepo = invAssignmentRepo;
        this.examRepo = examRepo;
        this.hallTicketRepository = hallTicketRepository;
    }

    @Override
    public ViewSeatingDTO getSeating(Long examId, Long roomId, Integer year, Integer semester) {

        List<SeatAllocation> allocations =
                seatRepo.findByExam_IdAndRoom_IdAndYearAndSemesterAndDeletedFalse(
                        examId, roomId, year, semester);

        if (allocations.isEmpty()) {
            List<SeatAllocation> anySeats =
                    seatRepo.findByExam_IdAndRoom_IdAndDeletedFalse(examId, roomId);

            if (!anySeats.isEmpty()) {
                SeatAllocation first = anySeats.get(0);
                year = first.getYear();
                semester = first.getSemester();

                allocations = seatRepo
                        .findByExam_IdAndRoom_IdAndYearAndSemesterAndDeletedFalse(
                                examId, roomId, year, semester);
            }
        }

        ViewSeatingDTO dto = new ViewSeatingDTO();
        dto.setExamId(examId);
        dto.setRoomId(roomId);
        dto.setYear(year);
        dto.setSemester(semester);

        examRepo.findById(examId).ifPresent(exam -> {
            dto.setExamName(exam.getExamName());

            LocalDate startDate = exam.getStartDate();
            LocalDate endDate = exam.getEndDate();
            
            if (startDate.equals(endDate)) {
                dto.setExamDate(startDate.format(dateFormatter));
            } else {
                dto.setExamDate(startDate.format(dateFormatter) + " to " + 
                              endDate.format(dateFormatter));
            }

            if (!exam.getSubjects().isEmpty()) {
                Subject subject = exam.getSubjects().get(0);

                dto.setExamTime(
                        subject.getStartTime().format(timeFormatter)
                                + " - " +
                                subject.getEndTime().format(timeFormatter)
                );

                int mins = subject.getDuration();
                dto.setDuration(mins >= 60
                        ? (mins / 60) + " Hours" + (mins % 60 > 0 ? " " + (mins % 60) + " mins" : "")
                        : mins + " Minutes");
            }
        });

        roomRepo.findById(roomId).ifPresent(room ->
                dto.setRoomName(room.getRoomCode() + " - " + room.getRoomName())
        );

        Optional<InvigilatorAssignment> assignment =
                invAssignmentRepo.findByExam_IdAndRoom_IdAndYearAndSemesterAndDeletedFalse(
                        examId, roomId, year, semester
                );

        assignment.ifPresent(a -> {
            dto.setInvigilatorName(a.getInvigilator().getName());
            dto.setInvigilatorId(a.getInvigilator().getId());
        });

        List<HallTicket> hallTickets =
                hallTicketRepository.findByExamAndRoomAndYearAndSemester(
                        examId, roomId, year, semester);

        Map<Long, HallTicket> hallTicketMap = new HashMap<>();
        for (HallTicket ht : hallTickets) {
            hallTicketMap.put(ht.getStudent().getId(), ht);
        }

        List<SeatDTO> seats = new ArrayList<>();

        for (SeatAllocation allocation : allocations) {

            Student student = allocation.getStudent();

            SeatDTO seatDTO = new SeatDTO();
            seatDTO.setSeatNumber(allocation.getSeatNumber());

            if (student != null) {
                seatDTO.setRollNo(student.getRollNo());
                seatDTO.setStudentName(student.getName());
                seatDTO.setDepartment(student.getDepartment().name());
                seatDTO.setYear(student.getYear());

                HallTicket ticket = hallTicketMap.get(student.getId());

                if (ticket != null) {
                    seatDTO.setHallTicketNo(ticket.getHallTicketNumber());
                }
            } else {
                seatDTO.setRollNo("N/A");
                seatDTO.setStudentName("Student Not Found");
                seatDTO.setDepartment("N/A");
                seatDTO.setYear(null);
            }

            seats.add(seatDTO);
        }

        dto.setSeats(seats);
        return dto;
    }

    @Override
    public void assignInvigilator(InvigilatorAssignDTO dto) {
        Exam exam = examRepo.findById(dto.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + dto.getExamId()));
        
        Room room = roomRepo.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + dto.getRoomId()));
        
        Invigilator invigilator = invRepo.findById(dto.getInvigilatorId())
                .orElseThrow(() -> new RuntimeException("Invigilator not found with ID: " + dto.getInvigilatorId()));

        List<SeatAllocation> allocations = seatRepo
                .findByExam_IdAndRoom_IdAndDeletedFalse(
                        dto.getExamId(),
                        dto.getRoomId());
        
        if (allocations.isEmpty()) {
            throw new RuntimeException(
                "No seat allocations found for this exam and room. " +
                "Please generate seating first."
            );
        }

        Optional<InvigilatorAssignment> existing =
                invAssignmentRepo.findOptionalByExamIdAndRoomId(
                        dto.getExamId(),
                        dto.getRoomId());

        if (existing.isPresent()) {
            throw new RuntimeException(
                "An invigilator is already assigned to this exam room. " +
                "To reassign, please remove the existing assignment first."
            );
        }

        for (SeatAllocation allocation : allocations) {
            allocation.setInvigilator(invigilator);
        }
        seatRepo.saveAll(allocations);

        InvigilatorAssignment assignment = new InvigilatorAssignment();
        assignment.setExam(exam);
        assignment.setRoom(room);
        assignment.setInvigilator(invigilator);
        assignment.setYear(dto.getYear());
        assignment.setSemester(dto.getSemester());
        assignment.setDeleted(false);

        invAssignmentRepo.save(assignment);

        updateHallTicketsWithInvigilator(
                dto.getExamId(),
                dto.getRoomId(),
                invigilator
        );
    }

   @Override
   public List<InvigilatorDTO> getAvailableInvigilatorsForExam(Long examId) {
    List<Invigilator> allInvigilators = invRepo.findByDeletedFalse();
    
    List<Long> assignedInvigilatorIds = invAssignmentRepo.findByExam_IdAndDeletedFalse(examId)
            .stream()
            .map(assignment -> assignment.getInvigilator().getId())
            .collect(Collectors.toList());

    return allInvigilators.stream()
            .filter(inv -> !assignedInvigilatorIds.contains(inv.getId()))
            .map(this::mapToInvigilatorDTO)
            .collect(Collectors.toList());
}

    private void updateHallTicketsWithInvigilator(
            Long examId,
            Long roomId,
            Invigilator invigilator) {

        List<HallTicket> tickets = hallTicketRepository.findByExam_IdAndDeletedFalse(examId);

        for (HallTicket ticket : tickets) {
            if (ticket.getRoom() != null &&
                    ticket.getRoom().getId().equals(roomId)) {

                ticket.setInvigilator(invigilator);
                hallTicketRepository.save(ticket);
            }
        }
    }

    private InvigilatorDTO mapToInvigilatorDTO(Invigilator invigilator) {
        InvigilatorDTO dto = new InvigilatorDTO();
        dto.setId(invigilator.getId());
        dto.setEmployeeId(invigilator.getEmployeeId());
        dto.setName(invigilator.getName());
        dto.setPhone(invigilator.getPhone());
        dto.setEmail(invigilator.getEmail());
        dto.setDepartment(invigilator.getDepartment());
        return dto;
    }
}