package com.jeevan.orderMng.utils;

import com.jeevan.orderMng.entity.User;
import com.jeevan.orderMng.constant.Role;
import com.jeevan.orderMng.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .name("Manager One")
                    .email("manager@gmail.com")
                    .password(passwordEncoder.encode("manager@123"))
                    .role(Role.MANAGER)
                    .active(true)
                    .build();
            userRepository.save(admin);

            System.out.println("Default admin user created: admin@example.com / admin123");
        }
    }
}