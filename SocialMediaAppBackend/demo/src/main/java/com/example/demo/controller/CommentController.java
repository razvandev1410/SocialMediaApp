package com.example.demo.controller;

import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/getAll")
    @ResponseBody
    public List<Comment> retrieveAllComments() {
        return this.commentService.retrieveComments();
    }

    @GetMapping("/getById")
    @ResponseBody
    public Comment getCommentById(@RequestParam("id") Long id) {
        return this.commentService.retrieveCommentById(id);
    }

    @GetMapping("/getByPostId")
    @ResponseBody
    public List<Comment> getCommentsByPostId(@RequestParam("postId") Long postId) {
        return this.commentService.retrieveCommentsByPostId(postId);
    }

    @PostMapping("/insertComment")
    @ResponseBody
    public Comment insertComment(@RequestBody Comment comment) {
        return this.commentService.insertComment(comment);
    }

    @PutMapping("/updateComment")
    @ResponseBody
    public Comment updateComment(@RequestBody Comment comment) {
        return this.commentService.updateComment(comment);
    }

    @DeleteMapping("/deleteById")
    @ResponseBody
    public String deleteByCommentId(@RequestParam Long id) {
        return this.commentService.deleteById(id);
    }
}