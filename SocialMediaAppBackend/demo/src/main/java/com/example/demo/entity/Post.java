package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="posts")
public class Post {
    @Id
    @Column(name="post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(name="title", nullable = false)
    private String title;

    @Column(name="text", nullable = false)
    private String text;

    @Column(name="image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name="author_id")
    private User author;

    @CreationTimestamp
    @Column(name="creation_date")
    private Instant creationDate;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments;
}
