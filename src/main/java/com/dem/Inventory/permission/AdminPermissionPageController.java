package com.dem.Inventory.permission;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminPermissionPageController {

    @GetMapping("/permissions")
    public String permissionsPage(Model model) {
        model.addAttribute("roles", new String[]{"STAFF", "SUPERUSER"});
        model.addAttribute("defaultRole", "STAFF");
        return "permissions"; // <-- matches templates/permissions.html
    }
}
