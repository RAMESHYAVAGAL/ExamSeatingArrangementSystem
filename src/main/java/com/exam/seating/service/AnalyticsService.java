package com.exam.seating.service;

import com.exam.seating.dto.*;
import java.util.List;

public interface AnalyticsService {

    DashboardStatsDTO getDashboardStats();

    List<DepartmentChartDTO> getStudentsByDepartment();

    List<YearSemesterChartDTO> getStudentsByYearSemester();

    List<ExamReportDTO> getExamReport();
}