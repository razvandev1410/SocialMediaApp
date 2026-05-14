package com.example.demo.service;

import com.example.demo.dto.PostDTO;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private TagService tagService;

    public List<Post> retrievePosts() {
        return (List<Post>) postRepository.findAllByOrderByCreationDateDesc();
    }

    public Post retrievePostById(Long postId) {
        Optional<Post> post = this.postRepository.findById(postId);
        if(post.isPresent())
            return post.get();
        else
            throw new ResourceNotFoundException("Post with id " + postId + " not found!");
    }

    public List<Post> retrievePostsByUserId(Long userId) {
        return this.postRepository.findByAuthorUserIdOrderByCreationDateDesc(userId);
    }

    public Post insertPost(PostDTO dto) {
        User author = userService.findEntityById(dto.getAuthorId());
        if(author.getIsBanned()) {
            throw new IllegalArgumentException("Banned users can't do this!");
        }

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setText(dto.getText());
        post.setImageUrl(dto.getImageUrl());
        post.setAuthor(author);
        post.setPostStatus(PostStatus.JUST_POSTED);
        post.setVoteCount(0);

        if(dto.getTags() != null && !dto.getTags().isEmpty()) {
            List<Tag> tags = new ArrayList<>();
            for(String tagName : dto.getTags()) {
                tags.add(tagService.getOrCreateTag(tagName));
            }
            post.setTags(tags);
        }

        return postRepository.save(post);
    }

    public Post updatePost(Long postId, PostDTO dto, Long requestingUserId) {
        Post existing = retrievePostById(postId);
        User requester = userService.findEntityById(requestingUserId);

        User author = userService.findEntityById(requestingUserId);
        if(author.getIsBanned()) {
            throw new IllegalArgumentException("Banned users can't do this!");
        }

        if(!existing.getAuthor().getUserId().equals(requestingUserId) && !requester.getIsModerator()) {
            throw new IllegalArgumentException("Only the author or a moderator can edit this post!");
        }

        if(dto.getTitle() != null)
            existing.setTitle(dto.getTitle());
        if(dto.getText() != null)
            existing.setText(dto.getText());
        if(dto.getImageUrl() != null)
            existing.setImageUrl(dto.getImageUrl());
        if(dto.getTags() != null) {
            List<Tag> tags = new ArrayList<>();
            for(String tagName : dto.getTags()) {
                tags.add(tagService.getOrCreateTag(tagName));
            }
            existing.setTags(tags);
        }

        return postRepository.save(existing);
    }

    public void deleteById(Long postId, Long requestingUserId) {
        Post post = retrievePostById(postId);
        User requester = userService.findEntityById(requestingUserId);

        if(!post.getAuthor().getUserId().equals(requestingUserId) && !requester.getIsModerator()) {
            throw new IllegalArgumentException("Only the author or a moderator can delete this post");
        }

        postRepository.deleteById(postId);
    }

    public Post setOutdated(Long postId, Long requestingUserId) {
        Post post = retrievePostById(postId);

        if(!post.getAuthor().getUserId().equals(requestingUserId)) {
            throw new IllegalArgumentException("Only the author of the post can set its status to OUTDATED!");
        }

        post.setPostStatus(PostStatus.OUTDATED);
        return postRepository.save(post);
    }

    public List<Post> searchByTitle(String keyword) {
        return postRepository.searchByTitle(keyword);
    }

    public List<Post> filterByTag(String tagName) {
        return postRepository.findByTagName(tagName);
    }

    public List<Post> filterByTagAndUser(String tagName, Long userId) {
        return postRepository.findByTagNameAndUserId(tagName, userId);
    }

    public void updateStatusToFirstReactions(Long postId) {
        Post post = retrievePostById(postId);
        if(post.getPostStatus() == PostStatus.JUST_POSTED) {
            post.setPostStatus(PostStatus.FIRST_REACTIONS);
            postRepository.save(post);
        }
    }

    public void updateVoteCount(Long postId, int newCount) {
        Post post = retrievePostById(postId);
        post.setVoteCount(newCount);
        postRepository.save(post);
    }
}
