package com.example.demo.service;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Post> retrievePosts() {
        return (List<Post>) this.postRepository.findAll();
    }

    public Post retrievePostById(Long postId) {
        Optional<Post> post = this.postRepository.findById(postId);
        if(post.isPresent())
            return post.get();
        else
            return null;
    }

    public List<Post> retrievePostsByUserId(Long userId) {
        return this.postRepository.findByAuthorUserId(userId);
    }

    public Post insertPost(Post post) {
        if (post.getAuthor() == null || post.getAuthor().getUserId() == null)
            throw new RuntimeException("Author is required!");

        Optional<User> authorOptional = userRepository.findById(post.getAuthor().getUserId());
        if (authorOptional.isPresent()) {
            post.setAuthor(authorOptional.get());
        } else {
            throw new RuntimeException("User not found!");
        }

        return this.postRepository.save(post);
    }

    public Post updatePost(Post post) {
        if (post.getPostId() != null) {
            Optional<Post> existingOptional = postRepository.findById(post.getPostId());
            if (existingOptional.isPresent()) {
                Post existing = existingOptional.get();
                post.setCreationDate(existing.getCreationDate());
            }
        }

        if (post.getAuthor() != null && post.getAuthor().getUserId() != null) {
            Optional<User> authorOptional = userRepository.findById(post.getAuthor().getUserId());
            if (authorOptional.isPresent()) {
                post.setAuthor(authorOptional.get());
            } else {
                throw new RuntimeException("User not found!");
            }
        }

        return this.postRepository.save(post);
    }

    public String deleteById(Long postId) {
        if(!postRepository.existsById(postId))
            return "Post doesn't exist";
        try {
            this.postRepository.deleteById(postId);
        }
        catch(Exception e) {
            return "Failed deleting post " + postId;
        }
        return "Post deletion succesful";
    }
}
