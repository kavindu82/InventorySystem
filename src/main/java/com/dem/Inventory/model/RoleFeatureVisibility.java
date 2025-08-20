package com.dem.Inventory.model;

import com.dem.Inventory.model.FeatureKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "role_feature_visibility",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role","feature"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RoleFeatureVisibility {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String role; // "MANAGER", "STAFF"  (we usually don't store ADMIN here)

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=64)
    private FeatureKey feature;

    @Column(nullable=false)
    private boolean visible = true; // default: visible

}
