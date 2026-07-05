package com.exam.seating.service;

import com.exam.seating.dto.BulkUploadResponseDTO;
import com.exam.seating.entity.*;
import com.exam.seating.enums.Department;
import com.exam.seating.enums.Gender;
import com.exam.seating.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BulkUploadService {

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final InvigilatorRepository invigilatorRepository;
    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DEFAULT_PASSWORD = "password123";

    @Transactional
    public BulkUploadResponseDTO uploadStudents(MultipartFile file, String department, Integer year) {
        BulkUploadResponseDTO response = BulkUploadResponseDTO.builder()
                .success(true)
                .errors(new ArrayList<>())
                .summaries(new ArrayList<>())
                .build();

        List<Student> studentsToSave = new ArrayList<>();
        List<BulkUploadResponseDTO.UploadError> errors = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new RuntimeException("No sheet found in the Excel file");
            }

            boolean isFirstRow = true;
            int rowNum = 0;

            for (Row row : sheet) {
                rowNum++;
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                if (row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                    continue;
                }

                try {
                    Student student = new Student();

                    String rollNo = getStringValue(row.getCell(0));
                    if (rollNo == null || rollNo.isEmpty()) {
                        throw new RuntimeException("Roll No is required");
                    }
                    student.setRollNo(rollNo);

                    String name = getStringValue(row.getCell(1));
                    if (name == null || name.isEmpty()) {
                        throw new RuntimeException("Name is required");
                    }
                    student.setName(name);

                    String email = getStringValue(row.getCell(2));
                    if (email == null || email.isEmpty()) {
                        throw new RuntimeException("Email is required");
                    }
                    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        throw new RuntimeException("Invalid email format: " + email);
                    }
                    student.setEmail(email);

                    String phone = getStringValue(row.getCell(3));
                    if (phone == null || phone.isEmpty()) {
                        throw new RuntimeException("Phone is required");
                    }
                    if (!phone.matches("^[6-9]\\d{9}$")) {
                        throw new RuntimeException("Invalid phone number: " + phone + " (must be 10 digits starting with 6,7,8,9)");
                    }
                    student.setPhone(phone);

                    String deptStr = getStringValue(row.getCell(4));
                    if (department != null && !department.isEmpty()) {
                        student.setDepartment(Department.valueOf(department));
                    } else if (deptStr != null && !deptStr.isEmpty()) {
                        try {
                            student.setDepartment(Department.valueOf(deptStr.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid department: " + deptStr + ". Must be one of: CSE, ECE, EEE, MECH, CIVIL, IT, MBA, MCA, CHEM, AERO");
                        }
                    } else {
                        throw new RuntimeException("Department is required");
                    }

                    if (year != null) {
                        student.setYear(year);
                    } else {
                        Double yearVal = getNumericValue(row.getCell(5));
                        if (yearVal == null) {
                            throw new RuntimeException("Year is required");
                        }
                        student.setYear(yearVal.intValue());
                    }

                    Double semVal = getNumericValue(row.getCell(6));
                    if (semVal == null) {
                        throw new RuntimeException("Current Semester is required");
                    }
                    student.setCurrentSemester(semVal.intValue());

                    String genderStr = getStringValue(row.getCell(7));
                    if (genderStr != null && !genderStr.isEmpty()) {
                        try {
                            student.setGender(Gender.valueOf(genderStr.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid gender: " + genderStr + ". Must be MALE, FEMALE, or OTHER");
                        }
                    }

                    student.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                    student.setDeleted(false);

                    studentsToSave.add(student);
                    response.setSuccessCount(response.getSuccessCount() + 1);

                } catch (Exception e) {
                    response.setFailureCount(response.getFailureCount() + 1);
                    errors.add(BulkUploadResponseDTO.UploadError.builder()
                            .rowNumber(rowNum)
                            .errorMessage(e.getMessage())
                            .build());
                }
            }

            response.setTotalRecords(response.getSuccessCount() + response.getFailureCount());

            if (!studentsToSave.isEmpty()) {
                try {
                    studentRepository.saveAll(studentsToSave);
                    response.getSummaries().add(BulkUploadResponseDTO.UploadSummary.builder()
                            .entityType("Students")
                            .total(studentsToSave.size())
                            .created(studentsToSave.size())
                            .build());
                } catch (DataIntegrityViolationException e) {
                    String errorMsg = e.getMostSpecificCause().getMessage();
                    if (errorMsg.contains("roll_no")) {
                        throw new RuntimeException("Duplicate Roll Number found. Please check your data.");
                    } else if (errorMsg.contains("email")) {
                        throw new RuntimeException("Duplicate Email found. Please check your data.");
                    } else if (errorMsg.contains("phone")) {
                        throw new RuntimeException("Duplicate Phone Number found. Please check your data.");
                    } else {
                        throw new RuntimeException("Data integrity violation: " + errorMsg);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to save students: " + e.getMessage());
                }
            }

            response.setErrors(errors);

            if (response.getSuccessCount() > 0) {
                response.setMessage("Successfully uploaded " + response.getSuccessCount() + " students. " + 
                                   response.getFailureCount() + " failed.");
            } else {
                response.setSuccess(false);
                response.setMessage("Upload failed. " + response.getFailureCount() + " records failed.");
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to process file: " + e.getMessage());
        }

        return response;
    }

    @Transactional
    public BulkUploadResponseDTO uploadRooms(MultipartFile file) {
        BulkUploadResponseDTO response = BulkUploadResponseDTO.builder()
                .success(true)
                .errors(new ArrayList<>())
                .summaries(new ArrayList<>())
                .build();

        List<Room> roomsToSave = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new RuntimeException("No sheet found in the Excel file");
            }

            boolean isFirstRow = true;
            int rowNum = 0;

            for (Row row : sheet) {
                rowNum++;
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                if (row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                    continue;
                }

                try {
                    Room room = new Room();
                    
                    String roomCode = getStringValue(row.getCell(0));
                    if (roomCode == null || roomCode.isEmpty()) {
                        throw new RuntimeException("Room Code is required");
                    }
                    room.setRoomCode(roomCode);

                    String roomName = getStringValue(row.getCell(1));
                    if (roomName == null || roomName.isEmpty()) {
                        throw new RuntimeException("Room Name is required");
                    }
                    room.setRoomName(roomName);

                    Double capacityVal = getNumericValue(row.getCell(2));
                    if (capacityVal == null) {
                        throw new RuntimeException("Capacity is required");
                    }
                    room.setCapacity(capacityVal.intValue());

                    Double rowsVal = getNumericValue(row.getCell(3));
                    if (rowsVal == null) {
                        throw new RuntimeException("Rows is required");
                    }
                    room.setRows(rowsVal.intValue());

                    Double colsVal = getNumericValue(row.getCell(4));
                    if (colsVal == null) {
                        throw new RuntimeException("Columns is required");
                    }
                    room.setCols(colsVal.intValue());

                    String location = getStringValue(row.getCell(5));
                    room.setLocation(location != null ? location : "");

                    room.setDeleted(false);

                    roomsToSave.add(room);
                    response.setSuccessCount(response.getSuccessCount() + 1);

                } catch (Exception e) {
                    response.setFailureCount(response.getFailureCount() + 1);
                    response.getErrors().add(BulkUploadResponseDTO.UploadError.builder()
                            .rowNumber(rowNum)
                            .errorMessage(e.getMessage())
                            .build());
                }
            }

            response.setTotalRecords(response.getSuccessCount() + response.getFailureCount());

            if (!roomsToSave.isEmpty()) {
                try {
                    roomRepository.saveAll(roomsToSave);
                    response.getSummaries().add(BulkUploadResponseDTO.UploadSummary.builder()
                            .entityType("Rooms")
                            .total(roomsToSave.size())
                            .created(roomsToSave.size())
                            .build());
                } catch (DataIntegrityViolationException e) {
                    if (e.getMostSpecificCause().getMessage().contains("room_code")) {
                        throw new RuntimeException("Duplicate Room Code found. Please check your data.");
                    }
                    throw new RuntimeException("Data integrity violation: " + e.getMostSpecificCause().getMessage());
                }
            }

            if (response.getSuccessCount() > 0) {
                response.setMessage("Successfully uploaded " + response.getSuccessCount() + " rooms.");
            } else {
                response.setSuccess(false);
                response.setMessage("Upload failed. No valid records found.");
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to process file: " + e.getMessage());
        }

        return response;
    }

    @Transactional
    public BulkUploadResponseDTO uploadInvigilators(MultipartFile file) {
        BulkUploadResponseDTO response = BulkUploadResponseDTO.builder()
                .success(true)
                .errors(new ArrayList<>())
                .summaries(new ArrayList<>())
                .build();

        List<Invigilator> invigilatorsToSave = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new RuntimeException("No sheet found in the Excel file");
            }

            boolean isFirstRow = true;
            int rowNum = 0;

            for (Row row : sheet) {
                rowNum++;
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                if (row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                    continue;
                }

                try {
                    Invigilator invigilator = new Invigilator();
                    
                    String employeeId = getStringValue(row.getCell(0));
                    if (employeeId == null || employeeId.isEmpty()) {
                        throw new RuntimeException("Employee ID is required");
                    }
                    invigilator.setEmployeeId(employeeId);

                    String name = getStringValue(row.getCell(1));
                    if (name == null || name.isEmpty()) {
                        throw new RuntimeException("Name is required");
                    }
                    invigilator.setName(name);

                    String phone = getStringValue(row.getCell(2));
                    if (phone == null || phone.isEmpty()) {
                        throw new RuntimeException("Phone is required");
                    }
                    if (!phone.matches("^[6-9]\\d{9}$")) {
                        throw new RuntimeException("Invalid phone number: " + phone);
                    }
                    invigilator.setPhone(phone);

                    String email = getStringValue(row.getCell(3));
                    if (email == null || email.isEmpty()) {
                        throw new RuntimeException("Email is required");
                    }
                    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        throw new RuntimeException("Invalid email format: " + email);
                    }
                    invigilator.setEmail(email);

                    String deptStr = getStringValue(row.getCell(4));
                    if (deptStr != null && !deptStr.isEmpty()) {
                        try {
                            invigilator.setDepartment(Department.valueOf(deptStr.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid department: " + deptStr);
                        }
                    } else {
                        throw new RuntimeException("Department is required");
                    }

                    invigilator.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                    invigilator.setFirstLogin(true);
                    invigilator.setDeleted(false);

                    invigilatorsToSave.add(invigilator);
                    response.setSuccessCount(response.getSuccessCount() + 1);

                } catch (Exception e) {
                    response.setFailureCount(response.getFailureCount() + 1);
                    response.getErrors().add(BulkUploadResponseDTO.UploadError.builder()
                            .rowNumber(rowNum)
                            .errorMessage(e.getMessage())
                            .build());
                }
            }

            response.setTotalRecords(response.getSuccessCount() + response.getFailureCount());

            if (!invigilatorsToSave.isEmpty()) {
                try {
                    invigilatorRepository.saveAll(invigilatorsToSave);
                    response.getSummaries().add(BulkUploadResponseDTO.UploadSummary.builder()
                            .entityType("Invigilators")
                            .total(invigilatorsToSave.size())
                            .created(invigilatorsToSave.size())
                            .build());
                } catch (DataIntegrityViolationException e) {
                    if (e.getMostSpecificCause().getMessage().contains("employee_id")) {
                        throw new RuntimeException("Duplicate Employee ID found. Please check your data.");
                    } else if (e.getMostSpecificCause().getMessage().contains("email")) {
                        throw new RuntimeException("Duplicate Email found. Please check your data.");
                    }
                    throw new RuntimeException("Data integrity violation: " + e.getMostSpecificCause().getMessage());
                }
            }

            if (response.getSuccessCount() > 0) {
                response.setMessage("Successfully uploaded " + response.getSuccessCount() + " invigilators.");
            } else {
                response.setSuccess(false);
                response.setMessage("Upload failed. No valid records found.");
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Failed to process file: " + e.getMessage());
        }

        return response;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private Double getNumericValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public byte[] generateTemplate(String type) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(type);

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = getHeadersForType(type);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            addSampleData(sheet, type);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate template: " + e.getMessage());
        }
    }

    private String[] getHeadersForType(String type) {
        switch (type.toLowerCase()) {
            case "students":
                return new String[]{"Roll No", "Name", "Email", "Phone", "Department", "Year", "Current Semester", "Gender"};
            case "rooms":
                return new String[]{"Room Code", "Room Name", "Capacity", "Rows", "Columns", "Location"};
            case "invigilators":
                return new String[]{"Employee ID", "Name", "Phone", "Email", "Department"};
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private void addSampleData(Sheet sheet, String type) {
        Row sampleRow = sheet.createRow(1);
        switch (type.toLowerCase()) {
            case "students":
                sampleRow.createCell(0).setCellValue("2024001");
                sampleRow.createCell(1).setCellValue("John Doe");
                sampleRow.createCell(2).setCellValue("john.doe@email.com");
                sampleRow.createCell(3).setCellValue("9876543210");
                sampleRow.createCell(4).setCellValue("CSE");
                sampleRow.createCell(5).setCellValue("1");
                sampleRow.createCell(6).setCellValue("2");
                sampleRow.createCell(7).setCellValue("MALE");
                break;
            case "rooms":
                sampleRow.createCell(0).setCellValue("R101");
                sampleRow.createCell(1).setCellValue("Room 101");
                sampleRow.createCell(2).setCellValue("40");
                sampleRow.createCell(3).setCellValue("5");
                sampleRow.createCell(4).setCellValue("8");
                sampleRow.createCell(5).setCellValue("Building A, Floor 1");
                break;
            case "invigilators":
                sampleRow.createCell(0).setCellValue("EMP001");
                sampleRow.createCell(1).setCellValue("Dr. Smith");
                sampleRow.createCell(2).setCellValue("9876543210");
                sampleRow.createCell(3).setCellValue("smith@university.edu");
                sampleRow.createCell(4).setCellValue("CSE");
                break;
        }
    }
}