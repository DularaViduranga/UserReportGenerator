package com.userreport.UserReportBackend.entity;

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

    // One-to-One: This target belongs to one Branch
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sdb_branch_id", unique = true) // unique = true ensures one-to-one
    private BranchEntity branch;

    // Audit Fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false) // Assuming createdBy is
    private UserEntity createdBy;

    @Column(name = "created_datetime")
    private LocalDateTime createdDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modify_by") // Assuming modifyBy is optional
    private UserEntity modifyBy;

    @Column(name = "modify_datetime")
    private LocalDateTime modifyDatetime;
}

