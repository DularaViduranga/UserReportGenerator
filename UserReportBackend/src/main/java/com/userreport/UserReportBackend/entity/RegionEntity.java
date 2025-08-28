package com.userreport.UserReportBackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mkt_sdb_region")
public class RegionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(name = "rgn_name", length = 45,nullable = false)
    private String rgnName;

    @Column(name = "rgn_des", length = 45, nullable = false)
    private String rgnDes;

    // One-to-Many: One Region has many Branches
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<BranchEntity> branches = new ArrayList<>();

    // Constructor for creating a new Region
    public RegionEntity(String rgnName, String rgnDes) {
        this.rgnName = rgnName;
        this.rgnDes = rgnDes;
    }

    public int getId() {
        return id.intValue();
    }
}
