package com.exam.seating.service;

import com.exam.seating.dto.InvigilatorDashboardDTO;
import com.exam.seating.dto.RoomDTO;
import java.util.List;

public interface InvigilatorDashboardService {

    InvigilatorDashboardDTO getDashboard(Long invigilatorId);

    List<RoomDTO> getRooms(Long invigilatorId);
}