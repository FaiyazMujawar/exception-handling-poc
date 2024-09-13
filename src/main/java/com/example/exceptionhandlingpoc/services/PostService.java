package com.example.exceptionhandlingpoc.services;

import com.example.exceptionhandlingpoc.api.dto.request.PostDto;
import com.example.exceptionhandlingpoc.models.Post;
import com.example.exceptionhandlingpoc.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;

    public String addPost(PostDto postDto) {
        log.info("# PostService::addPost started #");
        var post = new Post();
        post.setId(postDto.getId());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        postRepository.save(post);
        log.info("# PostService::addPost ended #");
        return "Saved post: %s".formatted(post.getId());
    }

    public List<Post> getPosts() {
        return postRepository.findAllPosts();
    }
}