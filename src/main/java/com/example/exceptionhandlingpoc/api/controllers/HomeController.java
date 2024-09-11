package com.example.exceptionhandlingpoc.api.controllers;

import com.example.exceptionhandlingpoc.api.dto.request.AddUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/")
public class HomeController {
    @GetMapping(path = "/greet")
    public String greet(@RequestParam(value = "name") String name) {
        return "Welcome, %s!".formatted(name);
    }

    @PostMapping(path = "/add-user")
    public String post(@RequestBody @Valid AddUser user) {
        return "User added successfully with name: %s".formatted(user.getName());
    }
}