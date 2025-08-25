package com.dem.Inventory.permission;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
public class AdminPermissionApiController {

    private final PermissionService service;
    public AdminPermissionApiController(PermissionService service) { this.service = service; }

    @GetMapping
    public List<PermissionService.FeatureVM> list(@RequestParam String role) {
        return service.listForRole(role);
    }

    public record UpdateReq(String role, String featureKey, boolean visible) {}
    @PutMapping
    public void update(@RequestBody UpdateReq req, Authentication auth) {
        service.setVisibility(req.role(), req.featureKey(), req.visible(),
                auth != null ? auth.getName() : "system");
    }

    @PostMapping("/reset")
    public void reset(@RequestParam String role) {
        service.resetRole(role);
    }
}
