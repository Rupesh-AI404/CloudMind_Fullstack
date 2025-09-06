package com.cloudmind.controller;

import com.cloudmind.model.ContactMessage;
import com.cloudmind.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ContactController {

    @Autowired
    private ContactMessageRepository contactRepo;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/contact")
    public String contactPage(Model model) {
        model.addAttribute("contactMessage", new ContactMessage());
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContactForm(@ModelAttribute ContactMessage contactMessage,
                                    RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== CONTACT FORM SUBMISSION ===");
            System.out.println("Name: " + contactMessage.getName());
            System.out.println("Email: " + contactMessage.getEmail());
            System.out.println("Subject: " + contactMessage.getSubject());

            // Set defaults
            if (contactMessage.getPriority() == null || contactMessage.getPriority().isEmpty()) {
                contactMessage.setPriority("medium");
            }
            contactMessage.setIsRead(false);
            contactMessage.setCreatedAt(LocalDateTime.now());

            // Save to database
            ContactMessage savedMessage = contactRepo.save(contactMessage);
            System.out.println("✅ Message saved with ID: " + savedMessage.getId());

            // Send emails
            boolean adminEmailSent = sendAdminNotification(contactMessage);
            boolean userEmailSent = sendUserConfirmation(contactMessage);

            // Success message with email status
            String successMsg = "🎉 Thank you " + contactMessage.getName() + "! Your message has been sent successfully.";
            if (adminEmailSent && userEmailSent) {
                successMsg += " Confirmation emails have been sent.";
            } else if (userEmailSent) {
                successMsg += " A confirmation email has been sent to you.";
            } else {
                successMsg += " We'll get back to you soon!";
            }

            redirectAttributes.addFlashAttribute("successMessage", successMsg);
            return "redirect:/contact";

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Sorry, there was an error sending your message. Please try again.");
            return "redirect:/contact";
        }
    }

    private boolean sendAdminNotification(ContactMessage message) {
        try {
            SimpleMailMessage adminEmail = new SimpleMailMessage();
            adminEmail.setTo("sabiraventures1@gmail.com"); // Your admin email
            adminEmail.setSubject("🔔 New Contact Message: " + message.getSubject());
            adminEmail.setText(buildAdminEmailContent(message));

            mailSender.send(adminEmail);
            System.out.println("✅ Admin notification sent");
            return true;
        } catch (Exception e) {
            System.out.println("⚠️ Failed to send admin notification: " + e.getMessage());
            return false;
        }
    }

    private boolean sendUserConfirmation(ContactMessage message) {
        try {
            SimpleMailMessage userEmail = new SimpleMailMessage();
            userEmail.setTo(message.getEmail());
            userEmail.setSubject("✅ Message Received - Cloud Mind");
            userEmail.setText(buildUserConfirmationContent(message));

            mailSender.send(userEmail);
            System.out.println("✅ User confirmation sent to: " + message.getEmail());
            return true;
        } catch (Exception e) {
            System.out.println("⚠️ Failed to send user confirmation: " + e.getMessage());
            return false;
        }
    }

    private String buildAdminEmailContent(ContactMessage message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        return String.format("""
            New Contact Message Received
            ============================
            
            From: %s
            Email: %s
            Subject: %s
            Service: %s
            Received: %s
            
            Message:
            %s
            
            ============================
            Please respond to this inquiry promptly.
            
            You can manage all contact messages in your admin dashboard.
            """,
                message.getName(),
                message.getEmail(),
                message.getSubject(),
                message.getPriority().toUpperCase(),
                message.getCreatedAt().format(formatter),
                message.getMessage()
        );
    }

    private String buildUserConfirmationContent(ContactMessage message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        return String.format("""
            Dear %s,
            
            Thank you for contacting Cloud Mind! We have successfully received your message.
            
            ✅ Your Message Details:
            • Subject: %s
            • Service: %s
            • Received: %s
            
            What happens next?
            • Our team will review your message within 24 hours
            • You'll receive a personalized response within 1-2 business days
            • For urgent matters, please call us at +977 9863038840
            
            In the meantime, feel free to explore our services at our website.
            
            Best regards,
            The Cloud Mind Team
            
            ---
            Cloud Mind - Digital Marketing Solutions
            Email: cloudmind45@gmail.com
            Phone: +977 9863038840
            Website: https://cloudmind1.netlify.app/
            """,
                message.getName(),
                message.getSubject(),
                message.getPriority().toUpperCase(),
                message.getCreatedAt().format(formatter)
        );
    }

    // AJAX endpoints for admin dashboard
    @PostMapping("/admin/contact/mark-read/{id}")
    @ResponseBody
    public String markAsRead(@PathVariable Long id) {
        try {
            ContactMessage message = contactRepo.findById(id).orElse(null);
            if (message != null) {
                message.setIsRead(true);
                contactRepo.save(message);
                System.out.println("✅ Message marked as read: ID=" + id);
                return "success";
            }
            return "not-found";
        } catch (Exception e) {
            System.out.println("❌ Error marking message as read: " + e.getMessage());
            return "error";
        }
    }

    @DeleteMapping("/admin/contact/delete/{id}")
    @ResponseBody
    public String deleteMessage(@PathVariable Long id) {
        try {
            contactRepo.deleteById(id);
            System.out.println("✅ Message deleted: ID=" + id);
            return "success";
        } catch (Exception e) {
            System.out.println("❌ Error deleting message: " + e.getMessage());
            return "error";
        }
    }
}