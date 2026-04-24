package com.aquaindica.config;

import com.aquaindica.Entity.Admin;
import com.aquaindica.enums.Role;
import com.aquaindica.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class StartupDataLoader implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
 private  PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminRepository.findByUsername("sumedhchobitkar04@gmail.com").isEmpty()) {
            Admin admin = Admin.builder()
                    .username("sumedhchobitkar04@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .email("sumedhchobitkar04@gmail.com")
                    .role(Role.ADMIN)
                    .build();
            adminRepository.save(admin);
            System.out.println("✅ Default admin created");
        }
    }
}
