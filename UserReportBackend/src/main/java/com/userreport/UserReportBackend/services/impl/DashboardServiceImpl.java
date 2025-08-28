package com.userreport.UserReportBackend.services.impl;

import com.userreport.UserReportBackend.dto.info.ChartDataDto;
import com.userreport.UserReportBackend.entity.BranchEntity;
import com.userreport.UserReportBackend.repository.BranchRepo;
import com.userreport.UserReportBackend.repository.CollectionRepo;
import com.userreport.UserReportBackend.repository.TargetRepo;
import com.userreport.UserReportBackend.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private BranchRepo branchRepo;
    @Autowired
    private TargetRepo targetRepo;
    @Autowired
    private CollectionRepo collectionRepo;

    @Override
    public List<ChartDataDto> getDashboardDataForYear(int year) {
        List<BranchEntity> branches = branchRepo.findAll();

        return branches.stream().map(branch -> {
            BigDecimal totalTarget = targetRepo.sumTargetByBranchAndYear(branch.getId(), year);
            BigDecimal totalCollection = collectionRepo.sumCollectionByBranchAndYear(branch.getId(), year);

            totalTarget = (totalTarget == null) ? BigDecimal.ZERO : totalTarget;
            totalCollection = (totalCollection == null) ? BigDecimal.ZERO : totalCollection;

            BigDecimal achievement = BigDecimal.ZERO;
            if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
                achievement = totalCollection.divide(totalTarget, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            return new ChartDataDto(branch.getBrnName(), totalTarget, totalCollection, achievement);
        }).collect(Collectors.toList());
    }
}