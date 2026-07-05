package com.exam.seating.controller;

import com.exam.seating.dto.*;
import com.exam.seating.entity.AdminAccount;
import com.exam.seating.enums.Role;
import com.exam.seating.repository.AdminAccountRepository;
import com.exam.seating.service.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminRestController {

    private final AdminDashboardService dashboardService;
    private final AnalyticsService analyticsService;
    private final StudentService studentService;
    private final ExamService examService;
    private final SubjectService subjectService;
    private final RoomService roomService;
    private final InvigilatorService invigilatorService;
    private final SeatingGenerationService seatingService;
    private final ViewSeatingService viewSeatingService;
    private final AdminAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminRestController(
            AdminDashboardService dashboardService,
            AnalyticsService analyticsService,
            StudentService studentService,
            ExamService examService,
            SubjectService subjectService,
            RoomService roomService,
            InvigilatorService invigilatorService,
            SeatingGenerationService seatingService,
            ViewSeatingService viewSeatingService,
            PdfRenderService pdfRenderService,
            AdminAccountRepository userRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.dashboardService = dashboardService;
        this.analyticsService = analyticsService;
        this.studentService = studentService;
        this.examService = examService;
        this.subjectService = subjectService;
        this.roomService = roomService;
        this.invigilatorService = invigilatorService;
        this.seatingService = seatingService;
        this.viewSeatingService = viewSeatingService;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(
            @Valid @RequestBody AdminRegisterDTO dto,
            HttpSession session
    ) {
        Boolean allowed = (Boolean) session.getAttribute("ADMIN_REGISTER_ALLOWED");

        if (allowed == null || !allowed) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Admin registration not allowed");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Passwords do not match");
            return ResponseEntity.badRequest().body(response);
        }

        session.removeAttribute("ADMIN_REGISTER_ALLOWED");

        if (userRepo.existsByEmailIgnoreCaseAndDeletedFalse(dto.getEmail())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email already exists");
            return ResponseEntity.badRequest().body(response);
        }

        AdminAccount admin = new AdminAccount();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setRole(Role.ADMIN);
        admin.setFirstLogin(true);
        admin.setDeleted(false);

        userRepo.save(admin);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Admin registered successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/data")
    public AdminDashboardDTO dashboardData() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/analytics/stats")
    public DashboardStatsDTO dashboardStats() {
        return analyticsService.getDashboardStats();
    }

    @GetMapping("/analytics/students-by-department")
    public List<DepartmentChartDTO> studentsByDepartment() {
        return analyticsService.getStudentsByDepartment();
    }

    @GetMapping("/analytics/exam-report")
    public List<ExamReportDTO> examReport() {
        return analyticsService.getExamReport();
    }

    @GetMapping("/analytics/students-by-year-semester")
    public List<YearSemesterChartDTO> studentsByYearSemester() {
        return analyticsService.getStudentsByYearSemester();
    }

    // ========== STUDENT ENDPOINTS ==========
    
    @PostMapping("/students")
    public ResponseEntity<?> addStudent(@RequestBody StudentDTO dto) {
        try {
            StudentDTO savedStudent = studentService.saveStudent(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to add student");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents() {
        try {
            List<StudentDTO> students = studentService.getAllStudents();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to get students");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        try {
            StudentDTO student = studentService.getStudentById(id);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Student not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody StudentDTO dto) {
        try {
            StudentDTO updatedStudent = studentService.updateStudent(id, dto);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to update student");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Student deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete student");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<?> getDepartments() {
        try {
            List<?> departments = studentService.getAllDepartments();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to get departments");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== EXAM ENDPOINTS ==========
    
    @PostMapping("/exams")
    public ResponseEntity<?> addExam(@RequestBody ExamDTO dto) {
        try {
            ExamDTO savedExam = examService.saveExam(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedExam);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to add exam");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/exams")
    public ResponseEntity<?> getAllExams() {
        try {
            List<ExamDTO> exams = examService.getAllExams();
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to get exams");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/exams/{id}")
    public ResponseEntity<?> getExam(@PathVariable Long id) {
        try {
            ExamDTO exam = examService.getExamById(id);
            return ResponseEntity.ok(exam);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Exam not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/exams/{id}")
    public ResponseEntity<?> updateExam(@PathVariable Long id, @RequestBody ExamDTO dto) {
        try {
            ExamDTO updatedExam = examService.updateExam(id, dto);
            return ResponseEntity.ok(updatedExam);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to update exam");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/exams/{id}")
    public ResponseEntity<?> deleteExam(@PathVariable Long id) {
        try {
            examService.deleteExam(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exam deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete exam");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== SUBJECT ENDPOINTS ==========
    
    @PostMapping("/subjects")
    public ResponseEntity<?> createSubject(@RequestBody SubjectDTO dto) {
        try {
            SubjectDTO savedSubject = subjectService.saveSubject(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSubject);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to create subject");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/subjects/exam/{examId}")
    public ResponseEntity<?> getSubjectsByExam(@PathVariable Long examId) {
        try {
            List<SubjectDTO> subjects = subjectService.getSubjectsByExamId(examId);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to get subjects");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/subjects/{id}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long id) {
        try {
            SubjectDTO subject = subjectService.getSubjectById(id);
            return ResponseEntity.ok(subject);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Subject not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable Long id, @RequestBody SubjectDTO dto) {
        try {
            SubjectDTO updatedSubject = subjectService.updateSubject(id, dto);
            return ResponseEntity.ok(updatedSubject);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to update subject");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long id) {
        try {
            subjectService.deleteSubject(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Subject deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete subject");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== ROOM ENDPOINTS ==========
    
    @PostMapping("/rooms")
    public ResponseEntity<?> addRoom(@RequestBody RoomDTO dto) {
        try {
            RoomDTO savedRoom = roomService.saveRoom(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to add room");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> getAllRooms() {
        try {
            List<RoomDTO> rooms = roomService.getAllRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to get rooms");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        try {
            RoomDTO room = roomService.getRoomById(id);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Room not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody RoomDTO dto) {
        try {
            RoomDTO updatedRoom = roomService.updateRoom(id, dto);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to update room");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete room");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== INVIGILATOR ENDPOINTS ==========
    
    @PostMapping("/invigilators")
    public ResponseEntity<?> addInvigilator(@RequestBody InvigilatorDTO dto) {
        try {
            InvigilatorDTO savedInvigilator = invigilatorService.saveInvigilator(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedInvigilator);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to add invigilator");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/invigilators")
    public ResponseEntity<?> getAllInvigilators() {
        try {
            List<InvigilatorDTO> invigilators = invigilatorService.getAllInvigilators();
            return ResponseEntity.ok(invigilators);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to get invigilators");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/invigilators/{id}")
    public ResponseEntity<?> getInvigilator(@PathVariable Long id) {
        try {
            InvigilatorDTO invigilator = invigilatorService.getInvigilatorById(id);
            return ResponseEntity.ok(invigilator);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Invigilator not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/invigilators/{id}")
    public ResponseEntity<?> updateInvigilator(@PathVariable Long id, @RequestBody InvigilatorDTO dto) {
        try {
            InvigilatorDTO updatedInvigilator = invigilatorService.updateInvigilator(id, dto);
            return ResponseEntity.ok(updatedInvigilator);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to update invigilator");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/invigilators/{id}")
    public ResponseEntity<?> deleteInvigilator(@PathVariable Long id) {
        try {
            invigilatorService.deleteInvigilator(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invigilator deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete invigilator");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== SEATING ENDPOINTS - FIXED ==========
    
    @GetMapping("/seating")
    public ResponseEntity<?> getAllSeating() {
        try {
            List<SeatingGenerationDTO> seating = seatingService.getAllGeneratedSeating();
            return ResponseEntity.ok(seating);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to get seating arrangements");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/seating/{id}")
    public ResponseEntity<?> getSeatingById(@PathVariable Long id) {
        try {
            SeatingGenerationDTO seating = seatingService.getById(id);
            return ResponseEntity.ok(seating);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Seating arrangement not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/seating/generate")
    public ResponseEntity<?> generateSeating(@RequestBody SeatingGenerationDTO dto) {
        try {
            SeatingGenerationDTO result = seatingService.generateSeating(dto);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to generate seating arrangement");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to generate seating");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/seating/{id}")
    public ResponseEntity<?> updateSeating(@PathVariable Long id, @RequestBody SeatingGenerationDTO dto) {
        try {
            if (!seatingService.canEditSeating(id)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Cannot update seating. Hall tickets have already been generated.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            SeatingGenerationDTO result = seatingService.updateSeating(id, dto);
            
            if (result == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to update seating arrangement");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to update seating arrangement");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/seating/{id}")
    public ResponseEntity<?> deleteSeating(@PathVariable Long id) {
        try {
            seatingService.deleteSeating(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Seating arrangement deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete seating arrangement");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/seating/{id}/can-edit")
    public ResponseEntity<?> canEditSeating(@PathVariable Long id) {
        try {
            boolean canEdit = seatingService.canEditSeating(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("canEdit", canEdit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to check edit permission");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/view-seating/data")
    public ResponseEntity<?> viewSeating(
            @RequestParam Long examId,
            @RequestParam Long roomId,
            @RequestParam Integer year,
            @RequestParam Integer semester
    ) {
        try {
            ViewSeatingDTO result = viewSeatingService.getSeating(examId, roomId, year, semester);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to get seating data");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/view-seating/assign-invigilator")
    public ResponseEntity<?> assignInvigilator(@RequestBody InvigilatorAssignDTO dto) {
        try {
            viewSeatingService.assignInvigilator(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invigilator assigned successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to assign invigilator");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/view-seating/available-invigilators/{examId}")
    public ResponseEntity<?> getAvailableInvigilators(@PathVariable Long examId) {
        try {
            List<InvigilatorDTO> invigilators = viewSeatingService.getAvailableInvigilatorsForExam(examId);
            return ResponseEntity.ok(invigilators);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to get available invigilators");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/uploads/history")
    public ResponseEntity<?> getUploadHistory() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", new ArrayList<>());
        return ResponseEntity.ok(response);
    }
}