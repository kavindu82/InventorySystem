package com.dem.Inventory.permission;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PermissionService {

    private final FeatureRepository featureRepo;
    private final RoleFeatureVisibilityRepository rfvRepo;

    // simple cache: role -> (featureKey -> visible)
    private final Map<String, Map<String, Boolean>> cache = new ConcurrentHashMap<>();

    public PermissionService(FeatureRepository featureRepo, RoleFeatureVisibilityRepository rfvRepo) {
        this.featureRepo = featureRepo; this.rfvRepo = rfvRepo;
    }

    /** Server & template check */
    public boolean can(Authentication auth, String featureKey) {
        if (auth == null || !auth.isAuthenticated()) return false;

        if (hasRole(auth, "ROLE_ADMIN")) return true; // Admin unrestricted

        String role = resolveRole(auth); // SUPERUSER or STAFF (first matching)
        if (role == null) return false;

        Map<String, Boolean> m = cache.computeIfAbsent(role, this::loadVisibilityForRole);
        // default = visible if not overridden
        return m.getOrDefault(featureKey, true);
    }

    /** Admin updates a single toggle */
    public void setVisibility(String role, String featureKey, boolean visible, String updatedBy) {
        RoleFeatureVisibility r = rfvRepo.findByRoleNameAndFeatureKey(role, featureKey)
                .orElseGet(RoleFeatureVisibility::new);
        r.setRoleName(role);
        r.setFeatureKey(featureKey);
        r.setVisible(visible);
        r.setUpdatedBy(updatedBy);
        rfvRepo.save(r);
        cache.remove(role); // invalidate
    }

    /** Admin resets a role to defaults (all visible) */
    public void resetRole(String role) {
        rfvRepo.deleteAllByRoleName(role);
        cache.remove(role);
    }

    /** For the Admin UI */
    public List<FeatureVM> listForRole(String role) {
        List<Feature> all = featureRepo.findAllByOrderByCategoryAscGroupNameAscLabelAsc();
        Map<String, Boolean> m = cache.computeIfAbsent(role, this::loadVisibilityForRole);

        List<FeatureVM> out = new ArrayList<>();
        for (Feature f : all) {
            boolean visible = m.getOrDefault(f.getFeatureKey(), true);
            out.add(FeatureVM.from(f, visible));
        }
        return out;
    }

    // ---- helpers ----
    private boolean hasRole(Authentication auth, String role) {
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (role.equalsIgnoreCase(ga.getAuthority())) return true;
        }
        return false;
    }

    private String resolveRole(Authentication auth) {
        // priority SUPERUSER then STAFF (adjust if you have more)
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_SUPERUSER".equalsIgnoreCase(a)) return "SUPERUSER";
            if ("ROLE_STAFF".equalsIgnoreCase(a)) return "STAFF";
        }
        return null;
    }

    private Map<String, Boolean> loadVisibilityForRole(String role) {
        Map<String, Boolean> map = new HashMap<>();
        for (RoleFeatureVisibility r : rfvRepo.findAllByRoleName(role)) {
            map.put(r.getFeatureKey(), r.isVisible());
        }
        return map;
    }

    // View model for Admin UI
    public record FeatureVM(String featureKey, String label, String category, String groupName, String description, boolean visible) {
        static FeatureVM from(Feature f, boolean visible) {
            return new FeatureVM(
                    f.getFeatureKey(), f.getLabel(), f.getCategory().name(),
                    f.getGroupName(), f.getDescription(), visible
            );
        }
    }
}
