package com.cloudmind.RestController;


import com.cloudmind.model.Subscriber;
import com.cloudmind.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscribers")
public class SubscriberRestController {

    @Autowired
    private SubscriberRepository subscriberRepo;

    @GetMapping("/getAll")
    public List<Subscriber> getAllSubscribers() {
        return subscriberRepo.findAll();
    }

    @GetMapping("/getById/{id}")
    public Optional<Subscriber> getSubscriberById(@PathVariable Long id) {
        return subscriberRepo.findById(id);
    }

    @PostMapping("/create")
    public Subscriber createSubscriber(@RequestBody Subscriber subscriber) {
        return subscriberRepo.save(subscriber);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteSubscriber(@PathVariable Long id) {
        subscriberRepo.deleteById(id);
        return "Subscriber deleted successfully";
    }
}