package com.exam.seating.service.impl;

import com.exam.seating.dto.*;
import com.exam.seating.repository.AnalyticsRepository;
import com.exam.seating.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        return DashboardStatsDTO.builder()
                .totalStudents(analyticsRepository.countStudents())
                .totalExams(analyticsRepository.countExams())
                .totalRooms(analyticsRepository.countRooms())
                .totalInvigilators(analyticsRepository.countInvigilators())
                .build();
    }

    @Override
    public List<DepartmentChartDTO> getStudentsByDepartment() {
        return analyticsRepository.studentsByDepartment();
    }

    @Override
    public List<YearSemesterChartDTO> getStudentsByYearSemester() {
        return analyticsRepository.studentsByYearSemester();
    }

    @Override
    public List<ExamReportDTO> getExamReport() {
        return analyticsRepository.examReport();
    }
}