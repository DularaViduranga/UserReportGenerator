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
@Table(name = "sdb_collection")
public class CollectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target", precision = 10, scale = 2)
    private BigDecimal target;

    @Column(name = "due", precision = 10, scale = 2)
    private BigDecimal due;

    @Column(name = "collection", precision = 10, scale = 2)
    private BigDecimal collectionAmount;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    // New fields for time period
    @Column(name = "collection_year", nullable = false)
    private Integer collectionYear;

    @Column(name = "collection_month", nullable = false)
    private Integer collectionMonth; // 1-12 for Jan-Dec

    // Changed from One-to-One to Many-to-One: Many collections can belong to one Branch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sdb_branch_id", nullable = false)
    @JsonIgnore
    private BranchEntity branch;

    // Audit Fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnore
    private  UserEntity createdBy;

    @Column(name = "created_datetime")
    private LocalDateTime createdDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modify_by")
    @JsonIgnore
    private UserEntity modifyBy;

    @Column(name = "modify_datetime")
    private LocalDateTime modifyDatetime;

    // Constructor for creating a new collection
    public CollectionEntity(BigDecimal target, BigDecimal due, BigDecimal collectionAmount,
                            Integer collectionYear, Integer collectionMonth,
                            BranchEntity branch, UserEntity createdBy) {
        this.target = target;
        this.due = due;
        this.collectionAmount = collectionAmount;
        this.collectionYear = collectionYear;
        this.collectionMonth = collectionMonth;
        this.branch = branch;
        this.createdBy = createdBy;
        this.createdDatetime = LocalDateTime.now();

        // Calculate percentage if target is not zero
        if (target != null && target.compareTo(BigDecimal.ZERO) > 0) {
            this.percentage = collectionAmount.divide(target, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }


}
