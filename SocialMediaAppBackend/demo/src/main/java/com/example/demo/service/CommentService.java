package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    public List<Comment> retrieveComments() {
        return (List<Comment>) this.commentRepository.findAll();
    }

    public Comment retrieveCommentById(Long commentId) {
        Optional<Comment> comment = this.commentRepository.findById(commentId);
        if(comment.isPresent())
            return comment.get();
        else
            return null;
    }

    public List<Comment> retrieveCommentsByPostId(Long postId) {
        return this.commentRepository.findByPostPostId(postId);
    }

    public Comment insertComment(Comment comment) {
        if (comment.getAuthor() == null || comment.getAuthor().getUserId() == null)
            throw new RuntimeException("Author is required!");
        if (comment.getPost() == null || comment.getPost().getPostId() == null)
            throw new RuntimeException("Post is required!");

        Optional<User> authorOptional = userRepository.findById(comment.getAuthor().getUserId());
        if (authorOptional.isPresent()) {
            comment.setAuthor(authorOptional.get());
        } else {
            throw new RuntimeException("User not found!");
        }

        Optional<Post> postOptional = postRepository.findById(comment.getPost().getPostId());
        if (postOptional.isPresent()) {
            comment.setPost(postOptional.get());
        } else {
            throw new RuntimeException("Post not found!");
        }

        return this.commentRepository.save(comment);
    }

    public Comment updateComment(Comment comment) {
        if (comment.getCommentId() != null) {
            Optional<Comment> existingOptional = commentRepository.findById(comment.getCommentId());
            if (existingOptional.isPresent()) {
                Comment existing = existingOptional.get();
                comment.setCreationDate(existing.getCreationDate());
            }
        }

        if (comment.getAuthor() != null && comment.getAuthor().getUserId() != null) {
            Optional<User> authorOptional = userRepository.findById(comment.getAuthor().getUserId());
            if (authorOptional.isPresent()) {
                comment.setAuthor(authorOptional.get());
            } else {
                throw new RuntimeException("User not found!");
            }
        }

        if (comment.getPost() != null && comment.getPost().getPostId() != null) {
            Optional<Post> postOptional = postRepository.findById(comment.getPost().getPostId());
            if (postOptional.isPresent()) {
                comment.setPost(postOptional.get());
            } else {
                throw new RuntimeException("Post not found!");
            }
        }

        return this.commentRepository.save(comment);
    }


    public String deleteById(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            return "Comment doesn't exist";
        }

        try {
            this.commentRepository.deleteById(commentId);
        }
        catch(Exception e) {
            return "Failed deleting comment " + commentId;
        }
        return "Comment deletion successful";
    }


}
