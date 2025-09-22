package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.entity.CollectionEntity;
import com.userreport.UserReportBackend.entity.TargetEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface ExcelUploadService {
    boolean isValidExcelFile(MultipartFile file);
    List<TargetEntity> getTargetsFromExcel(InputStream inputStream, int year, int month);
    List<CollectionEntity> getCollectionsFromExcel(InputStream inputStream, int year, int month);

    List<CollectionEntity> updateCollectionsFromExcel(InputStream inputStream, int year, int month);
}
