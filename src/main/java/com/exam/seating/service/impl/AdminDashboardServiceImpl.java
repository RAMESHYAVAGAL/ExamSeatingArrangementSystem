package com.exam.seating.service.impl;

import com.exam.seating.dto.AdminDashboardDTO;
import com.exam.seating.repository.AdminDashboardRepository;
import com.exam.seating.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardRepository repository;

    @Override
    public AdminDashboardDTO getDashboardData() {
        LocalDate today = LocalDate.now();
        LocalDate next7Days = today.plusDays(7);
        LocalDate yesterday = today.minusDays(1);

        long totalStudents = repository.countStudents();
        long totalExams = repository.countExams();
        long totalRooms = repository.countRooms();
        long totalInvigilators = repository.countInvigilators();

        long todayExams = repository.countTodayExams(today);
        long upcomingExams = repository.countUpcomingExams(today, next7Days);
        long pendingExams = repository.countPendingExams(today);
        long generatedExams = repository.countGeneratedExams(today);

        long studentsAssigned = repository.countStudentsAssigned(today);
        long totalCapacity = repository.totalRoomCapacity();
        long usedCapacity = repository.usedRoomCapacity(today);
        long freeInvigilators = repository.countFreeInvigilators(today);

        int roomUtilization = calculatePercentage(usedCapacity, totalCapacity);
        int generatedPercentage = calculatePercentage(generatedExams, todayExams);
        int pendingPercentage = todayExams > 0 ? 100 - generatedPercentage : 0;

        boolean seatingGenerated = todayExams > 0 && pendingExams == 0;

        long yesterdayExams = repository.countTodayExams(yesterday);
        long todayChange = todayExams - yesterdayExams;

        return AdminDashboardDTO.builder()
                .totalStudents(totalStudents)
                .totalExams(totalExams)
                .totalRooms(totalRooms)
                .totalInvigilators(totalInvigilators)
                .todayExams(todayExams)
                .todayChange(todayChange)
                .upcomingExams(upcomingExams)
                .seatingGenerated(seatingGenerated)
                .studentsAssigned(studentsAssigned)
                .roomUtilization(roomUtilization)
                .freeInvigilators(freeInvigilators)
                .pendingExams(pendingExams)
                .pendingExamNames(repository.findPendingExamNames(today))
                .generatedPercentage(generatedPercentage)
                .pendingPercentage(pendingPercentage)
                .build();
    }

    private int calculatePercentage(long value, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) ((value * 100) / total);
    }
}