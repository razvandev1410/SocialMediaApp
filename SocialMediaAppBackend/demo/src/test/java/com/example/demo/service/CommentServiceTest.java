// DEPRECATED AFTER ASSIGNMENT 1

package com.example.demo.service;

import com.example.demo.dto.CommentDTO;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
    @Mock
    private PostService postService;
    @Mock
    private UserService userService;

    @InjectMocks
    private CommentService commentService;

    private User author;
    private User moderator;
    private User otherUser;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setUserId(1L);
        author.setUsername("commenter");
        author.setIsBanned(false);
        author.setIsModerator(false);

        moderator = new User();
        moderator.setUserId(99L);
        moderator.setUsername("mod");
        moderator.setIsBanned(false);
        moderator.setIsModerator(true);

        otherUser = new User();
        otherUser.setUserId(2L);
        otherUser.setUsername("other");
        otherUser.setIsBanned(false);
        otherUser.setIsModerator(false);

        post = new Post();
        post.setPostId(10L);
        post.setTitle("Test Post");
        post.setPostStatus(PostStatus.JUST_POSTED);
        post.setAuthor(author);

        comment = new Comment();
        comment.setCommentId(20L);
        comment.setText("Test comment");
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setVoteCount(0);
    }

    // ========== INSERT COMMENT ==========

    @Test
    void testInsertComment_Success() {
        CommentDTO dto = new CommentDTO();
        dto.setPostId(10L);
        dto.setAuthorId(1L);
        dto.setText("Great post!");

        when(postService.retrievePostById(10L)).thenReturn(post);
        when(userService.findEntityById(1L)).thenReturn(author);
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> {
            Comment saved = i.getArgument(0);
            saved.setCommentId(21L);
            return saved;
        });

        Comment result = commentService.insertComment(dto);

        assertNotNull(result);
        assertEquals("Great post!", result.getText());
        verify(postService).updateStatusToFirstReactions(10L);
    }

    @Test
    void testInsertComment_OutdatedPost_Throws() {
        post.setPostStatus(PostStatus.OUTDATED);

        CommentDTO dto = new CommentDTO();
        dto.setPostId(10L);
        dto.setAuthorId(1L);
        dto.setText("Late comment");

        when(postService.retrievePostById(10L)).thenReturn(post);

        assertThrows(IllegalArgumentException.class, () -> commentService.insertComment(dto));
    }

    @Test
    void testInsertComment_BannedUser_Throws() {
        author.setIsBanned(true);

        CommentDTO dto = new CommentDTO();
        dto.setPostId(10L);
        dto.setAuthorId(1L);
        dto.setText("Banned comment");

        when(postService.retrievePostById(10L)).thenReturn(post);
        when(userService.findEntityById(1L)).thenReturn(author);

        assertThrows(IllegalArgumentException.class, () -> commentService.insertComment(dto));
    }

    @Test
    void testInsertComment_TriggersFirstReactions() {
        CommentDTO dto = new CommentDTO();
        dto.setPostId(10L);
        dto.setAuthorId(1L);
        dto.setText("First comment!");

        when(postService.retrievePostById(10L)).thenReturn(post);
        when(userService.findEntityById(1L)).thenReturn(author);
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

        commentService.insertComment(dto);

        verify(postService).updateStatusToFirstReactions(10L);
    }

    // ========== UPDATE COMMENT ==========

    @Test
    void testUpdateComment_ByAuthor_Success() {
        CommentDTO dto = new CommentDTO();
        dto.setText("Updated text");

        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(userService.findEntityById(1L)).thenReturn(author);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.updateComment(20L, dto, 1L);

        assertEquals("Updated text", comment.getText());
    }

    @Test
    void testUpdateComment_ByModerator_Success() {
        CommentDTO dto = new CommentDTO();
        dto.setText("Mod edited");

        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(userService.findEntityById(99L)).thenReturn(moderator);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.updateComment(20L, dto, 99L);

        assertEquals("Mod edited", comment.getText());
    }

    @Test
    void testUpdateComment_ByOtherUser_Throws() {
        CommentDTO dto = new CommentDTO();
        dto.setText("Hacked");

        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(userService.findEntityById(2L)).thenReturn(otherUser);

        assertThrows(IllegalArgumentException.class,
                () -> commentService.updateComment(20L, dto, 2L));
    }

    @Test
    void testUpdateComment_BannedUser_Throws() {
        author.setIsBanned(true);
        CommentDTO dto = new CommentDTO();
        dto.setText("Update");

        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(userService.findEntityById(1L)).thenReturn(author);

        assertThrows(IllegalArgumentException.class,
                () -> commentService.updateComment(20L, dto, 1L));
    }

    // ========== DELETE COMMENT ==========

    @Test
    void testDeleteComment_ByAuthor_Success() {
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(userService.findEntityById(1L)).thenReturn(author);

        commentService.deleteById(20L, 1L);

        verify(commentRepository).deleteById(20L);
    }

    @Test
    void testDeleteComment_ByModerator_Success() {
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(userService.findEntityById(99L)).thenReturn(moderator);

        commentService.deleteById(20L, 99L);

        verify(commentRepository).deleteById(20L);
    }

    @Test
    void testDeleteComment_ByOtherUser_Throws() {
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(userService.findEntityById(2L)).thenReturn(otherUser);

        assertThrows(IllegalArgumentException.class,
                () -> commentService.deleteById(20L, 2L));
    }

    // ========== RETRIEVE ==========

    @Test
    void testRetrieveCommentsByPostId() {
        when(commentRepository.findByPostPostIdOrderByVoteCountDesc(10L)).thenReturn(List.of(comment));

        List<Comment> result = commentService.retrieveCommentsByPostId(10L);

        assertEquals(1, result.size());
    }

    @Test
    void testRetrieveCommentsByPostId_Empty() {
        when(commentRepository.findByPostPostIdOrderByVoteCountDesc(999L)).thenReturn(List.of());

        List<Comment> result = commentService.retrieveCommentsByPostId(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testRetrieveCommentById_Found() {
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));

        Comment result = commentService.retrieveCommentById(20L);

        assertNotNull(result);
        assertEquals("Test comment", result.getText());
    }

    @Test
    void testRetrieveCommentById_NotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        Comment result = commentService.retrieveCommentById(999L);

        assertNull(result);
    }

    // ========== VOTE COUNT ==========

    @Test
    void testUpdateVoteCount() {
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        commentService.updateVoteCount(20L, 5);

        assertEquals(5, comment.getVoteCount());
    }
}


/*

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
 */