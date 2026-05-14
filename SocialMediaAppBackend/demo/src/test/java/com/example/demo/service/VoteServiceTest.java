package com.example.demo.service;

import com.example.demo.dto.VoteRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;
    @Mock
    private UserService userService;
    @Mock
    private PostService postService;
    @Mock
    private CommentService commentService;

    @InjectMocks
    private VoteService voteService;

    private User voter;
    private User postAuthor;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        voter = new User();
        voter.setUserId(1L);
        voter.setUsername("voter");
        voter.setIsBanned(false);
        voter.setScore(0.0);

        postAuthor = new User();
        postAuthor.setUserId(2L);
        postAuthor.setUsername("author");
        postAuthor.setIsBanned(false);
        postAuthor.setScore(0.0);

        post = new Post();
        post.setPostId(10L);
        post.setAuthor(postAuthor);
        post.setVoteCount(0);

        comment = new Comment();
        comment.setCommentId(20L);
        comment.setAuthor(postAuthor);
        comment.setPost(post);
        comment.setVoteCount(0);
    }

    @Test
    void testVoteOnPost_Upvote_Success() {
        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setPostId(10L);
        request.setVoteType(VoteType.UPVOTE);

        when(userService.findEntityById(1L)).thenReturn(voter);
        when(postService.retrievePostById(10L)).thenReturn(post);
        when(voteRepository.findByUserUserIdAndPostPostId(1L, 10L)).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenAnswer(i -> i.getArgument(0));
        when(voteRepository.countByPostPostIdAndVoteType(10L, VoteType.UPVOTE)).thenReturn(1L);
        when(voteRepository.countByPostPostIdAndVoteType(10L, VoteType.DOWNVOTE)).thenReturn(0L);

        Vote result = voteService.voteOnPost(request);

        assertNotNull(result);
        assertEquals(VoteType.UPVOTE, result.getVoteType());
        verify(userService).updateScore(2L, 2.5);   // author gets +2.5
        verify(postService).updateVoteCount(10L, 1);
    }

    @Test
    void testVoteOnPost_Downvote_ScoreUpdates() {
        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setPostId(10L);
        request.setVoteType(VoteType.DOWNVOTE);

        when(userService.findEntityById(1L)).thenReturn(voter);
        when(postService.retrievePostById(10L)).thenReturn(post);
        when(voteRepository.findByUserUserIdAndPostPostId(1L, 10L)).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenAnswer(i -> i.getArgument(0));
        when(voteRepository.countByPostPostIdAndVoteType(10L, VoteType.UPVOTE)).thenReturn(0L);
        when(voteRepository.countByPostPostIdAndVoteType(10L, VoteType.DOWNVOTE)).thenReturn(1L);

        voteService.voteOnPost(request);

        verify(userService).updateScore(2L, -1.5);  // author gets -1.5
        verify(userService).updateScore(1L, -1.5);  // voter gets -1.5 penalty
        verify(postService).updateVoteCount(10L, -1);
    }

    @Test
    void testVoteOnPost_SelfVote_Throws() {
        post.setAuthor(voter); // voter is also author

        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setPostId(10L);
        request.setVoteType(VoteType.UPVOTE);

        when(userService.findEntityById(1L)).thenReturn(voter);
        when(postService.retrievePostById(10L)).thenReturn(post);

        assertThrows(IllegalArgumentException.class, () -> voteService.voteOnPost(request));
    }

    @Test
    void testVoteOnPost_DuplicateVote_Throws() {
        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setPostId(10L);
        request.setVoteType(VoteType.UPVOTE);

        Vote existing = new Vote();
        when(userService.findEntityById(1L)).thenReturn(voter);
        when(postService.retrievePostById(10L)).thenReturn(post);
        when(voteRepository.findByUserUserIdAndPostPostId(1L, 10L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> voteService.voteOnPost(request));
    }

    @Test
    void testVoteOnPost_BannedUser_Throws() {
        voter.setIsBanned(true);

        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setPostId(10L);
        request.setVoteType(VoteType.UPVOTE);

        when(userService.findEntityById(1L)).thenReturn(voter);
        when(postService.retrievePostById(10L)).thenReturn(post);

        assertThrows(IllegalArgumentException.class, () -> voteService.voteOnPost(request));
    }

    @Test
    void testVoteOnPost_NullPostId_Throws() {
        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setPostId(null);
        request.setVoteType(VoteType.UPVOTE);

        assertThrows(IllegalArgumentException.class, () -> voteService.voteOnPost(request));
    }

    @Test
    void testVoteOnComment_Upvote_Success() {
        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setCommentId(20L);
        request.setVoteType(VoteType.UPVOTE);

        when(userService.findEntityById(1L)).thenReturn(voter);
        when(commentService.retrieveCommentById(20L)).thenReturn(comment);
        when(voteRepository.findByUserUserIdAndCommentCommentId(1L, 20L)).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenAnswer(i -> i.getArgument(0));
        when(voteRepository.countByCommentCommentIdAndVoteType(20L, VoteType.UPVOTE)).thenReturn(1L);
        when(voteRepository.countByCommentCommentIdAndVoteType(20L, VoteType.DOWNVOTE)).thenReturn(0L);

        Vote result = voteService.voteOnComment(request);

        assertNotNull(result);
        verify(userService).updateScore(2L, 5.0);   // comment author gets +5
    }

    @Test
    void testVoteOnComment_Downvote_ScoreUpdates() {
        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setCommentId(20L);
        request.setVoteType(VoteType.DOWNVOTE);

        when(userService.findEntityById(1L)).thenReturn(voter);
        when(commentService.retrieveCommentById(20L)).thenReturn(comment);
        when(voteRepository.findByUserUserIdAndCommentCommentId(1L, 20L)).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenAnswer(i -> i.getArgument(0));
        when(voteRepository.countByCommentCommentIdAndVoteType(20L, VoteType.UPVOTE)).thenReturn(0L);
        when(voteRepository.countByCommentCommentIdAndVoteType(20L, VoteType.DOWNVOTE)).thenReturn(1L);

        voteService.voteOnComment(request);

        verify(userService).updateScore(2L, -2.5);  // comment author gets -2.5
        verify(userService).updateScore(1L, -1.5);  // voter gets -1.5
    }

    @Test
    void testVoteOnComment_SelfVote_Throws() {
        comment.setAuthor(voter);

        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setCommentId(20L);
        request.setVoteType(VoteType.UPVOTE);

        when(userService.findEntityById(1L)).thenReturn(voter);
        when(commentService.retrieveCommentById(20L)).thenReturn(comment);

        assertThrows(IllegalArgumentException.class, () -> voteService.voteOnComment(request));
    }

    @Test
    void testVoteOnComment_DuplicateVote_Throws() {
        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setCommentId(20L);
        request.setVoteType(VoteType.UPVOTE);

        when(userService.findEntityById(1L)).thenReturn(voter);
        when(commentService.retrieveCommentById(20L)).thenReturn(comment);
        when(voteRepository.findByUserUserIdAndCommentCommentId(1L, 20L)).thenReturn(Optional.of(new Vote()));

        assertThrows(IllegalArgumentException.class, () -> voteService.voteOnComment(request));
    }

    @Test
    void testVoteOnComment_NullCommentId_Throws() {
        VoteRequest request = new VoteRequest();
        request.setUserId(1L);
        request.setCommentId(null);
        request.setVoteType(VoteType.UPVOTE);

        assertThrows(IllegalArgumentException.class, () -> voteService.voteOnComment(request));
    }
}
