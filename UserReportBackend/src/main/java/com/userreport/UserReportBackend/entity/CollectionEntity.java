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
@Table(name = "sdb_collection")
public class CollectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "targat", precision = 10, scale = 2)
    private BigDecimal targat;

    @Column(name = "due", precision = 10, scale = 2)
    private BigDecimal due;

    @Column(name = "collection", precision = 10, scale = 2)
    private BigDecimal collectionAmount;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    // One-to-One: This collection belongs to one Branch
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sdb_branch_id", unique = true)
    private BranchEntity branch;

    // Audit Fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false) // Assuming createdBy is mandatory
    private  UserEntity createdBy;

    @Column(name = "created_datetime")
    private LocalDateTime createdDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modify_by") // Assuming modifyBy is optional
    private UserEntity modifyBy;

    @Column(name = "modify_datetime")
    private LocalDateTime modifyDatetime;
}

