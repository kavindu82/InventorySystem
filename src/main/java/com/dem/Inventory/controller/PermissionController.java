package com.dem.Inventory.controller;

import com.dem.Inventory.model.FeatureKey;
import com.dem.Inventory.service.PermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin/permissions")

public class PermissionController {

    private final PermissionService perm;

    public PermissionController(PermissionService perm) {
        this.perm = perm;
    }

    @GetMapping
    public String page(Model model) {
        Map<FeatureKey, Boolean> manager = perm.getRoleMatrix("MANAGER");
        Map<FeatureKey, Boolean> staff   = perm.getRoleMatrix("STAFF");
        model.addAttribute("manager", manager);
        model.addAttribute("staff", staff);
        model.addAttribute("features", FeatureKey.values());
        return "admin/permissions"; // Thymeleaf view
    }

    @PostMapping("/toggle")
    @ResponseBody
    public String toggle(
            @RequestParam String role,      // MANAGER or STAFF
            @RequestParam String feature,   // e.g. BTN_DELETE_ITEM
            @RequestParam boolean visible   // true/false
    ) {
        perm.setVisibility(role, FeatureKey.valueOf(feature), visible);
        return "OK";
    }
}
