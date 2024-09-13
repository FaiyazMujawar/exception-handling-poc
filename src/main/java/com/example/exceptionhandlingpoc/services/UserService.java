package com.example.exceptionhandlingpoc.services;

import com.example.exceptionhandlingpoc.api.dto.request.AddUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public String addUser(AddUser addUser) {
        return "User added successfully with name: %s".formatted(addUser.getName());
    }
}