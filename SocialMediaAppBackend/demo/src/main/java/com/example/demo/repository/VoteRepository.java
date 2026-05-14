package com.example.demo.repository;

import com.example.demo.entity.Vote;
import com.example.demo.entity.VoteType;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VoteRepository extends CrudRepository<Vote, Long> {
    Optional<Vote> findByUserUserIdAndPostPostId(Long userId, Long postId);
    Optional<Vote> findByUserUserIdAndCommentCommentId(Long userId, Long commentId);
    long countByPostPostIdAndVoteType(Long postId, VoteType voteType);
    long countByCommentCommentIdAndVoteType(Long commentId, VoteType voteType);
}
