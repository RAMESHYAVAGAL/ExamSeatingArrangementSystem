package com.exam.seating.service.impl;

import com.exam.seating.dto.*;
import com.exam.seating.entity.*;
import com.exam.seating.repository.*;
import com.exam.seating.service.InvigilatorDashboardService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvigilatorDashboardServiceImpl implements InvigilatorDashboardService {

    private final InvigilatorRepository invigilatorRepository;
    private final InvigilatorAssignmentRepository invigilatorAssignmentRepository;
    private final SeatAllocationRepository seatAllocationRepository;

    public InvigilatorDashboardServiceImpl(
            InvigilatorRepository invigilatorRepository,
            InvigilatorAssignmentRepository invigilatorAssignmentRepository,
            SeatAllocationRepository seatAllocationRepository
    ) {
        this.invigilatorRepository = invigilatorRepository;
        this.invigilatorAssignmentRepository = invigilatorAssignmentRepository;
        this.seatAllocationRepository = seatAllocationRepository;
    }

    @Override
    @Transactional
    public InvigilatorDashboardDTO getDashboard(Long invigilatorId) {

        Invigilator invigilator = invigilatorRepository.findById(invigilatorId)
                .orElseThrow(() -> new RuntimeException("Invigilator not found"));

        List<InvigilatorAssignment> assignments =
                invigilatorAssignmentRepository
                        .findByInvigilator_IdAndDeletedFalse(invigilatorId);

        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);

        List<InvigilatorAssignment> todayAssignments = assignments.stream()
                .filter(a -> a.getExam() != null)
                .filter(a -> {
                    LocalDate startDate = a.getExam().getStartDate();
                    LocalDate endDate = a.getExam().getEndDate();
                    return (startDate.equals(today) || endDate.equals(today) || 
                            (today.isAfter(startDate) && today.isBefore(endDate)));
                })
                .collect(Collectors.toList());

        List<InvigilatorAssignment> upcomingAssignments = assignments.stream()
                .filter(a -> a.getExam() != null)
                .filter(a -> {
                    LocalDate startDate = a.getExam().getStartDate();
                    return startDate.isAfter(today) && !startDate.isAfter(weekLater);
                })
                .collect(Collectors.toList());

        long totalStudents = calculateTotalStudents(todayAssignments);

        InvigilatorDashboardDTO dto = new InvigilatorDashboardDTO();
        dto.setInvigilatorName(invigilator.getName());
        dto.setAssignedExams(todayAssignments.size());
        dto.setTodayExams(todayAssignments.size());
        dto.setActiveRooms(calculateActiveRooms(todayAssignments));
        dto.setTotalStudents(totalStudents);
        dto.setAvgDuration(calculateAverageDuration(todayAssignments));
        dto.setTodayExamsList(prepareExamItems(todayAssignments));
        dto.setUpcomingExamsList(prepareExamItems(upcomingAssignments));

        return dto;
    }

    @Override
    @Transactional
    public List<RoomDTO> getRooms(Long invigilatorId) {

        List<InvigilatorAssignment> assignments =
                invigilatorAssignmentRepository
                        .findByInvigilator_IdAndDeletedFalse(invigilatorId);

        List<RoomDTO> rooms = new ArrayList<>();

        for (InvigilatorAssignment assignment : assignments) {

            Exam exam = assignment.getExam();
            Room room = assignment.getRoom();
            Subject subject = exam.getSubjects().isEmpty()
                    ? null
                    : exam.getSubjects().get(0);

            List<SeatAllocation> allocations =
                    seatAllocationRepository.findByExam_IdAndRoom_IdAndDeletedFalse(
                            exam.getId(),
                            room.getId()
                    );

            int studentCount = allocations.size();

            RoomDTO dto = new RoomDTO();
            dto.setRoomId(room.getId());
            dto.setExamId(exam.getId());

            dto.setRoomCode(room.getRoomCode());
            dto.setRoomName(room.getRoomName());
            dto.setCapacity(room.getCapacity());
            dto.setRows(room.getRows());
            dto.setCols(room.getCols());
            dto.setLocation(room.getLocation());

            dto.setExamName(exam.getExamName());
            String dateRange = exam.getStartDate().toString();
            if (!exam.getStartDate().equals(exam.getEndDate())) {
                dateRange = exam.getStartDate() + " to " + exam.getEndDate();
            }
            dto.setExamDate(dateRange);

            LocalTime start = subject != null ? subject.getStartTime() : LocalTime.of(9,0);
            LocalTime end = subject != null ? subject.getEndTime() : LocalTime.of(12,0);

            dto.setExamTime(start + " - " + end);

            int duration = subject != null ? subject.getDuration() : 180;
            dto.setDuration(duration + " mins");

            dto.setStudentCount(studentCount);

            int capacityPercentage = room.getCapacity() != null && room.getCapacity() > 0
                    ? (studentCount * 100) / room.getCapacity()
                    : 0;

            dto.setCapacityPercentage(capacityPercentage);
            dto.setStatus(determineStatus(exam, subject));

            rooms.add(dto);
        }

        return rooms;
    }

    private long calculateTotalStudents(List<InvigilatorAssignment> assignments) {
        long total = 0;

        for (InvigilatorAssignment assignment : assignments) {
            total += seatAllocationRepository
                    .findByExam_IdAndRoom_IdAndDeletedFalse(
                            assignment.getExam().getId(),
                            assignment.getRoom().getId()
                    ).size();
        }

        return total;
    }

    private long calculateActiveRooms(List<InvigilatorAssignment> assignments) {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        return assignments.stream()
                .filter(a -> {
                    Exam exam = a.getExam();
                    LocalDate startDate = exam.getStartDate();
                    LocalDate endDate = exam.getEndDate();
                    boolean isTodayInRange = (startDate.equals(today) || endDate.equals(today) || 
                            (today.isAfter(startDate) && today.isBefore(endDate)));
                    
                    if (!isTodayInRange) return false;
                    
                    Subject subject = exam.getSubjects().isEmpty()
                            ? null
                            : exam.getSubjects().get(0);

                    if (subject == null) return false;

                    return !now.isBefore(subject.getStartTime())
                            && !now.isAfter(subject.getEndTime());
                })
                .count();
    }

    private String calculateAverageDuration(List<InvigilatorAssignment> assignments) {
        if (assignments.isEmpty()) return "0h";

        double avg = assignments.stream()
                .map(a -> a.getExam().getSubjects().isEmpty()
                        ? 180
                        : a.getExam().getSubjects().get(0).getDuration())
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        int h = (int) avg / 60;
        int m = (int) avg % 60;

        return h > 0 ? h + "h " + m + "m" : m + "m";
    }

    private List<ExamItemDTO> prepareExamItems(List<InvigilatorAssignment> assignments) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        List<ExamItemDTO> list = new ArrayList<>();

        for (InvigilatorAssignment assignment : assignments) {
            Exam exam = assignment.getExam();
            Room room = assignment.getRoom();

            Subject subject = exam.getSubjects().isEmpty()
                    ? null
                    : exam.getSubjects().get(0);

            LocalTime start = subject != null ? subject.getStartTime() : LocalTime.of(9,0);
            LocalTime end = subject != null ? subject.getEndTime() : LocalTime.of(12,0);

            ExamItemDTO dto = new ExamItemDTO();
            dto.setExamId(exam.getId());
            dto.setRoomId(room.getId());
            dto.setExamName(exam.getExamName());
            
            String dateDisplay = exam.getStartDate().format(dateFormatter);
            if (!exam.getStartDate().equals(exam.getEndDate())) {
                dateDisplay = exam.getStartDate().format(dateFormatter) + " - " + 
                             exam.getEndDate().format(dateFormatter);
            }
            dto.setExamDate(dateDisplay);
            
            dto.setExamTime(start.format(timeFormatter) + " - " + end.format(timeFormatter));
            dto.setRoomName(room.getRoomCode() + " - " + room.getRoomName());

            list.add(dto);
        }

        return list;
    }

    private String determineStatus(Exam exam, Subject subject) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LocalDate startDate = exam.getStartDate();
        LocalDate endDate = exam.getEndDate();

        if (today.isBefore(startDate)) return "scheduled";
        
        if (today.isAfter(endDate)) return "completed";

        LocalTime start = subject != null ? subject.getStartTime() : LocalTime.of(9,0);
        LocalTime end = subject != null ? subject.getEndTime() : LocalTime.of(12,0);

        if (now.isBefore(start)) return "scheduled";
        if (now.isAfter(end)) return "completed";

        return "ongoing";
    }
}