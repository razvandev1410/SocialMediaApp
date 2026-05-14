package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor @NoArgsConstructor
public class PostDTO {
    @NotBlank(message = "Title can't be null / blank!")
    private String title;

    @NotBlank(message = "Text can't be null / blank!")
    private String text;

    private String imageUrl;

    @NotNull(message = "Author ID can't be null")
    private Long authorId;

    private List<String> tags;
}
