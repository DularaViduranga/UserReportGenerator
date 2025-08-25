package com.userreport.UserReportBackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sdb_branch")
public class BranchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brn_name", length = 45, nullable = false)
    private String brnName;

    @Column(name = "brn_des", length = 45)
    private String brnDes;

    // Many-to-One: Many Branches belong to one Region
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "mkt_sdb_region_id", nullable = false)
    private RegionEntity region;

    // One-to-One with SdbTargat
    @OneToOne(mappedBy = "branch", cascade = CascadeType.ALL)
    private TargetEntity target;

    //Constructor for creating a new Branch
    public BranchEntity(String brnName, String brnDes, RegionEntity region) {
        this.brnName = brnName;
        this.brnDes = brnDes;
        this.region = region;
    }
}
