// DEPRECATED AFTER ASSIGNMENT 1

package com.example.demo.service;

import com.example.demo.dto.PostDTO;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    @Mock
    private UserService userService;
    @Mock
    private TagService tagService;

    @InjectMocks
    private PostService postService;

    private User author;
    private User moderator;
    private User otherUser;
    private Post post;

    @BeforeEach
    void setup() {
        author = new User();
        author.setUserId(1L);
        author.setUsername("author");
        author.setIsBanned(false);
        author.setIsModerator(false);
        author.setScore(0.0);

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
        post.setText("Test content");
        post.setAuthor(author);
        post.setPostStatus(PostStatus.JUST_POSTED);
        post.setVoteCount(0);
    }

    @Test
    void testSearchByTitle_ReturnsResults() {
        when(postRepository.searchByTitle("Test")).thenReturn(List.of(post));

        List<Post> result = postService.searchByTitle("Test");

        assertEquals(1, result.size());
        assertEquals("Test Post", result.get(0).getTitle());
    }

    @Test
    void testSearchByTitle_NoResults() {
        when(postRepository.searchByTitle("nonexistent")).thenReturn(List.of());

        List<Post> result = postService.searchByTitle("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterByTag() {
        when(postRepository.findByTagName("java")).thenReturn(List.of(post));

        List<Post> result = postService.filterByTag("java");

        assertEquals(1, result.size());
    }

    @Test
    void testFilterByTagAndUser() {
        when(postRepository.findByTagNameAndUserId("java", 1L)).thenReturn(List.of(post));

        List<Post> result = postService.filterByTagAndUser("java", 1L);

        assertEquals(1, result.size());
    }

    // ========== POST STATUS ==========

    @Test
    void testSetOutdated_ByAuthor_Success() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.setOutdated(10L, 1L);

        assertEquals(PostStatus.OUTDATED, result.getPostStatus());
    }

    @Test
    void testSetOutdated_NotAuthor_Throws() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThrows(IllegalArgumentException.class, () -> postService.setOutdated(10L, 2L));
    }

    @Test
    void testUpdateStatusToFirstReactions_FromJustPosted() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        postService.updateStatusToFirstReactions(10L);

        assertEquals(PostStatus.FIRST_REACTIONS, post.getPostStatus());
        verify(postRepository).save(post);
    }

    @Test
    void testUpdateStatusToFirstReactions_AlreadyFirstReactions_NoChange() {
        post.setPostStatus(PostStatus.FIRST_REACTIONS);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        postService.updateStatusToFirstReactions(10L);

        assertEquals(PostStatus.FIRST_REACTIONS, post.getPostStatus());
        verify(postRepository, never()).save(any());
    }

    @Test
    void testUpdateStatusToFirstReactions_Outdated_NoChange() {
        post.setPostStatus(PostStatus.OUTDATED);

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        postService.updateStatusToFirstReactions(10L);

        assertEquals(PostStatus.OUTDATED, post.getPostStatus());
        verify(postRepository, never()).save(any());
    }

    // ========== INSERT POST ==========

    @Test
    void testInsertPost_Success() {
        PostDTO dto = new PostDTO();
        dto.setTitle("New Post");
        dto.setText("Content");
        dto.setAuthorId(1L);

        when(userService.findEntityById(1L)).thenReturn(author);
        when(postRepository.save(any(Post.class))).thenAnswer(i -> {
            Post saved = i.getArgument(0);
            saved.setPostId(11L);
            return saved;
        });

        Post result = postService.insertPost(dto);

        assertNotNull(result);
        assertEquals("New Post", result.getTitle());
        assertEquals(PostStatus.JUST_POSTED, result.getPostStatus());
    }

    @Test
    void testInsertPost_WithTags() {
        PostDTO dto = new PostDTO();
        dto.setTitle("Tagged Post");
        dto.setText("Content");
        dto.setAuthorId(1L);
        dto.setTags(List.of("java", "spring"));

        Tag javaTag = new Tag();
        javaTag.setName("java");
        Tag springTag = new Tag();
        springTag.setName("spring");

        when(userService.findEntityById(1L)).thenReturn(author);
        when(tagService.getOrCreateTag("java")).thenReturn(javaTag);
        when(tagService.getOrCreateTag("spring")).thenReturn(springTag);
        when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        Post result = postService.insertPost(dto);

        assertEquals(2, result.getTags().size());
    }

    @Test
    void testInsertPost_BannedUser_Throws() {
        author.setIsBanned(true);

        PostDTO dto = new PostDTO();
        dto.setTitle("Post");
        dto.setAuthorId(1L);

        when(userService.findEntityById(1L)).thenReturn(author);

        assertThrows(IllegalArgumentException.class, () -> postService.insertPost(dto));
    }

    // ========== UPDATE POST ==========

    @Test
    void testUpdatePost_ByAuthor_Success() {
        PostDTO dto = new PostDTO();
        dto.setTitle("Updated Title");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userService.findEntityById(1L)).thenReturn(author);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.updatePost(10L, dto, 1L);

        assertEquals("Updated Title", post.getTitle());
    }

    @Test
    void testUpdatePost_ByModerator_Success() {
        PostDTO dto = new PostDTO();
        dto.setTitle("Mod Updated");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userService.findEntityById(99L)).thenReturn(moderator);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.updatePost(10L, dto, 99L);

        assertEquals("Mod Updated", post.getTitle());
    }

    @Test
    void testUpdatePost_ByOtherUser_Throws() {
        PostDTO dto = new PostDTO();
        dto.setTitle("Hacked");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userService.findEntityById(2L)).thenReturn(otherUser);

        assertThrows(IllegalArgumentException.class, () -> postService.updatePost(10L, dto, 2L));
    }

    @Test
    void testUpdatePost_BannedUser_Throws() {
        author.setIsBanned(true);
        PostDTO dto = new PostDTO();
        dto.setTitle("Update");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userService.findEntityById(1L)).thenReturn(author);

        assertThrows(IllegalArgumentException.class, () -> postService.updatePost(10L, dto, 1L));
    }

    // ========== DELETE POST ==========

    @Test
    void testDeletePost_ByAuthor_Success() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userService.findEntityById(1L)).thenReturn(author);

        postService.deleteById(10L, 1L);

        verify(postRepository).deleteById(10L);
    }

    @Test
    void testDeletePost_ByModerator_Success() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userService.findEntityById(99L)).thenReturn(moderator);

        postService.deleteById(10L, 99L);

        verify(postRepository).deleteById(10L);
    }

    @Test
    void testDeletePost_ByOtherUser_Throws() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userService.findEntityById(2L)).thenReturn(otherUser);

        assertThrows(IllegalArgumentException.class, () -> postService.deleteById(10L, 2L));
    }

    // ========== RETRIEVE ==========

    @Test
    void testRetrievePosts_SortedByDate() {
        Post post2 = new Post();
        post2.setPostId(11L);

        when(postRepository.findAllByOrderByCreationDateDesc()).thenReturn(Arrays.asList(post, post2));

        List<Post> result = postService.retrievePosts();

        assertEquals(2, result.size());
    }

    @Test
    void testRetrievePostById_NotFound_Throws() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.retrievePostById(999L));
    }

    @Test
    void testRetrievePostsByUserId() {
        when(postRepository.findByAuthorUserIdOrderByCreationDateDesc(1L)).thenReturn(List.of(post));

        List<Post> result = postService.retrievePostsByUserId(1L);

        assertEquals(1, result.size());
    }

}

/*
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
*/
