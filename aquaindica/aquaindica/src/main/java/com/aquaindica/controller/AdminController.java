package com.aquaindica.controller;

import com.aquaindica.Entity.Admin;
import com.aquaindica.Entity.Service;
import com.aquaindica.Entity.ServiceTopic;
import com.aquaindica.Entity.TopicDetail;
import com.aquaindica.config.OtpUtil;
import com.aquaindica.dto.AdminLoginRequest;
import com.aquaindica.dto.OtpRequest;
import com.aquaindica.dto.ReplyDto;
import com.aquaindica.repository.AdminRepository;
import com.aquaindica.repository.ServiceRepository;
import com.aquaindica.repository.ServiceTopicRepository;
import com.aquaindica.repository.TopicDetailRepository;
import com.aquaindica.service.ContactService;
import com.aquaindica.serviceimpl.EmailService;
import com.aquaindica.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminController {

    private final AdminRepository adminRepository;
    private final OtpUtil otpUtil;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ContactService contactService;

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
//        Admin admin = adminRepository.findByUsername(request.getUsername())
//                .orElseThrow(() -> new RuntimeException("Invalid username"));
//
//        if (!admin.getPassword().equals(request.getPassword())) {
//            throw new RuntimeException("Invalid password");
//        }
//
//        return ResponseEntity.ok("Login successful");
//    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {

        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String otp = otpUtil.generateOtp();
        admin.setOtp(otp);
        admin.setOtpExpiry(LocalDateTime.now().plusMinutes(3));
        adminRepository.save(admin);

        emailService.sendOtp(admin.getEmail(), otp);
        return ResponseEntity.ok(Map.of("message", "OTP sent to your email"));
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request) {

        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (admin.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        admin.setOtp(null);
        admin.setOtpExpiry(null);
        adminRepository.save(admin);

        String token = jwtUtil.generateToken(admin.getUsername(),admin.getRole().name());

        emailService.sendLoginNotification(admin.getEmail(),request.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/reply")
    public ResponseEntity<Map<String, String>> sendReply(@RequestBody ReplyDto dto) {
        emailService.sendMsg(dto.getEmail(), dto.getUsername(), dto.getMessage(),dto.getCc());
        return ResponseEntity.ok(Map.of("message", "Email Sent Successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok("Contact deleted successfully");
    }
// service

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private ServiceTopicRepository topicRepo;

    @Autowired
    private TopicDetailRepository detailRepo;

    // Add Service
    @PostMapping("/service")
    public Service addService(@RequestBody Service service) {
        return serviceRepo.save(service);
    }

    // Add Topic
    @PostMapping("/topic/{serviceId}")
    public ServiceTopic addTopic(@PathVariable Long serviceId, @RequestBody ServiceTopic topic) {
        Service service = serviceRepo.findById(serviceId).orElseThrow();
        topic.setService(service);
        return topicRepo.save(topic);
    }

    // Add Topic Detail
//    @PostMapping("/detail/{topicId}")
//    public TopicDetail addDetail(@PathVariable Long topicId, @RequestBody TopicDetail detail) {
//        ServiceTopic topic = topicRepo.findById(topicId).orElseThrow();
//        detail.setTopic(topic);
//        return detailRepo.save(detail);
//    }

    @PostMapping(value = "/detail/{topicId}", consumes = "multipart/form-data")
    public TopicDetail addDetail(
            @PathVariable Long topicId,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile file
    ) throws Exception {

        ServiceTopic topic = topicRepo.findById(topicId).orElseThrow();

        TopicDetail detail = new TopicDetail();
        detail.setDescription(description);
        detail.setTopic(topic);

        // convert image to byte[]
        detail.setImage(file.getBytes());

        return detailRepo.save(detail);
    }

    @GetMapping("/detail/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {

        TopicDetail detail = detailRepo.findById(id).orElseThrow();

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(detail.getImage());
    }
    @DeleteMapping("/service/{id}")
    public ResponseEntity<String> deleteService(@PathVariable Long id) {
        serviceRepo.deleteById(id);
        return ResponseEntity.ok("Service deleted successfully");
    }

    @DeleteMapping("/topic/{id}")
    public ResponseEntity<String> deleteTopic(@PathVariable Long id) {
        topicRepo.deleteById(id);
        return ResponseEntity.ok("Topic deleted successfully");
    }

    @DeleteMapping("/detail/{id}")
    public ResponseEntity<String> deleteDetail(@PathVariable Long id) {
        detailRepo.deleteById(id);
        return ResponseEntity.ok("Detail deleted successfully");
    }
}
