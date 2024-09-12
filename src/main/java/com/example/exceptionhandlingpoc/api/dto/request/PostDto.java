package com.example.exceptionhandlingpoc.api.dto.request;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.example.exceptionhandlingpoc.models.Post}
 */
@Value
public class PostDto implements Serializable {
    int id;
    String title;
    String content;
}