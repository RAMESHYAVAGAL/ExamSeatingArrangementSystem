package com.exam.seating.service.impl;

import com.exam.seating.dto.HallTicketDTO;
import com.exam.seating.entity.*;
import com.exam.seating.repository.*;
import com.exam.seating.service.HallTicketService;
import com.exam.seating.service.PDFGeneratorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HallTicketServiceImpl implements HallTicketService {

    private final ExamRepository examRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final HallTicketRepository hallTicketRepository;
    private final InvigilatorRepository invigilatorRepository;
    private final InvigilatorAssignmentRepository invigilatorAssignmentRepository;
    private final PDFGeneratorService pdfGeneratorService;

    @Override
    @Transactional
    public Map<String, Object> generateHallTicketsForExam(Long examId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Generating hall tickets for exam ID: {}", examId);
            
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + examId));

            List<SeatAllocation> allocations = seatAllocationRepository
                    .findByExam_IdAndDeletedFalse(examId);

            if (allocations.isEmpty()) {
                throw new RuntimeException("No seat allocations found for this exam");
            }

            log.info("Found {} seat allocations for exam", allocations.size());

            int generated = 0;
            int skipped = 0;

            for (SeatAllocation allocation : allocations) {
                Student student = allocation.getStudent();
       
                Optional<HallTicket> existingTicket = hallTicketRepository
                        .findByStudent_IdAndExam_Id(student.getId(), examId);
                
                if (existingTicket.isPresent()) {
                    HallTicket ticket = existingTicket.get();
                    if (!ticket.isDeleted()) {
                        skipped++;
                        continue;
                    } else {
                        ticket.setDeleted(false);
                        ticket.setRoom(allocation.getRoom());
                        ticket.setRowNo(allocation.getRowNo());
                        ticket.setColNo(allocation.getColNo());
                        ticket.setSeatNumber(allocation.getSeatNumber());
                        ticket.setYear(allocation.getYear());
                        ticket.setSemester(allocation.getSemester());
                        ticket.setGeneratedAt(LocalDateTime.now());
                        ticket.setActive(true);
                        
                        if (ticket.getCreatedAt() == null) {
                            ticket.setCreatedAt(LocalDateTime.now());
                        }
                        if (ticket.getUpdatedAt() == null) {
                            ticket.setUpdatedAt(LocalDateTime.now());
                        }

                        Invigilator invigilator = getInvigilatorForRoom(
                                examId,
                                allocation.getRoom().getId(),
                                allocation.getYear(),
                                allocation.getSemester()
                        );
                        if (invigilator != null) {
                            ticket.setInvigilator(invigilator);
                        }
                        
                        hallTicketRepository.save(ticket);
                        generated++;
                        continue;
                    }
                }

                HallTicket hallTicket = new HallTicket();
                hallTicket.setStudent(student);
                hallTicket.setExam(exam);
                hallTicket.setRoom(allocation.getRoom());
                hallTicket.setRowNo(allocation.getRowNo());
                hallTicket.setColNo(allocation.getColNo());
                hallTicket.setSeatNumber(allocation.getSeatNumber());
                hallTicket.setYear(allocation.getYear());
                hallTicket.setSemester(allocation.getSemester());
                hallTicket.setGeneratedAt(LocalDateTime.now());
                hallTicket.setActive(true);
                hallTicket.setDeleted(false);

                LocalDateTime now = LocalDateTime.now();
                hallTicket.setCreatedAt(now);
                hallTicket.setUpdatedAt(now);

                Invigilator invigilator = getInvigilatorForRoom(
                        examId,
                        allocation.getRoom().getId(),
                        allocation.getYear(),
                        allocation.getSemester()
                );

                if (invigilator != null) {
                    hallTicket.setInvigilator(invigilator);
                }

                hallTicket.setHallTicketNumber(
                        generateHallTicketNumber(
                                exam,
                                student,
                                allocation.getYear(),
                                allocation.getSemester()
                        )
                );

                hallTicketRepository.save(hallTicket);
                generated++;
            }

            log.info("Generated {} hall tickets, skipped {}", generated, skipped);

            result.put("success", true);
            result.put("generated", generated);
            result.put("skipped", skipped);
            result.put("message", generated + " hall tickets generated successfully");
            
            return result;
            
        } catch (Exception e) {
            log.error("Error generating hall tickets: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate hall tickets: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> generateHallTicketForStudent(Long examId, Long studentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<HallTicket> existing = hallTicketRepository
                    .findByStudent_IdAndExam_IdAndDeletedFalse(studentId, examId);

            if (existing.isPresent()) {
                result.put("success", true);
                result.put("hallTicketId", existing.get().getId());
                result.put("message", "Hall ticket already exists");
                return result;
            }

            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new RuntimeException("Exam not found"));

            SeatAllocation allocation = seatAllocationRepository
                    .findByExam_IdAndStudent_IdAndDeletedFalse(examId, studentId)
                    .orElseThrow(() -> new RuntimeException("Seat allocation not found"));

            HallTicket hallTicket = new HallTicket();
            hallTicket.setStudent(allocation.getStudent());
            hallTicket.setExam(exam);
            hallTicket.setRoom(allocation.getRoom());
            hallTicket.setRowNo(allocation.getRowNo());
            hallTicket.setColNo(allocation.getColNo());
            hallTicket.setSeatNumber(allocation.getSeatNumber());
            hallTicket.setYear(allocation.getYear());
            hallTicket.setSemester(allocation.getSemester());
            hallTicket.setGeneratedAt(LocalDateTime.now());
            hallTicket.setActive(true);
            hallTicket.setDeleted(false);

            LocalDateTime now = LocalDateTime.now();
            hallTicket.setCreatedAt(now);
            hallTicket.setUpdatedAt(now);

            hallTicket.setHallTicketNumber(
                    generateHallTicketNumber(
                            exam,
                            allocation.getStudent(),
                            allocation.getYear(),
                            allocation.getSemester()
                    )
            );

            hallTicketRepository.save(hallTicket);

            result.put("success", true);
            result.put("hallTicketId", hallTicket.getId());
            result.put("message", "Hall ticket generated successfully");
            return result;
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    private String generateHallTicketNumber(
            Exam exam,
            Student student,
            Integer year,
            Integer semester
    ) {
        String subjectCode = exam.getSubjects().isEmpty()
                ? "GEN"
                : exam.getSubjects().get(0).getSubjectCode();

        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);

        return exam.getExamCode() + "/" +
                subjectCode + "/" +
                year + "-S" + semester + "/" +
                student.getRollNo() + "/" +
                timestamp;
    }

    private Invigilator getInvigilatorForRoom(
            Long examId,
            Long roomId,
            Integer year,
            Integer semester
    ) {
        return invigilatorAssignmentRepository
                .findByExam_IdAndRoom_IdAndYearAndSemesterAndDeletedFalse(
                        examId,
                        roomId,
                        year,
                        semester
                )
                .map(InvigilatorAssignment::getInvigilator)
                .orElse(null);
    }

    @Override
    public HallTicketDTO getHallTicketDetails(Long hallTicketId) {
        HallTicket hallTicket = hallTicketRepository.findByIdAndDeletedFalse(hallTicketId)
                .orElseThrow(() -> new RuntimeException("Hall ticket not found"));

        return getHallTicketDTO(hallTicket);
    }

    private HallTicketDTO getHallTicketDTO(HallTicket hallTicket) {
        Student student = hallTicket.getStudent();
        Exam exam = hallTicket.getExam();
        Room room = hallTicket.getRoom();

        Subject subject = exam.getSubjects().isEmpty()
                ? null
                : exam.getSubjects().get(0);

        HallTicketDTO dto = new HallTicketDTO();
        dto.setExamId(exam.getId());
        dto.setStudentName(student.getName());
        dto.setRollNo(student.getRollNo());
        dto.setHallTicketNo(hallTicket.getHallTicketNumber());
        dto.setDepartment(student.getDepartment().name());
        dto.setYear(student.getYear());
        dto.setSemester(hallTicket.getSemester());

        dto.setExamName(exam.getExamName());
        
        dto.setExamStartDate(exam.getStartDate());
        dto.setExamEndDate(exam.getEndDate());

        if (subject != null) {
            dto.setSubjectCode(subject.getSubjectCode());
            dto.setStartTime(subject.getStartTime());
            dto.setEndTime(subject.getEndTime());
            dto.setDuration(subject.getDuration());
            dto.setSubjectExamDate(subject.getExamDate());
        }

        if (room != null) {
            dto.setRoomCode(room.getRoomCode());
            dto.setRoomName(room.getRoomName());
            dto.setLocation(room.getLocation());
        }

        dto.setRowNo(hallTicket.getRowNo());
        dto.setColNo(hallTicket.getColNo());
        dto.setSeatNumber(hallTicket.getSeatNumber());

        dto.setStatus("ACTIVE");
        dto.setHallTicketsGenerated(true);

        if (hallTicket.getInvigilator() != null) {
            Invigilator inv = hallTicket.getInvigilator();
            dto.setInvigilatorName(inv.getName());
            dto.setInvigilatorEmployeeId(inv.getEmployeeId());
            dto.setInvigilatorPhone(inv.getPhone());
            dto.setInvigilatorDepartment(inv.getDepartment().name());
        }

        return dto;
    }

    @Override
    public byte[] downloadHallTicketPDF(Long hallTicketId) {
        try {
            return pdfGeneratorService.generateHallTicketPDF(
                    getHallTicketDetails(hallTicketId)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    @Override
    public List<HallTicketDTO> getHallTicketsByExam(Long examId) {
        return hallTicketRepository.findByExam_IdAndDeletedFalse(examId)
                .stream()
                .map(this::getHallTicketDTO)
                .toList();
    }

    @Override
    public Map<String, Object> deleteHallTicket(Long hallTicketId) {
        Map<String, Object> result = new HashMap<>();

        HallTicket ticket = hallTicketRepository.findByIdAndDeletedFalse(hallTicketId)
                .orElseThrow(() -> new RuntimeException("Hall ticket not found"));

        ticket.setDeleted(true);
        hallTicketRepository.save(ticket);

        result.put("success", true);
        result.put("message", "Hall ticket deleted successfully");
        return result;
    }

    @Override
    public Map<String, Object> assignInvigilator(Long hallTicketId, Long invigilatorId) {
        HallTicket ticket = hallTicketRepository.findByIdAndDeletedFalse(hallTicketId)
                .orElseThrow(() -> new RuntimeException("Hall ticket not found"));

        Invigilator invigilator = invigilatorRepository.findByIdAndDeletedFalse(invigilatorId)
                .orElseThrow(() -> new RuntimeException("Invigilator not found"));

        ticket.setInvigilator(invigilator);
        hallTicketRepository.save(ticket);

        return Map.of("success", true, "message", "Invigilator assigned successfully");
    }

    @Override
    @Transactional
    public Map<String, Object> assignInvigilatorToMultipleTickets(
            List<Long> hallTicketIds,
            Long invigilatorId
    ) {
        Map<String, Object> result = new HashMap<>();
        List<String> assigned = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        try {
            Invigilator invigilator = invigilatorRepository.findByIdAndDeletedFalse(invigilatorId)
                    .orElseThrow(() -> new RuntimeException("Invigilator not found"));

            for (Long hallTicketId : hallTicketIds) {
                try {
                    HallTicket hallTicket = hallTicketRepository.findByIdAndDeletedFalse(hallTicketId)
                            .orElseThrow(() ->
                                    new RuntimeException("Hall ticket not found: " + hallTicketId));

                    hallTicket.setInvigilator(invigilator);
                    hallTicketRepository.save(hallTicket);

                    assigned.add("Ticket " + hallTicketId + " - "
                            + hallTicket.getStudent().getRollNo());

                } catch (Exception e) {
                    failed.add("Ticket " + hallTicketId + ": " + e.getMessage());
                }
            }

            result.put("success", true);
            result.put("message", "Assigned invigilator to " + assigned.size() + " tickets");
            result.put("assigned", assigned);
            result.put("failed", failed);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }
    
    @Override
    public Map<String, Object> getHallTicketsWithoutInvigilator(Long examId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<HallTicket> tickets = hallTicketRepository.findByExam_IdAndDeletedFalse(examId);
            List<HallTicketDTO> ticketsWithoutInvigilator = new ArrayList<>();

            for (HallTicket ticket : tickets) {
                if (ticket.getInvigilator() == null) {
                    ticketsWithoutInvigilator.add(getHallTicketDTO(ticket));
                }
            }

            result.put("success", true);
            result.put("count", ticketsWithoutInvigilator.size());
            result.put("tickets", ticketsWithoutInvigilator);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> updateInvigilatorsFromAssignments(Long examId) {
        Map<String, Object> result = new HashMap<>();
        List<String> updated = new ArrayList<>();
        List<String> unchanged = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            List<HallTicket> tickets = hallTicketRepository.findByExam_IdAndDeletedFalse(examId);

            for (HallTicket ticket : tickets) {
                try {
                    Room room = ticket.getRoom();

                    if (room == null) {
                        unchanged.add("Ticket " + ticket.getId() + " has no room");
                        continue;
                    }

                    Optional<InvigilatorAssignment> assignment =
                            invigilatorAssignmentRepository
                                    .findByExam_IdAndRoom_IdAndYearAndSemesterAndDeletedFalse(
                                            examId,
                                            room.getId(),
                                            ticket.getYear(),
                                            ticket.getSemester()
                                    );

                    if (assignment.isPresent()) {
                        Invigilator assignedInvigilator =
                                assignment.get().getInvigilator();

                        if (ticket.getInvigilator() == null ||
                                !ticket.getInvigilator().getId()
                                        .equals(assignedInvigilator.getId())) {

                            ticket.setInvigilator(assignedInvigilator);
                            hallTicketRepository.save(ticket);

                            updated.add(
                                    "Ticket " + ticket.getId()
                                            + " -> "
                                            + assignedInvigilator.getName()
                            );
                        } else {
                            unchanged.add(
                                    "Ticket " + ticket.getId()
                                            + " already correct"
                            );
                        }
                    } else {
                        unchanged.add(
                                "No assignment for ticket " + ticket.getId()
                        );
                    }

                } catch (Exception e) {
                    errors.add(
                            "Ticket " + ticket.getId()
                                    + ": " + e.getMessage()
                    );
                }
            }

            result.put("success", true);
            result.put("message", "Updated " + updated.size() + " tickets");
            result.put("updated", updated);
            result.put("unchanged", unchanged);
            result.put("errors", errors);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }
}