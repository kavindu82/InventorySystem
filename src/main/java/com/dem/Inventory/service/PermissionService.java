package com.dem.Inventory.service;

import com.dem.Inventory.model.FeatureKey;
import com.dem.Inventory.model.RoleFeatureVisibility;
import com.dem.Inventory.repository.RoleFeatureVisibilityRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Service("perm") // <-- this name lets you call @perm in Thymeleaf

public class PermissionService {
    private final RoleFeatureVisibilityRepository repo;

    public PermissionService(RoleFeatureVisibilityRepository repo) {
        this.repo = repo;
    }

    /** Check if the current user can see a feature. Admin always can. */
    @Transactional(readOnly = true)
    public boolean can(String featureKey, Authentication auth) {
        FeatureKey key = FeatureKey.valueOf(featureKey);
        if (auth == null) return false;

        // Admin sees everything
        if (hasRole(auth, "ADMIN")) return true;

        String role = hasRole(auth, "MANAGER") ? "MANAGER"
                : hasRole(auth, "STAFF")   ? "STAFF"
                : null;

        if (role == null) return false;

        return repo.findByRoleAndFeature(role, key)
                .map(RoleFeatureVisibility::isVisible)
                .orElse(true); // default visible unless hidden by admin
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /** Admin use: set visibility for a role/feature */
    @Transactional
    public void setVisibility(String role, FeatureKey feature, boolean visible) {
        var rec = repo.findByRoleAndFeature(role, feature)
                .orElseGet(() -> {
                    var r = new RoleFeatureVisibility();
                    r.setRole(role);
                    r.setFeature(feature);
                    return r;
                });
        rec.setVisible(visible);
        repo.save(rec);
    }

    /** For the admin page: get a role→feature map */
    @Transactional(readOnly = true)
    public Map<FeatureKey, Boolean> getRoleMatrix(String role) {
        Map<FeatureKey, Boolean> map = new EnumMap<>(FeatureKey.class);
        // default all true
        for (FeatureKey fk : FeatureKey.values()) map.put(fk, true);
        repo.findAllByRole(role).forEach(r -> map.put(r.getFeature(), r.isVisible()));
        return map;
    }
}
