package com.userreport.UserReportBackend.services;

import com.userreport.UserReportBackend.dto.info.ChartDataDto;
import java.util.List;

public interface DashboardService {
    List<ChartDataDto> getDashboardDataForYear(int year);
}