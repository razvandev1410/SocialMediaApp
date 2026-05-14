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
public class Comment {
    @Id
    @Column(name="comment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(name="text", nullable = false)
    private String text;

    @Column(name="image_url", nullable = true)
    private String imageUrl;

    @CreationTimestamp
    @Column(name="creation_date")
    private Instant creationDate;

    @Column(name = "vote_count")
    private Integer voteCount = 0;

    @ManyToOne()
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne()
    @JoinColumn(name = "post_id")
    private Post post;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Vote> votes;
}