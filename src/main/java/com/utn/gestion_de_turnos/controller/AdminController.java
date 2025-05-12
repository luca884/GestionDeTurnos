package com.utn.gestion_de_turnos.controller;

import com.utn.gestion_de_turnos.model.Admin;
import com.utn.gestion_de_turnos.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping
    public Admin createAdmin(@RequestBody Admin admin) {
        return adminService.save(admin);
    }

    @GetMapping("/{id}")
    public Optional<Admin> getAdminById(@PathVariable Long id) {
        return adminService.findById(id);
    }

    @GetMapping
    public List<Admin> getAllAdmins() {
        return adminService.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteAdmin(@PathVariable Long id) {
        adminService.deleteById(id);
    }

}
