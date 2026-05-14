package com.example.demo.service;

import com.example.demo.dto.CommentDTO;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
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
    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;

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
        return this.commentRepository.findByPostPostIdOrderByVoteCountDesc(postId);
    }

    public Comment insertComment(CommentDTO dto) {
        Post post = postService.retrievePostById(dto.getPostId());

        if(post.getPostStatus() == PostStatus.OUTDATED) {
            throw new IllegalArgumentException("Can't comment on an outdated post!");
        }

        User author = userService.findEntityById(dto.getAuthorId());
        if(author.getIsBanned()) {
            throw new IllegalArgumentException("Banned users can't do this!");
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setImageUrl(dto.getImageUrl());
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setVoteCount(0);

        Comment saved = commentRepository.save(comment);

        postService.updateStatusToFirstReactions(post.getPostId());

        return saved;
    }

    public Comment updateComment(Long commentId, CommentDTO dto, Long requestingUserId) {
        Comment existing = retrieveCommentById(commentId);
        User requester = userService.findEntityById(requestingUserId);

        if(requester.getIsBanned()) {
            throw new IllegalArgumentException("Banned users can't do this!");
        }

        if(!existing.getAuthor().getUserId().equals(requestingUserId) && !requester.getIsModerator()) {
            throw new IllegalArgumentException("Only the author or a moderator can edit this comment");
        }

        if(dto.getText() != null)
            existing.setText(dto.getText());
        if(dto.getImageUrl() != null)
            existing.setImageUrl(dto.getImageUrl());

        return commentRepository.save(existing);
    }


    public void deleteById(Long commentId, Long requestingUserId) {
        Comment comment = retrieveCommentById(commentId);
        User requester = userService.findEntityById(requestingUserId);

        if(!comment.getAuthor().getUserId().equals(requestingUserId) && !requester.getIsModerator()) {
            throw new IllegalArgumentException("Only the author or a moderator can delete this comment!");
        }

        commentRepository.deleteById(commentId);
    }

    public void updateVoteCount(Long commentId, int newCount) {
        Comment comment = retrieveCommentById(commentId);
        comment.setVoteCount(newCount);
        commentRepository.save(comment);
    }


}
