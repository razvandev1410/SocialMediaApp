package com.example.demo.service;

import com.example.demo.dto.VoteRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class VoteService {
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;

    @Transactional
    public Vote voteOnPost(VoteRequest request) {
        if(request.getPostId() == null) {
            throw new IllegalArgumentException("Post ID is required for voting on a post!");
        }

        User voter = userService.findEntityById(request.getUserId());
        Post post = postService.retrievePostById(request.getPostId());

        if(voter.getIsBanned()) {
            throw new IllegalArgumentException("Banned users can't do this!");
        }

        if(post.getAuthor().getUserId().equals(voter.getUserId())) {
            throw new IllegalArgumentException("You can't vote on your own post!");
        }

        Optional<Vote> existingVote = voteRepository.findByUserUserIdAndPostPostId(request.getUserId(), request.getPostId());

        if(existingVote.isPresent()) {
            throw new IllegalArgumentException("You have already voted on this post!");
        }

        Vote vote = new Vote();
        vote.setUser(voter);
        vote.setPost(post);
        vote.setVoteType(request.getVoteType());
        Vote saved = voteRepository.save(vote);

        long upvotes = voteRepository.countByPostPostIdAndVoteType(request.getPostId(), VoteType.UPVOTE);
        long downvotes = voteRepository.countByPostPostIdAndVoteType(request.getPostId(), VoteType.DOWNVOTE);
        int newCount = (int) (upvotes - downvotes);
        postService.updateVoteCount(request.getPostId(), newCount);

        Long authorId = post.getAuthor().getUserId();
        if(request.getVoteType() == VoteType.UPVOTE) {
            userService.updateScore(authorId, 2.5);
        }
        else {
            userService.updateScore(authorId, -1.5);
            userService.updateScore(voter.getUserId(), -1.5);
        }

        return saved;
    }

    @Transactional
    public Vote voteOnComment(VoteRequest request) {
        if(request.getCommentId() == null) {
            throw new IllegalArgumentException("Comment ID is required for voting on a comment!");
        }
        User voter = userService.findEntityById(request.getUserId());
        Comment comment = commentService.retrieveCommentById(request.getCommentId());

        if(voter.getIsBanned()) {
            throw new IllegalArgumentException("Banned users can't do this!");
        }

        if(comment.getAuthor().getUserId().equals(voter.getUserId())) {
            throw new IllegalArgumentException("You can't vote on your own comment!");
        }

        Optional<Vote> existingVote = voteRepository.findByUserUserIdAndCommentCommentId(request.getUserId(), request.getCommentId());

        if(existingVote.isPresent()) {
            throw new IllegalArgumentException("You have already voted on this comment!");
        }

        Vote vote = new Vote();
        vote.setUser(voter);
        vote.setComment(comment);
        vote.setVoteType(request.getVoteType());
        Vote saved = voteRepository.save(vote);

        long upvotes = voteRepository.countByCommentCommentIdAndVoteType(request.getCommentId(), VoteType.UPVOTE);
        long downvotes = voteRepository.countByCommentCommentIdAndVoteType(request.getCommentId(), VoteType.DOWNVOTE);

        int newCount = (int)(upvotes - downvotes);
        commentService.updateVoteCount(request.getCommentId(), newCount);

        Long authorId = comment.getAuthor().getUserId();
        if(request.getVoteType() == VoteType.UPVOTE) {
            userService.updateScore(authorId, 5.0);
        }
        else {
            userService.updateScore(authorId, -2.5);
            userService.updateScore(voter.getUserId(), -1.5);
        }

    return saved;
    }



}
