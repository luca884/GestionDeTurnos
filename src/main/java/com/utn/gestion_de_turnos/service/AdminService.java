package com.utn.gestion_de_turnos.service;

import com.utn.gestion_de_turnos.model.Admin;
import com.utn.gestion_de_turnos.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Admin save(Admin admin) {
        return adminRepository.save(admin);
    }

    public Optional<Admin> findById(Long id) {
        return adminRepository.findById(id);
    }

    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    public void deleteById(Long id) {
        adminRepository.deleteById(id);
    }

    public Admin login(String email, String contrasena) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            System.out.println("❌ Email not found: " + email);
        } else {
            System.out.println("✅ Found admin: " + admin.getEmail());
            System.out.println("🔑 Encrypted password in DB: " + admin.getContrasena());
            System.out.println("🔑 Password entered: " + contrasena);
            System.out.println("🔍 Password matches? " + passwordEncoder.matches(contrasena, admin.getContrasena()));
        }
        if (admin != null && passwordEncoder.matches(contrasena,admin.getContrasena())) {
            return admin;
        }
        return null;
    }
}
