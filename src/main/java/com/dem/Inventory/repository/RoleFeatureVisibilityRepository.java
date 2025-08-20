package com.dem.Inventory.repository;

import com.dem.Inventory.model.FeatureKey;
import com.dem.Inventory.model.RoleFeatureVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RoleFeatureVisibilityRepository extends JpaRepository<RoleFeatureVisibility, Long> {
    Optional<RoleFeatureVisibility> findByRoleAndFeature(String role, FeatureKey feature);
    List<RoleFeatureVisibility> findAllByRole(String role);
}
