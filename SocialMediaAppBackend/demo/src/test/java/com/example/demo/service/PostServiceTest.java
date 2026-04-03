package com.example.demo.service;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
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
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void testRetrievePosts() {
        Post post1 = new Post();
        post1.setPostId(1L);
        Post post2 = new Post();
        post2.setPostId(2L);

        when(postRepository.findAll()).thenReturn(Arrays.asList(post1, post2));

        List<Post> result = postService.retrievePosts();

        assertEquals(2, result.size());
        verify(postRepository, times(1)).findAll();
    }

    @Test
    void testRetrievePostById_Found() {
        Post post = new Post();
        post.setPostId(1L);
        post.setTitle("Test post");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post result = postService.retrievePostById(1L);

        assertNotNull(result);
        assertEquals("Test post", result.getTitle());
    }

    @Test
    void testRetrievePostById_NotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        Post result = postService.retrievePostById(999L);

        assertNull(result);
    }

    @Test
    void testRetrievePostsByUserId() {
        Post post = new Post();
        post.setPostId(1L);

        when(postRepository.findByAuthorUserId(1L)).thenReturn(List.of(post));

        List<Post> result = postService.retrievePostsByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testRetrievePostsByUserId_Empty() {
        when(postRepository.findByAuthorUserId(999L)).thenReturn(List.of());

        List<Post> result = postService.retrievePostsByUserId(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testInsertPost_WithAuthor() {
        User author = new User();
        author.setUserId(1L);
        author.setUsername("Andrei");

        Post post = new Post();
        post.setTitle("New post");
        post.setAuthor(author);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.insertPost(post);

        assertNotNull(result);
        assertEquals("Andrei", result.getAuthor().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }


    @Test
    void testUpdatePost_KeepCreationDate() {
        Instant originalDate = Instant.parse("2026-01-01T10:00:00Z");

        Post existing = new Post();
        existing.setPostId(1L);
        existing.setCreationDate(originalDate);

        User author = new User();
        author.setUserId(1L);

        Post updated = new Post();
        updated.setPostId(1L);
        updated.setTitle("Updated title");
        updated.setAuthor(author);

        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenReturn(updated);

        postService.updatePost(updated);

        assertEquals(originalDate, updated.getCreationDate());
    }

    @Test
    void testUpdatePost_ResolvesAuthor() {
        User author = new User();
        author.setUserId(1L);
        author.setUsername("Andrei");

        Post existing = new Post();
        existing.setPostId(1L);
        existing.setCreationDate(Instant.now());

        Post updated = new Post();
        updated.setPostId(1L);
        updated.setTitle("Updated");
        updated.setAuthor(author);

        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenReturn(updated);

        Post result = postService.updatePost(updated);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteById_Success() {
        when(postRepository.existsById(1L)).thenReturn(true);

        String result = postService.deleteById(1L);

        assertEquals("Post deletion succesful", result);
        verify(postRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound() {
        when(postRepository.existsById(999L)).thenReturn(false);

        String result = postService.deleteById(999L);

        assertEquals("Post doesn't exist", result);
        verify(postRepository, never()).deleteById(999L);
    }
}