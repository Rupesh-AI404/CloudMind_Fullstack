package com.cloudmind.controller;

import com.cloudmind.repository.SubscriberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/upgrade-payment")
public class UpgradePaymentController {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @GetMapping("/upgrade")
    public String showUpgradePayment(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Map<String, Object> upgradeDetails = (Map<String, Object>) session.getAttribute("upgradeDetails");

        if (upgradeDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Upgrade session expired");
            return "redirect:/user-dashboard";
        }

        // Add upgrade details to model for the existing payment page
        model.addAttribute("upgradeDetails", upgradeDetails);
        model.addAttribute("isUpgrade", true);
        model.addAttribute("amount", upgradeDetails.get("upgradePrice"));
        model.addAttribute("planName", upgradeDetails.get("newPlan"));

        // Use your existing payment page
        return "payment";
    }
}