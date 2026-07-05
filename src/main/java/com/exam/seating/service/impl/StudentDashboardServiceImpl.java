package com.exam.seating.service.impl;

import com.exam.seating.dto.*;
import com.exam.seating.entity.*;
import com.exam.seating.repository.HallTicketRepository;
import com.exam.seating.repository.StudentRepository;
import com.exam.seating.security.CustomUserDetails;
import com.exam.seating.service.StudentDashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentDashboardServiceImpl implements StudentDashboardService {

    private final StudentRepository studentRepository;
    private final HallTicketRepository hallTicketRepository;
    
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    public StudentDashboardServiceImpl(
            StudentRepository studentRepository,
            HallTicketRepository hallTicketRepository
    ) {
        this.studentRepository = studentRepository;
        this.hallTicketRepository = hallTicketRepository;
    }

    public Student getCurrentStudent() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

            return studentRepository
                    .findByEmailIgnoreCaseAndDeletedFalse(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Student not found"));
        }

        throw new RuntimeException("Not authenticated");
    }

    @Override
    public StudentProfileDTO getStudentProfile(Long studentId) {
        Student student = studentRepository.findByIdAndDeletedFalse(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return StudentProfileDTO.builder()
                .id(student.getId())
                .name(student.getName())
                .rollNo(student.getRollNo())
                .email(student.getEmail())
                .phone(student.getPhone())
                .gender(student.getGender())
                .department(student.getDepartment())
                .year(student.getYear())
                .dateOfBirth(student.getDateOfBirth())
                .bloodGroup(student.getBloodGroup())
                .emergencyContact(student.getEmergencyContact())
                .address(student.getAddress())
                .currentSemester(student.getCurrentSemester())
                .enrollmentDate(student.getEnrollmentDate())
                .build();
    }

    @Override
    public StudentDashboardStatsDTO getDashboardStats(Long studentId) {
        List<HallTicket> tickets = hallTicketRepository.findByStudentUsingId(studentId);
        LocalDate today = LocalDate.now();

        long total = tickets.size();
        
        // FIXED: Use startDate and endDate for exam period
        long completed = tickets.stream()
                .filter(t -> t.getExam().getEndDate().isBefore(today))
                .count();

        long upcoming = tickets.stream()
                .filter(t -> t.getExam().getStartDate().isAfter(today))
                .count();

        long current = tickets.stream()
                .filter(t -> {
                    LocalDate start = t.getExam().getStartDate();
                    LocalDate end = t.getExam().getEndDate();
                    return (today.equals(start) || today.equals(end) || 
                            (today.isAfter(start) && today.isBefore(end)));
                })
                .count();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentDashboardStatsDTO dto = new StudentDashboardStatsDTO();
        dto.setStudentId(studentId);
        dto.setStudentName(student.getName());
        dto.setRollNo(student.getRollNo());
        dto.setTotalExams(total);
        dto.setHallTicketsCount(total);
        dto.setCompletedExamsCount(completed);
        dto.setUpcomingExamsCount(upcoming);
        dto.setCurrentExamsCount(current);
        dto.setHasExamsToday(current > 0);

        return dto;
    }

    @Override
    public Map<String, Object> getDashboardOverview(Long studentId) {
        Map<String, Object> map = new HashMap<>();
        map.put("stats", getDashboardStats(studentId));
        map.put("currentExams", getCurrentExams(studentId));
        map.put("upcomingExams", getUpcomingExams(studentId));
        map.put("allHallTickets", getAllHallTickets(studentId));
        return map;
    }

    @Override
    public List<HallTicketDTO> getAllHallTickets(Long studentId) {
        return hallTicketRepository.findByStudentUsingId(studentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HallTicketDTO getHallTicketById(Long hallTicketId) {
        HallTicket hallTicket = hallTicketRepository.findById(hallTicketId)
                .orElseThrow(() -> new RuntimeException("Hall ticket not found"));

        return convertToDTO(hallTicket);
    }

    @Override
    public HallTicketDTO getHallTicketByExam(Long studentId, Long examId) {
        HallTicket hallTicket = hallTicketRepository
                .findByStudentAndExam(studentId, examId)
                .orElseThrow(() -> new RuntimeException("Hall ticket not found"));

        return convertToDTO(hallTicket);
    }

    @Override
    public List<HallTicketDTO> getCurrentExams(Long studentId) {
        return getAllHallTickets(studentId).stream()
                .filter(t -> "current".equals(t.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<HallTicketDTO> getUpcomingExams(Long studentId) {
        return getAllHallTickets(studentId).stream()
                .filter(t -> "upcoming".equals(t.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<HallTicketDTO> getCompletedExams(Long studentId) {
        return getAllHallTickets(studentId).stream()
                .filter(t -> "completed".equals(t.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getHallTicketStatusCounts(Long studentId) {
        Map<String, Long> map = new HashMap<>();
        List<HallTicketDTO> tickets = getAllHallTickets(studentId);

        map.put("total", (long) tickets.size());
        map.put("completed", tickets.stream().filter(t -> "completed".equals(t.getStatus())).count());
        map.put("current", tickets.stream().filter(t -> "current".equals(t.getStatus())).count());
        map.put("upcoming", tickets.stream().filter(t -> "upcoming".equals(t.getStatus())).count());

        return map;
    }

    @Override
    public boolean hasExamsToday(Long studentId) {
        return !getCurrentExams(studentId).isEmpty();
    }

    @Override
    public List<HallTicketDTO> searchHallTickets(Long studentId, String searchTerm) {
        return getAllHallTickets(studentId);
    }

    @Override
    public List<HallTicketDTO> filterHallTicketsByDate(Long studentId, String startDate, String endDate) {
        return getAllHallTickets(studentId);
    }

    @Override
    public List<HallTicketDTO> filterHallTicketsByStatus(Long studentId, String status) {
        return getAllHallTickets(studentId).stream()
                .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
    }

    private HallTicketDTO convertToDTO(HallTicket ht) {
        Exam exam = ht.getExam();
        Student student = ht.getStudent();
        Room room = ht.getRoom();
        
        Subject subject = exam.getSubjects().isEmpty() ? null : exam.getSubjects().get(0);

        HallTicketDTO dto = new HallTicketDTO();

        dto.setExamId(exam.getId());
        dto.setStudentName(student.getName());
        dto.setRollNo(student.getRollNo());
        dto.setHallTicketNo(ht.getHallTicketNumber());
        dto.setDepartment(student.getDepartment().name());
        dto.setStudentEmail(student.getEmail());
        dto.setStudentPhone(student.getPhone());
        dto.setYear(student.getYear());
        dto.setSemester(ht.getSemester());

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
        
        dto.setRowNo(ht.getRowNo());
        dto.setColNo(ht.getColNo());
        dto.setSeatNumber(ht.getSeatNumber());

        if (ht.getInvigilator() != null) {
            Invigilator inv = ht.getInvigilator();
            dto.setInvigilatorName(inv.getName());
            dto.setInvigilatorEmployeeId(inv.getEmployeeId());
            dto.setInvigilatorPhone(inv.getPhone());
            dto.setInvigilatorDepartment(inv.getDepartment().name());
        }

        dto.setStatus(determineExamStatus(exam));
        dto.setHallTicketsGenerated(true);

        return dto;
    }

    private String determineExamStatus(Exam exam) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = exam.getStartDate();
        LocalDate endDate = exam.getEndDate();
        LocalTime now = LocalTime.now();
        
        if (today.isAfter(endDate)) {
            return "completed";
        }
        
        if (today.isBefore(startDate)) {
            return "upcoming";
        }
        
        if (!exam.getSubjects().isEmpty()) {
            Subject subject = exam.getSubjects().get(0);
            LocalTime startTime = subject.getStartTime();
            LocalTime endTime = subject.getEndTime();
            
            if (now.isBefore(startTime)) {
                return "upcoming";
            }
            
            if (now.isAfter(endTime)) {
                return "completed";
            }
            
            return "current";
        }
        
        return "current";
    }
}