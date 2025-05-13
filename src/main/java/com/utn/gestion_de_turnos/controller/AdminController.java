package com.utn.gestion_de_turnos.controller;

import com.utn.gestion_de_turnos.model.Admin;
import com.utn.gestion_de_turnos.service.AdminService;
import com.utn.gestion_de_turnos.service.SessionManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping
    public Admin createAdmin(@RequestBody Admin admin) {
        return adminService.save(admin);
    }

    @GetMapping("/{id}")
    public Optional<Admin> getAdminById(@PathVariable Long id) {
        return adminService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteAdmin(@PathVariable Long id) {
        adminService.deleteById(id);
    }


    @PostMapping("/login")
    public ResponseEntity<String> loginAdmin(@RequestBody Map<String, String> requestBody, HttpServletResponse response) {
        String email = requestBody.get("email");
        String contrasena = requestBody.get("contrasena");

        Admin admin = adminService.login(email, contrasena);

        if (admin != null) {
            String sessionId = UUID.randomUUID().toString();
            Cookie sessionCookie = new Cookie("sessionId", sessionId);
            sessionCookie.setPath("/");
            sessionCookie.setHttpOnly(true);
            response.addCookie(sessionCookie);

            SessionManager.storeSession(sessionId, admin);

            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

    }

    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins(@CookieValue(value = "sessionId", defaultValue = "") String sessionId) {
        if (!SessionManager.isSessionValid(sessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        List<Admin> admins = adminService.findAll();
        return ResponseEntity.ok(admins);
    }
}
