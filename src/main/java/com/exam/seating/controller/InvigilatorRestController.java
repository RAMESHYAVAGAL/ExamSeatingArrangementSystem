package com.exam.seating.controller;

import com.exam.seating.dto.*;
import com.exam.seating.security.CustomUserDetails;
import com.exam.seating.service.InvigilatorDashboardService;
import com.exam.seating.service.InvigilatorService;
import com.exam.seating.service.ViewSeatingService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invigilator")
@CrossOrigin(origins = "*")
public class InvigilatorRestController {

    private final InvigilatorDashboardService dashboardService;
    private final InvigilatorService invigilatorService;
    private final ViewSeatingService viewSeatingService;

    public InvigilatorRestController(
            InvigilatorDashboardService dashboardService,
            InvigilatorService invigilatorService,
            ViewSeatingService viewSeatingService
    ) {
        this.dashboardService = dashboardService;
        this.invigilatorService = invigilatorService;
        this.viewSeatingService = viewSeatingService;
    }

    @GetMapping("/dashboard")
    public InvigilatorDashboardDTO getDashboard() {
        return dashboardService.getDashboard(getLoggedInvigilatorId());
    }

    @GetMapping("/rooms")
    public List<RoomDTO> getRooms() {
        return dashboardService.getRooms(getLoggedInvigilatorId());
    }

    @GetMapping("/profile")
    public InvigilatorDTO getProfile() {
        return invigilatorService.getInvigilatorById(getLoggedInvigilatorId());
    }

    @GetMapping("/assigned-exams")
    public List<AssignedExamDTO> getAssignedExams() {
        return invigilatorService.getAssignedExams(getLoggedInvigilatorId());
    }

    @GetMapping("/view-seating/data")
    public ResponseEntity<?> viewSeating(
            @RequestParam Long examId,
            @RequestParam Long roomId,
            @RequestParam Integer year,
            @RequestParam Integer semester) {
        try {
            ViewSeatingDTO dto =
                    viewSeatingService.getSeating(examId, roomId, year, semester);

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    private Long getLoggedInvigilatorId() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails user)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"INVIGILATOR".equals(user.getRole())) {
            throw new RuntimeException("Access denied");
        }

        return user.getUserId();
    }
}