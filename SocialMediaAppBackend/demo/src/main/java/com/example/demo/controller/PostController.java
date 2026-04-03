package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping("/getAll")
    @ResponseBody
    public List<Post> retrieveAllPosts() {
        return this.postService.retrievePosts();
    }

    @GetMapping("/getById")
    @ResponseBody
    public Post retrievePostById(@RequestParam("id") Long postId) {
        return this.postService.retrievePostById(postId);
    }

    @GetMapping("/getByUserId")
    @ResponseBody
    public List<Post> getPostsByUserId(@RequestParam("userId") Long userId) {
        return this.postService.retrievePostsByUserId(userId);
    }

    @PostMapping("/insertPost")
    @ResponseBody
    public Post insertPost(@RequestBody Post post) {
        return this.postService.insertPost(post);
    }

    @PutMapping("/updatePost")
    @ResponseBody
    public Post updatePost(@RequestBody Post post) {
        return this.postService.updatePost(post);
    }

    @DeleteMapping("/deleteById")
    public String deletePostById(@RequestParam Long postId) {
        return this.postService.deleteById(postId);
    }
}
