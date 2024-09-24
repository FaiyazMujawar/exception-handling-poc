package com.rheumera.poc.api.dto.request;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.rheumera.poc.models.Post}
 */
@Value
public class PostDto implements Serializable {
    int id;
    String title;
    String content;
}