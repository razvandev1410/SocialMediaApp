package com.example.demo.dto;

import com.example.demo.entity.VoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class VoteRequest {
    @NotNull(message = "User ID can't be null")
    private Long userId;

    private Long postId;
    private Long commentId;

    @NotNull(message = "Vote type can't be null!")
    private VoteType voteType;
}
