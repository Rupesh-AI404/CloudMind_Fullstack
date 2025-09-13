package com.cloudmind.RestController;


import com.cloudmind.model.ContactMessage;
import com.cloudmind.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contact")
public class ContactRestController {

    @Autowired
    private ContactMessageRepository contactRepo;

    @GetMapping("/getAll")
    public List<ContactMessage> getAllMessages() {
        return contactRepo.findAll();
    }

    @GetMapping("/getById/{id}")
    public Optional<ContactMessage> getMessageById(@PathVariable Long id) {
        return contactRepo.findById(id);
    }

    @PostMapping("/create")
    public ContactMessage createMessage(@RequestBody ContactMessage message) {
        return contactRepo.save(message);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteMessage(@PathVariable Long id) {
        contactRepo.deleteById(id);
        return "Message deleted successfully";
    }
}
