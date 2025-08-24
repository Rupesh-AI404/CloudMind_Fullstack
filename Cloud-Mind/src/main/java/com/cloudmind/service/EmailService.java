package com.cloudmind.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPaymentConfirmationEmail(String toEmail, String userName, String planName, String amount, String transactionId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Payment Confirmation - Cloud Mind Subscription");
            helper.setFrom(fromEmail);

            String emailContent = createEmailContent(userName, planName, amount, transactionId);
            helper.setText(emailContent, true);

            mailSender.send(message);
            System.out.println("Payment confirmation email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String createEmailContent(String userName, String planName, String amount, String transactionId) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; }
                    .header { background-color: #6366f1; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; }
                    .success-box { background-color: #d4edda; border: 1px solid #c3e6cb; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .details { background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; background-color: #f8f9fa; }
                    .btn { background-color: #6366f1; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Payment Successful!</h1>
                        <p>Welcome to Cloud Mind Premium</p>
                    </div>
                    
                    <div class="content">
                        <div class="success-box">
                            <h3 style="color: #155724; margin: 0;">✅ Payment Confirmed</h3>
                        </div>
                        
                        <h2>Hello %s,</h2>
                        <p>Thank you for your payment! Your subscription has been activated successfully.</p>
                        
                        <div class="details">
                            <h3>📋 Payment Details:</h3>
                            <p><strong>Plan:</strong> %s</p>
                            <p><strong>Amount:</strong> ₹%s</p>
                            <p><strong>Transaction ID:</strong> %s</p>
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Status:</strong> <span style="color: #28a745;">COMPLETED</span></p>
                        </div>
                        
                        <h3>🚀 What's Next?</h3>
                        <ul>
                            <li>Your premium features are now active</li>
                            <li>Access your dashboard to explore new capabilities</li>
                            <li>Enjoy unlimited access to all features</li>
                        </ul>
                        
                        <div style="text-align: center;">
                            <a href="http://localhost:8080/user-dashboard" class="btn">Access Dashboard</a>
                        </div>
                        
                        <p>If you have any questions, feel free to contact our support team.</p>
                    </div>
                    
                    <div class="footer">
                        <p>Thank you for choosing Cloud Mind!</p>
                        <p><small>This is an automated email. Please do not reply.</small></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, planName, amount, transactionId, currentDate);
    }
}