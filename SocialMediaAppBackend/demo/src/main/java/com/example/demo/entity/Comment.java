package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

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

    @CreationTimestamp
    @Column(name="creation_date")
    private Instant creationDate;

    @ManyToOne()
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne()
    @JoinColumn(name = "post_id")
    private Post post;
}