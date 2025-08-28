package com.userreport.UserReportBackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


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
    @JoinColumn(name = "mkt_sdb_region_id", nullable = false)
    @JsonIgnore
    private RegionEntity region;

    // One-to-Many with TargetEntity (changed from One-to-One)
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TargetEntity> targets = new ArrayList<>();

    // One-to-Many with CollectionEntity (changed from One-to-One)
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CollectionEntity> collections = new ArrayList<>();

    //Constructor for creating a new Branch
    public BranchEntity(String brnName, String brnDes, RegionEntity region) {
        this.brnName = brnName;
        this.brnDes = brnDes;
        this.region = region;
    }

    // Helper methods to get specific month/year data
    public TargetEntity getTargetForMonth(int year, int month) {
        return targets.stream()
                .filter(t -> t.getTargetYear().equals(year) && t.getTargetMonth().equals(month))
                .findFirst()
                .orElse(null);
    }

    public CollectionEntity getCollectionForMonth(int year, int month) {
        return collections.stream()
                .filter(c -> c.getCollectionYear().equals(year) && c.getCollectionMonth().equals(month))
                .findFirst()
                .orElse(null);
    }

    // Helper method to get all targets for a specific year
    public List<TargetEntity> getTargetsForYear(int year) {
        return targets.stream()
                .filter(t -> t.getTargetYear().equals(year))
                .toList();
    }

    // Helper method to get all collections for a specific year
    public List<CollectionEntity> getCollectionsForYear(int year) {
        return collections.stream()
                .filter(c -> c.getCollectionYear().equals(year))
                .toList();
    }

}
