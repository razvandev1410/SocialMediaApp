package com.example.demo.controller;

import com.example.demo.dto.CommentDTO;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@CrossOrigin(origins = "*")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping
    public List<Comment> retrieveAllComments() {
        return this.commentService.retrieveComments();
    }

    @GetMapping("/{id}")
    public Comment getCommentById(@PathVariable("id") Long id) {
        return this.commentService.retrieveCommentById(id);
    }

    @GetMapping("/post/{postId}")
    public List<Comment> getCommentsByPostId(@PathVariable Long postId) {
        return this.commentService.retrieveCommentsByPostId(postId);
    }

    @PostMapping
    public ResponseEntity<Comment> insertComment(@Valid @RequestBody CommentDTO dto) {
        Comment comment = commentService.insertComment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public Comment updateComment(@PathVariable("id") Long commentId,
                                 @Valid @RequestBody CommentDTO dto,
                                 @RequestParam Long requestingUserId) {
        return this.commentService.updateComment(commentId, dto, requestingUserId);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteByCommentId(@PathVariable("id") Long id,
                                                  @RequestParam Long requestingUserId) {
        commentService.deleteById(id, requestingUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}