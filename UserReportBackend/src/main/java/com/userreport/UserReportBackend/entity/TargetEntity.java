package com.userreport.UserReportBackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sdb_target")
public class TargetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target", precision = 10, scale = 2, nullable = false)
    private BigDecimal target;

    // New fields for time period
    @Column(name = "target_year", nullable = false)
    private Integer targetYear;

    @Column(name = "target_month", nullable = false)
    private Integer targetMonth; // 1-12 for Jan-Dec

    // Changed from One-to-One to Many-to-One: Many targets can belong to one Branch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sdb_branch_id", nullable = false)
    @JsonIgnore
    private BranchEntity branch;

    // Audit Fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnore
    private UserEntity createdBy;

    @Column(name = "created_datetime")
    private LocalDateTime createdDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modify_by")
    @JsonIgnore
    private UserEntity modifyBy;

    @Column(name = "modify_datetime")
    private LocalDateTime modifyDatetime;

    // Constructor for creating a new target
    public TargetEntity(BigDecimal target, Integer targetYear, Integer targetMonth,
                        BranchEntity branch, UserEntity createdBy) {
        this.target = target;
        this.targetYear = targetYear;
        this.targetMonth = targetMonth;
        this.branch = branch;
        this.createdBy = createdBy;
        this.createdDatetime = LocalDateTime.now();
    }


    public BigDecimal getTargetAmount() {
        return target;
    }

    public TargetEntity orElse(Object o) {
        return  o instanceof TargetEntity ? (TargetEntity)o : null;
    }

}
