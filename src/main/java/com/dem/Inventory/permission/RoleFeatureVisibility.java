package com.dem.Inventory.permission;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "role_feature_visibility",
        uniqueConstraints = @UniqueConstraint(name="uq_role_feature", columnNames = {"role_name","feature_key"}))
public class RoleFeatureVisibility {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, length = 32)
    private String roleName; // "ADMIN" | "SUPERUSER" | "STAFF"

    @Column(name = "feature_key", nullable = false, length = 128)
    private String featureKey;

    @Column(nullable = false)
    private boolean visible;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist @PreUpdate void touch() { updatedAt = Instant.now(); }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}