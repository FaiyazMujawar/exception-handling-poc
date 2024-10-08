package com.rheumera.poc.api.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.rheumera.poc.api.dto.request.AddUser;
import com.rheumera.poc.api.dto.request.PostDto;
import com.rheumera.poc.models.Post;
import com.rheumera.poc.services.PostService;
import com.rheumera.poc.services.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final UserService userService;
    private final PostService postService;

    @GetMapping(path = "/greet")
    public String greet(@RequestParam(value = "name") String name) {
        return "Welcome, %s!".formatted(name);
    }

    @PostMapping(path = "/add-user")
    public String post(@RequestBody @Valid AddUser user) {
        log.info("# HomeController::post ended #");
        var response = userService.addUser(user);
        log.info("# HomeController::post ended #");
        return response;
    }

    @PostMapping(path = "/add-post")
    public String addPost(@RequestBody PostDto postDto) {
        log.info("# HomeController::addPost started #");
        var response = postService.addPost(postDto);
        log.info("# HomeController::addPost ended #");
        return response;
    }

    @GetMapping(path = "/posts")
    public List<Post> getPosts() {
        return postService.getPosts();
    }

    @GetMapping(path = "/throw")
    public Post getPost() {
        throw new RuntimeException("test");
    }
}