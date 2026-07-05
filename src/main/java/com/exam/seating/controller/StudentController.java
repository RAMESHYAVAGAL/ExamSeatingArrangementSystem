package com.exam.seating.controller;

import com.exam.seating.dto.*;
import com.exam.seating.entity.Student;
import com.exam.seating.repository.StudentRepository;
import com.exam.seating.service.HallTicketPdfService;
import com.exam.seating.service.StudentDashboardService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentDashboardService studentDashboardService;
    private final StudentRepository studentRepository;
    private final HallTicketPdfService hallTicketPdfService;

    public StudentController(
            StudentDashboardService studentDashboardService,
            StudentRepository studentRepository,
            HallTicketPdfService hallTicketPdfService
    ) {
        this.studentDashboardService = studentDashboardService;
        this.studentRepository = studentRepository;
        this.hallTicketPdfService = hallTicketPdfService;
    }

    private Student getCurrentStudent() {
        return studentDashboardService.getCurrentStudent();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData() {
        try {
            Student student = getCurrentStudent();
            return ResponseEntity.ok(
                    studentDashboardService.getDashboardOverview(student.getId())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfileData() {
        try {
            Student student = getCurrentStudent();
            return ResponseEntity.ok(
                    studentDashboardService.getStudentProfile(student.getId())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody StudentProfileDTO profileDTO) {
        try {
            Student student = getCurrentStudent();

            Optional.ofNullable(profileDTO.getName()).ifPresent(student::setName);
            Optional.ofNullable(profileDTO.getEmail()).ifPresent(student::setEmail);
            Optional.ofNullable(profileDTO.getPhone()).ifPresent(student::setPhone);
            Optional.ofNullable(profileDTO.getGender()).ifPresent(student::setGender);
            Optional.ofNullable(profileDTO.getDateOfBirth()).ifPresent(student::setDateOfBirth);
            Optional.ofNullable(profileDTO.getBloodGroup()).ifPresent(student::setBloodGroup);
            Optional.ofNullable(profileDTO.getEmergencyContact()).ifPresent(student::setEmergencyContact);
            Optional.ofNullable(profileDTO.getAddress()).ifPresent(student::setAddress);
            Optional.ofNullable(profileDTO.getDepartment()).ifPresent(student::setDepartment);
            Optional.ofNullable(profileDTO.getYear()).ifPresent(student::setYear);
            Optional.ofNullable(profileDTO.getCurrentSemester()).ifPresent(student::setCurrentSemester);
            Optional.ofNullable(profileDTO.getEnrollmentDate()).ifPresent(student::setEnrollmentDate);

            studentRepository.save(student);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Profile updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/hall-tickets")
    public ResponseEntity<?> getAllHallTickets() {
        try {
            Student student = getCurrentStudent();
            List<HallTicketDTO> tickets =
                    studentDashboardService.getAllHallTickets(student.getId());

            return ResponseEntity.ok(Map.of(
                    "studentId", student.getId(),
                    "studentName", student.getName(),
                    "rollNo", student.getRollNo(),
                    "totalTickets", tickets.size(),
                    "hallTickets", tickets
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/hallticket/{examId}")
    public ResponseEntity<?> getHallTicket(@PathVariable Long examId) {
        try {
            Student student = getCurrentStudent();
            HallTicketDTO dto =
                    studentDashboardService.getHallTicketByExam(student.getId(), examId);

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/hallticket/pdf/{examId}")
    public ResponseEntity<InputStreamResource> downloadHallTicketPdf(
            @PathVariable Long examId) {

        try {
            Student student = getCurrentStudent();

            HallTicketDTO hallTicket =
                    studentDashboardService.getHallTicketByExam(student.getId(), examId);

            ByteArrayInputStream pdfStream =
                    hallTicketPdfService.generateHallTicketPdf(hallTicket);

            String filename = String.format(
                    "HallTicket_%s_%s.pdf",
                    hallTicket.getRollNo().replaceAll("[^a-zA-Z0-9]", "_"),
                    hallTicket.getExamName().replaceAll("[^a-zA-Z0-9]", "_")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition",
                    "attachment; filename=" + filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(pdfStream));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            Student student = getCurrentStudent();

            StudentDashboardStatsDTO stats =
                    studentDashboardService.getDashboardStats(student.getId());

            Map<String, Long> statusCounts =
                    studentDashboardService.getHallTicketStatusCounts(student.getId());

            return ResponseEntity.ok(Map.of(
                    "stats", stats,
                    "statusCounts", statusCounts,
                    "hasExamsToday",
                    studentDashboardService.hasExamsToday(student.getId())
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    private String getCurrentAcademicYear() {
        int currentYear = LocalDate.now().getYear();
        return currentYear + "-" + (currentYear + 1);
    }
}