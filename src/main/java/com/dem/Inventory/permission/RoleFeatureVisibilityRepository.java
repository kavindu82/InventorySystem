package com.dem.Inventory.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RoleFeatureVisibilityRepository extends JpaRepository<RoleFeatureVisibility, Long> {
    Optional<RoleFeatureVisibility> findByRoleNameAndFeatureKey(String roleName, String featureKey);
    List<RoleFeatureVisibility> findAllByRoleName(String roleName);
    void deleteAllByRoleName(String roleName);
}
