package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.entity.CollectionEntity;
import com.userreport.UserReportBackend.entity.TargetEntity;
import com.userreport.UserReportBackend.entity.UserEntity;
import com.userreport.UserReportBackend.repository.BranchRepo;
import com.userreport.UserReportBackend.repository.CollectionRepo;
import com.userreport.UserReportBackend.repository.TargetRepo;
import com.userreport.UserReportBackend.repository.UserRepo;
import com.userreport.UserReportBackend.services.ExcelUploadService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExcelUploadServiceImpl implements ExcelUploadService {
    private final BranchRepo branchRepo;
    private final UserRepo userRepo;
    private final CollectionRepo collectionRepo;
    private final TargetRepo targetRepo;

    public ExcelUploadServiceImpl(BranchRepo branchRepo, UserRepo userRepo, CollectionRepo collectionRepo, TargetRepo targetRepo) {
        this.branchRepo = branchRepo;
        this.userRepo = userRepo;
        this.collectionRepo = collectionRepo;
        this.targetRepo = targetRepo;
    }

    public boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        return Objects.equals(file.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Override
    public List<TargetEntity> getTargetsFromExcel(InputStream inputStream, int year, int month) {
        List<TargetEntity> targets = new ArrayList<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheet("targets");

            int rowIndex = 0;
            for (Row row : sheet) {
                if (rowIndex == 0) {
                    rowIndex++;
                    continue; // Skip header row
                }

                Iterator<Cell> cellIterator = row.cellIterator();
                int cellIndex = 0;
                TargetEntity target = new TargetEntity();

                // Set year and month from path variables
                target.setTargetYear(year);
                target.setTargetMonth(month);

                UserEntity currentUser = getCurrentUser();
                if (currentUser != null) {
                    target.setCreatedBy(currentUser);
                }
                target.setCreatedDatetime(LocalDateTime.now());

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cellIndex) {
                        case 0 -> target.setBranch(branchRepo.findByBrnName(cell.getStringCellValue()));
                        case 1 -> target.setTarget(BigDecimal.valueOf(cell.getNumericCellValue()));
                        default -> throw new IllegalStateException("Unexpected value: " + cellIndex);
                    }
                    cellIndex++;
                }
                targets.add(target);
            }
            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }
        return targets;
    }

    @Override
    public List<CollectionEntity> getCollectionsFromExcel(InputStream inputStream, int year, int month) {
        List<CollectionEntity> collections = new ArrayList<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheet("collections");

            int rowIndex = 0;
            for (Row row : sheet) {
                if (rowIndex == 0) {
                    rowIndex++;
                    continue; // Skip header row
                }

                Iterator<Cell> cellIterator = row.cellIterator();
                int cellIndex = 0;
                CollectionEntity collection = new CollectionEntity();
                String branchName = null;

                // Set year and month from path variables
                collection.setCollectionYear(year);
                collection.setCollectionMonth(month);

                UserEntity currentUser = getCurrentUser();
                if (currentUser != null) {
                    collection.setCreatedBy(currentUser);
                }
                collection.setCreatedDatetime(LocalDateTime.now());

                BigDecimal collectionAmount = null;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cellIndex) {
                        case 0 -> {
                            branchName = cell.getStringCellValue();
                            collection.setBranch(branchRepo.findByBrnName(branchName));
                        }
                        case 1 -> {
                            collectionAmount = BigDecimal.valueOf(cell.getNumericCellValue());
                            collection.setCollectionAmount(collectionAmount);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + cellIndex);
                    }
                    cellIndex++;
                }
                // Set target after processing all cells
                BigDecimal targetAmount = null;
                if (branchName != null) {
                    BranchEntity branch = branchRepo.findByBrnName(branchName);
                    if (branch != null) {
                        // Fix: Handle Optional return type
                        Optional<TargetEntity> targetEntityOpt = targetRepo.findByBranchIdAndYearAndMonth(
                                branch.getId(),
                                year,
                                month
                        );

                        if (targetEntityOpt.isPresent()) {
                            targetAmount = targetEntityOpt.get().getTarget();
                        }
                    }
                }

                if (targetAmount != null) {
                    collection.setTarget(targetAmount);
                    collection.setPercentage(calculatePercentage(collectionAmount, targetAmount));
                    collection.setDue(targetAmount.subtract(collectionAmount));
                } else {
                    // Handle case when no target is found
                    collection.setTarget(BigDecimal.ZERO);
                    collection.setPercentage(BigDecimal.ZERO);
                    collection.setDue(BigDecimal.ZERO);
                }

                collections.add(collection); // Don't forget to add the collection to the list
            }
            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }
        return collections;
    }


    @Override
    public List<CollectionEntity> updateCollectionsFromExcel(InputStream inputStream, int year, int month) {
        List<CollectionEntity> collections = new ArrayList<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheet("collections");

            int rowIndex = 0;
            for (Row row : sheet) {
                if (rowIndex == 0) {
                    rowIndex++;
                    continue; // Skip header row
                }

                Iterator<Cell> cellIterator = row.cellIterator();
                int cellIndex = 0;
                String branchName = null;
                BigDecimal collectionAmount = null;

                // First, extract data from Excel
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cellIndex) {
                        case 0 -> branchName = cell.getStringCellValue();
                        case 1 -> collectionAmount = BigDecimal.valueOf(cell.getNumericCellValue());
                        default -> throw new IllegalStateException("Unexpected value: " + cellIndex);
                    }
                    cellIndex++;
                }

                // Find and update existing collection record
                if (branchName != null) {
                    BranchEntity branch = branchRepo.findByBrnName(branchName);
                    if (branch != null) {
                        // Find existing collection by branch, year, and month
                        Optional<CollectionEntity> existingCollectionOpt = collectionRepo
                                .findByBranchAndCollectionYearAndCollectionMonth(branch, year, month);

                        if (existingCollectionOpt.isPresent()) {
                            CollectionEntity collection = existingCollectionOpt.get();

                            // Update only the collection amount
                            collection.setCollectionAmount(collectionAmount);

                            // Update audit fields
                            UserEntity currentUser = getCurrentUser();
                            if (currentUser != null) {
                                collection.setModifyBy(currentUser);
                            }
                            collection.setModifyDatetime(LocalDateTime.now());

                            // Recalculate percentage and due based on existing target
                            BigDecimal existingTarget = collection.getTarget();
                            if (existingTarget != null && existingTarget.compareTo(BigDecimal.ZERO) > 0) {
                                collection.setPercentage(calculatePercentage(collectionAmount, existingTarget));
                                collection.setDue(existingTarget.subtract(collectionAmount));
                            } else {
                                collection.setPercentage(BigDecimal.ZERO);
                                collection.setDue(collectionAmount.negate()); // Negative due if no target
                            }

                            collections.add(collection);
                        }
                    }
                }
            }
            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }
        return collections;
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return userRepo.findByUsername(username).orElse(null);
    }

    private BigDecimal calculatePercentage(BigDecimal collectionAmount, BigDecimal targetAmount) {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return collectionAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
    }

}
