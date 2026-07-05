package com.exam.seating.mapper;

import com.exam.seating.dto.AdminDashboardDTO;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper {

    public AdminDashboardDTO buildAdminDashboard(
            long students,
            long exams,
            long rooms,
            long invigilators
    ) {
        return AdminDashboardDTO.builder()
                .totalStudents(students)
                .totalExams(exams)
                .totalRooms(rooms)
                .totalInvigilators(invigilators)
                .build();
    }
}