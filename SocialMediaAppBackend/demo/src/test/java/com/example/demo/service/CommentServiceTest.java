package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void testRetrieveComments() {
        Comment c1 = new Comment();
        c1.setCommentId(1L);
        Comment c2 = new Comment();
        c2.setCommentId(2L);

        when(commentRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<Comment> result = commentService.retrieveComments();

        assertEquals(2, result.size());
        verify(commentRepository, times(1)).findAll();
    }

    @Test
    void testRetrieveCommentById_Found() {
        Comment comment = new Comment();
        comment.setCommentId(1L);
        comment.setText("Test comment");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Comment result = commentService.retrieveCommentById(1L);

        assertNotNull(result);
        assertEquals("Test comment", result.getText());
    }

    @Test
    void testRetrieveCommentById_NotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        Comment result = commentService.retrieveCommentById(999L);

        assertNull(result);
    }

    @Test
    void testRetrieveCommentsByPostId() {
        Comment comment = new Comment();
        comment.setCommentId(1L);

        when(commentRepository.findByPostPostId(1L)).thenReturn(List.of(comment));

        List<Comment> result = commentService.retrieveCommentsByPostId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testRetrieveCommentsByPostId_Empty() {
        when(commentRepository.findByPostPostId(999L)).thenReturn(List.of());

        List<Comment> result = commentService.retrieveCommentsByPostId(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testInsertComment_WithAuthorAndPost() {
        User author = new User();
        author.setUserId(1L);
        author.setUsername("Andrei");

        Post post = new Post();
        post.setPostId(1L);
        post.setTitle("Test post");

        Comment comment = new Comment();
        comment.setText("Nice post!");
        comment.setAuthor(author);
        comment.setPost(post);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.insertComment(comment);

        assertNotNull(result);
        assertEquals("Andrei", result.getAuthor().getUsername());
        assertEquals("Test post", result.getPost().getTitle());
    }


    @Test
    void testUpdateComment_KeepCreationDate() {
        Instant originalDate = Instant.parse("2026-01-01T10:00:00Z");

        Comment existing = new Comment();
        existing.setCommentId(1L);
        existing.setCreationDate(originalDate);

        User author = new User();
        author.setUserId(1L);

        Post post = new Post();
        post.setPostId(1L);

        Comment updated = new Comment();
        updated.setCommentId(1L);
        updated.setText("Updated text");
        updated.setAuthor(author);
        updated.setPost(post);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(updated);

        commentService.updateComment(updated);

        assertEquals(originalDate, updated.getCreationDate());
    }

    @Test
    void testDeleteById_Success() {
        when(commentRepository.existsById(1L)).thenReturn(true);

        String result = commentService.deleteById(1L);

        assertEquals("Comment deletion successful", result);
        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound() {
        when(commentRepository.existsById(999L)).thenReturn(false);

        String result = commentService.deleteById(999L);

        assertEquals("Comment doesn't exist", result);
        verify(commentRepository, never()).deleteById(999L);
    }
}