package com.example.demo.controller;

import com.example.demo.dto.PostDTO;
import com.example.demo.entity.Post;
import com.example.demo.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "*")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping
    public List<Post> retrieveAllPosts() {
        return this.postService.retrievePosts();
    }

    @GetMapping("/{id}")
    public Post retrievePostById(@PathVariable("id") Long postId) {
        return this.postService.retrievePostById(postId);
    }

    @GetMapping("/user/{userId}")
    public List<Post> getPostsByUserId(@PathVariable Long userId) {
        return this.postService.retrievePostsByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<Post> insertPost(@Valid @RequestBody PostDTO dto) {
        Post post = postService.insertPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{id}")
    public Post updatePost(@PathVariable("id") Long postId,
                           @Valid @RequestBody PostDTO dto,
                           @RequestParam Long requestingUserId) {
        return postService.updatePost(postId, dto, requestingUserId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePostById(@PathVariable("id") Long postId,
                                               @RequestParam Long requestingUserId) {
        this.postService.deleteById(postId, requestingUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/outdated")
    public Post markOutdated(@PathVariable("id") Long postId,
                             @RequestParam Long requestingUserId) {
        return postService.setOutdated(postId, requestingUserId);
    }

    @GetMapping("/search")
    public List<Post> searchByTitle(@RequestParam String keyword) {
        return postService.searchByTitle(keyword);
    }

    @GetMapping("/filter/tag")
    public List<Post> filterByTag(@RequestParam String tag,
                                  @RequestParam(required = false) Long userId) {
        if(userId != null) {
            return postService.filterByTagAndUser(tag, userId);
        }
        return postService.filterByTag(tag);
    }

}
