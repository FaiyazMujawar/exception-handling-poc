package com.rheumera.poc.services;

import org.springframework.stereotype.Service;

import com.rheumera.poc.api.dto.request.AddUser;

@Service
public class UserService {
    public String addUser(AddUser addUser) {
        return "User added successfully with name: %s".formatted(addUser.getName());
    }
}