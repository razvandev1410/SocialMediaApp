package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class CommentDTO {
    @NotBlank(message = "Texct can't be null / blank")
    private String text;

    private String imageUrl;

    @NotNull(message = "Author ID can't be null")
    private Long authorId;

    @NotNull(message = "Post ID can't be null")
    private Long postId;
}
