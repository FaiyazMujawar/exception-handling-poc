package com.example.exceptionhandlingpoc.api.controllers;

import com.example.exceptionhandlingpoc.api.dto.request.AddUser;
import com.example.exceptionhandlingpoc.api.dto.request.PostDto;
import com.example.exceptionhandlingpoc.models.Post;
import com.example.exceptionhandlingpoc.repository.PostRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
public class HomeController {
    private final PostRepository postRepository;

    @GetMapping(path = "/greet")
    public String greet(@RequestParam(value = "name") String name) {
        return "Welcome, %s!".formatted(name);
    }

    @PostMapping(path = "/add-user")
    public String post(@RequestBody @Valid AddUser user) {
        return "User added successfully with name: %s".formatted(user.getName());
    }

    @PostMapping(path = "/add-post")
    public String addPost(@RequestBody PostDto postDto) {
        var post = new Post();
        post.setId(postDto.getId());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        postRepository.save(post);
        return "Saved post: %s".formatted(post.getId());
    }

    @GetMapping(path = "/posts")
    public List<Post> getPosts() {
        return postRepository.findAllPosts();
    }
}