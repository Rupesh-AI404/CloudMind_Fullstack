package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class ImageController {

    @PostMapping("/uploadImage")
    public String uploadImage(@RequestParam("image") MultipartFile file,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select an image");
                return "redirect:/user-dashboard";
            }

            String email = (String) session.getAttribute("email");
            if (email == null) {
                redirectAttributes.addFlashAttribute("error", "Session expired");
                return "redirect:/login";
            }

            // Better email sanitization for filename
            String safeFileName = email.replaceAll("[^a-zA-Z0-9]", "_") + "_profile.jpg";
            System.out.println("Saving file as: " + safeFileName); // Debug log

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Please upload a valid image file");
                return "redirect:/user-dashboard";
            }

            // Validate file size (2MB max)
            if (file.getSize() > 2 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "File size must be less than 2MB");
                return "redirect:/user-dashboard";
            }

            // Create uploads directory if it doesn't exist
            String uploadDir = "uploads/profile-images/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                System.out.println("Directory created: " + created); // Debug log
            }

            // Save file
            java.nio.file.Path filePath = Paths.get(uploadDir + safeFileName);
            Files.write(filePath, file.getBytes());
            System.out.println("File saved successfully: " + filePath.toString()); // Debug log

            redirectAttributes.addFlashAttribute("success", "Profile picture updated successfully");

        } catch (IOException e) {
            System.err.println("Error uploading image: " + e.getMessage()); // Debug log
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
        }

        return "redirect:/user-dashboard";
    }

    @GetMapping("/image/user/{email}")
    @ResponseBody
    public ResponseEntity<byte[]> getUserProfileImage(@PathVariable String email) {
        try {
            // Use same sanitization logic
            String safeFileName = email.replaceAll("[^a-zA-Z0-9]", "_") + "_profile.jpg";
            java.nio.file.Path filePath = Paths.get("uploads/profile-images/" + safeFileName);

            System.out.println("Looking for file: " + filePath.toString()); // Debug log

            if (Files.exists(filePath)) {
                byte[] imageBytes = Files.readAllBytes(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageBytes);
            } else {
                System.out.println("File not found: " + filePath.toString()); // Debug log
            }
        } catch (IOException e) {
            System.err.println("Error reading image: " + e.getMessage()); // Debug log
        }

        return ResponseEntity.notFound().build();
    }




    //update profile

    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String bio,
                                @RequestParam(required = false) String company,
                                @RequestParam(required = false) String location,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            String email = (String) session.getAttribute("email");
            if (email == null) {
                redirectAttributes.addFlashAttribute("error", "Session expired");
                return "redirect:/login";
            }

            // Update user profile in database
            // You'll need to create/update your User entity and repository
            // Example:
            // userService.updateProfile(email, fullName, phone, bio, company, location);

            // Update session with new name
            session.setAttribute("activeUser", fullName);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/user-dashboard";
    }
}