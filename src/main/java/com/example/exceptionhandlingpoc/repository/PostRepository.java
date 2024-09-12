package com.example.exceptionhandlingpoc.repository;

import com.example.exceptionhandlingpoc.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query(value = "SELECT * FROM posts", nativeQuery = true)
    List<Post> findAllPosts();
}