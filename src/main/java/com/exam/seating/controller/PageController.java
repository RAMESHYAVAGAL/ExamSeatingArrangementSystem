package com.exam.seating.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/invigilator-dashboard")
    public String invigilatorDashboard() {
        return "invigilator-dashboard";
    }

    @GetMapping("/students")
    public String students() {
        return "students";
    }

    @GetMapping("/exams")
    public String exams() {
        return "exams";
    }

    @GetMapping("/rooms")
    public String rooms() {
        return "rooms";
    }

    @GetMapping("/invigilators")
    public String invigilators() {
        return "invigilators";
    }

    @GetMapping("/generate-seating")
    public String generateSeating() {
        return "generate-seating";
    }

    @GetMapping("/view-seating")
    public String viewSeating() {
        return "view-seating";
    }

    @GetMapping("/charts")
    public String charts() {
        return "charts";
    }

    @GetMapping("/bulk-upload")
    public String bulkUpload() {
        return "bulk-upload";
    }

    @GetMapping("/invigilator-profile")
    public String invigilatorProfile() {
        return "invigilator-profile";
    }

    @GetMapping("/assigned-exams")
    public String assignedExams() {
        return "assigned-exams";
    }

    @GetMapping("/admin-login")
    public String adminLogin() {
        return "index";
    }
    
    @GetMapping("/student-dashboard")
    public String studentDashboard() {
        return "student-dashboard";
    }
    
    @GetMapping("/student-profile")
    public String studentProfile() {
        return "student-profile";
    }

    @GetMapping("/hall-tickets")
    public String hallTickets() {
        return "hall-tickets";
    }
}