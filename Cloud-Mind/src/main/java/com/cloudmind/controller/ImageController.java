package com.cloudmind.controller;

import com.cloudmind.model.ImageClass;
import com.cloudmind.repository.ImageRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Controller
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @PostMapping("/uploadImage")
    public String uploadProfileImage(@RequestParam("image") MultipartFile file,
                                     HttpSession session, RedirectAttributes redirectAttributes) {

        // Check session
        String userEmail = (String) session.getAttribute("email");
        if (userEmail == null) {
            return "redirect:/login";
        }

        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select an image");
                return "redirect:/user-dashboard";
            }

            // Check file size (max 2MB for better performance)
            if (file.getSize() > 2 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "File size should be less than 2MB");
                return "redirect:/user-dashboard";
            }

            // Delete existing profile image if exists
            Optional<ImageClass> existingImage = imageRepository.findByUserEmailAndFileName(userEmail, "profile_image");
            if (existingImage.isPresent()) {
                imageRepository.delete(existingImage.get());
            }

            // Save new profile image
            ImageClass profileImage = new ImageClass();
            profileImage.setImage(Base64.getEncoder().encodeToString(file.getBytes()));
            profileImage.setUserEmail(userEmail);
            profileImage.setFileName("profile_image");
            profileImage.setUploadDate(new Date());

            imageRepository.save(profileImage);

            redirectAttributes.addFlashAttribute("success", "Profile image updated successfully");

        } catch (Exception e) {
            System.err.println("Error uploading image: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error uploading image");
        }

        return "redirect:/user-dashboard";
    }

    @GetMapping("/image/user/{email}")
    @ResponseBody
    public ResponseEntity<byte[]> getUserProfileImage(@PathVariable String email) {
        try {
            Optional<ImageClass> profileImage = imageRepository.findByUserEmailAndFileName(email, "profile_image");

            if (profileImage.isPresent()) {
                byte[] imageBytes = Base64.getDecoder().decode(profileImage.get().getImage());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                headers.setCacheControl("max-age=3600"); // Cache for 1 hour

                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            }
        } catch (Exception e) {
            System.err.println("Error serving image: " + e.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}