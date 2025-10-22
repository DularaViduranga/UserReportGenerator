package com.userreport.UserReportBackend.controller;

import com.userreport.UserReportBackend.dto.info.ChartDataDto;
import com.userreport.UserReportBackend.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<List<ChartDataDto>> getDashboardData(@RequestParam int year) {
        List<ChartDataDto> data = dashboardService.getDashboardDataForYear(year);
        return ResponseEntity.ok(data);
    }
}