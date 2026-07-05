package com.exam.seating.service;

import com.exam.seating.dto.*;
import java.util.List;

public interface InvigilatorService {

    InvigilatorDTO saveInvigilator(InvigilatorDTO dto);

    InvigilatorDTO updateInvigilator(Long id, InvigilatorDTO dto);

    List<InvigilatorDTO> getAllInvigilators();

    InvigilatorDTO getInvigilatorById(Long id);

    InvigilatorDTO getByEmail(String email);

    void deleteInvigilator(Long id);

    void changePassword(ChangePasswordDTO dto);

    List<AssignedExamDTO> getAssignedExams(Long invigilatorId);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);
}