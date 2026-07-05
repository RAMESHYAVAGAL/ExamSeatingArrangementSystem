package com.exam.seating.service.impl;

import com.exam.seating.dto.AssignedExamDTO;
import com.exam.seating.dto.ChangePasswordDTO;
import com.exam.seating.dto.InvigilatorDTO;
import com.exam.seating.entity.Exam;
import com.exam.seating.entity.Invigilator;
import com.exam.seating.entity.InvigilatorAssignment;
import com.exam.seating.entity.SeatAllocation;
import com.exam.seating.entity.Subject;
import com.exam.seating.exception.ResourceNotFoundException;
import com.exam.seating.repository.InvigilatorAssignmentRepository;
import com.exam.seating.repository.InvigilatorRepository;
import com.exam.seating.repository.SeatAllocationRepository;
import com.exam.seating.service.InvigilatorService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvigilatorServiceImpl implements InvigilatorService {

    private final InvigilatorRepository invigilatorRepository;
    private final InvigilatorAssignmentRepository invigilatorAssignmentRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final PasswordEncoder passwordEncoder;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("hh:mm a");
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public InvigilatorServiceImpl(
            InvigilatorRepository invigilatorRepository,
            InvigilatorAssignmentRepository invigilatorAssignmentRepository,
            SeatAllocationRepository seatAllocationRepository,
            PasswordEncoder passwordEncoder) {

        this.invigilatorRepository = invigilatorRepository;
        this.invigilatorAssignmentRepository = invigilatorAssignmentRepository;
        this.seatAllocationRepository = seatAllocationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public InvigilatorDTO saveInvigilator(InvigilatorDTO dto) {
        try {
            if (invigilatorRepository.existsByEmployeeId(dto.getEmployeeId())) {
                throw new RuntimeException("Employee ID '" + dto.getEmployeeId() + "' already exists. Please use a different employee ID.");
            }

            if (invigilatorRepository.existsByEmailIgnoreCase(dto.getEmail())) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' already exists. Please use a different email.");
            }

            if (invigilatorRepository.existsByPhone(dto.getPhone())) {
                throw new RuntimeException("Phone number '" + dto.getPhone() + "' already exists. Please use a different phone number.");
            }

            Invigilator inv = new Invigilator();
            inv.setEmployeeId(dto.getEmployeeId());
            inv.setName(dto.getName());
            inv.setPhone(dto.getPhone());
            inv.setEmail(dto.getEmail());
            inv.setDepartment(dto.getDepartment());

            String rawPassword = dto.getEmployeeId() + "@123";
            inv.setPassword(passwordEncoder.encode(rawPassword));
            inv.setFirstLogin(true);
            inv.setDeleted(false);

            return mapToDTO(invigilatorRepository.save(inv));
            
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message.contains("UKdxbg3g06gmwcoq2qgyb9s09sh") || message.contains("employee_id")) {
                throw new RuntimeException("Employee ID '" + dto.getEmployeeId() + "' already exists. Please use a different employee ID.");
            } else if (message.contains("UKbyeuhreyj6mjmuv0ilspyohwj") || message.contains("email")) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' already exists. Please use a different email.");
            } else if (message.contains("phone")) {
                throw new RuntimeException("Phone number '" + dto.getPhone() + "' already exists. Please use a different phone number.");
            }
            throw new RuntimeException("Failed to save invigilator: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save invigilator: " + e.getMessage());
        }
    }

    @Override
    public InvigilatorDTO updateInvigilator(Long id, InvigilatorDTO dto) {
        try {
            Invigilator inv = invigilatorRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Invigilator", id));

            if (!inv.getEmployeeId().equals(dto.getEmployeeId()) &&
                invigilatorRepository.existsByEmployeeId(dto.getEmployeeId())) {
                throw new RuntimeException("Employee ID '" + dto.getEmployeeId() + "' already exists. Please use a different employee ID.");
            }

            if (!inv.getEmail().equalsIgnoreCase(dto.getEmail()) &&
                invigilatorRepository.existsByEmailIgnoreCase(dto.getEmail())) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' already exists. Please use a different email.");
            }

            if (!inv.getPhone().equals(dto.getPhone()) &&
                invigilatorRepository.existsByPhone(dto.getPhone())) {
                throw new RuntimeException("Phone number '" + dto.getPhone() + "' already exists. Please use a different phone number.");
            }

            inv.setEmployeeId(dto.getEmployeeId());
            inv.setName(dto.getName());
            inv.setPhone(dto.getPhone());
            inv.setEmail(dto.getEmail());
            inv.setDepartment(dto.getDepartment());

            return mapToDTO(invigilatorRepository.save(inv));
            
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message.contains("UKdxbg3g06gmwcoq2qgyb9s09sh") || message.contains("employee_id")) {
                throw new RuntimeException("Employee ID '" + dto.getEmployeeId() + "' already exists. Please use a different employee ID.");
            } else if (message.contains("UKbyeuhreyj6mjmuv0ilspyohwj") || message.contains("email")) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' already exists. Please use a different email.");
            } else if (message.contains("phone")) {
                throw new RuntimeException("Phone number '" + dto.getPhone() + "' already exists. Please use a different phone number.");
            }
            throw new RuntimeException("Failed to update invigilator: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update invigilator: " + e.getMessage());
        }
    }

    @Override
    public List<InvigilatorDTO> getAllInvigilators() {
        return invigilatorRepository.findByDeletedFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InvigilatorDTO getInvigilatorById(Long id) {
        Invigilator inv = invigilatorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invigilator", id));
        return mapToDTO(inv);
    }

    @Override
    public InvigilatorDTO getByEmail(String email) {
        Invigilator inv = invigilatorRepository
                .findByEmailIgnoreCaseAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Invigilator", "email", email));
        return mapToDTO(inv);
    }

    @Override
    public void deleteInvigilator(Long id) {
        Invigilator inv = invigilatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invigilator", id));
        inv.setDeleted(true);
        invigilatorRepository.save(inv);
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        Invigilator inv = invigilatorRepository.findById(dto.getInvigilatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Invigilator", dto.getInvigilatorId()));

        if (dto.getCurrentPassword() != null &&
                !dto.getCurrentPassword().isBlank()) {
            if (!passwordEncoder.matches(dto.getCurrentPassword(), inv.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        inv.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        inv.setFirstLogin(false);
        invigilatorRepository.save(inv);
    }

    @Override
    public List<AssignedExamDTO> getAssignedExams(Long invigilatorId) {
        List<InvigilatorAssignment> assignments =
                invigilatorAssignmentRepository
                        .findByInvigilator_IdAndDeletedFalse(invigilatorId);

        List<AssignedExamDTO> result = new ArrayList<>();

        for (InvigilatorAssignment assignment : assignments) {
            AssignedExamDTO dto = new AssignedExamDTO();

            dto.setExamId(assignment.getExam().getId());
            dto.setRoomId(assignment.getRoom().getId());
            dto.setYear(assignment.getYear());
            dto.setSemester(assignment.getSemester());
            dto.setExamName(assignment.getExam().getExamName());

            dto.setRoomName(
                    assignment.getRoom().getRoomName()
                            + " (" + assignment.getRoom().getRoomCode() + ")"
            );

            Exam exam = assignment.getExam();
            LocalDate startDate = exam.getStartDate();
            LocalDate endDate = exam.getEndDate();
            
            if (startDate.equals(endDate)) {
                dto.setExamDate(startDate);
                dto.setExamDateDisplay(startDate.format(dateFormatter));
            } else {
                dto.setExamDate(startDate);
                dto.setExamDateDisplay(startDate.format(dateFormatter) + " to " + 
                                      endDate.format(dateFormatter));
            }

            if (!assignment.getExam().getSubjects().isEmpty()) {
                Subject subject = assignment.getExam().getSubjects().get(0);
                String start = subject.getStartTime().format(timeFormatter);
                String end = subject.getEndTime().format(timeFormatter);
                dto.setExamTime(start + " - " + end);
            } else {
                dto.setExamTime("09:00 AM - 12:00 PM");
            }

            List<SeatAllocation> seats =
                    seatAllocationRepository
                            .findByExam_IdAndRoom_IdAndYearAndSemesterAndDeletedFalse(
                                    assignment.getExam().getId(),
                                    assignment.getRoom().getId(),
                                    assignment.getYear(),
                                    assignment.getSemester()
                            );

            dto.setCapacity(seats.size() + " students");
            result.add(dto);
        }

        return result;
    }

    @Override
    public boolean existsByEmail(String email) {
        return invigilatorRepository.existsByEmailIgnoreCaseAndDeletedFalse(email);
    }

    @Override
    public boolean existsByEmployeeId(String employeeId) {
        return invigilatorRepository.existsByEmployeeIdAndDeletedFalse(employeeId);
    }

    private InvigilatorDTO mapToDTO(Invigilator inv) {
        InvigilatorDTO dto = new InvigilatorDTO();
        dto.setId(inv.getId());
        dto.setEmployeeId(inv.getEmployeeId());
        dto.setName(inv.getName());
        dto.setPhone(inv.getPhone());
        dto.setEmail(inv.getEmail());
        dto.setDepartment(inv.getDepartment());
        return dto;
    }
}