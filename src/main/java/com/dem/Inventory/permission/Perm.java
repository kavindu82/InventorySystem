package com.dem.Inventory.permission;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("perm")
public class Perm {
    private final PermissionService permissionService;
    public Perm(PermissionService permissionService) { this.permissionService = permissionService; }

    // For Thymeleaf: ${perm.show('BTN.SALES.DELETE')}
    public boolean show(String featureKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return permissionService.can(auth, featureKey);
    }

    // For @PreAuthorize: @PreAuthorize("@perm.can(authentication,'SALE.DELETE')")
    public boolean can(Authentication authentication, String featureKey) {
        return permissionService.can(authentication, featureKey);
    }
}