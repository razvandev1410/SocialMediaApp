package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findByAuthorUserId(Long userId);
    List<Post> findByAuthorUserIdOrderByCreationDateDesc(Long userId);
    List<Post> findAllByOrderByCreationDateDesc();

    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Post> searchByTitle(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE LOWER(t.name) = LOWER(:tagName) ORDER BY p.creationDate DESC")
    List<Post> findByTagName(@Param("tagName") String tagName);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE LOWER(t.name) = LOWER(:tagName) AND p.author.userId = :userId ORDER BY p.creationDate DESC")
    List<Post> findByTagNameAndUserId(@Param("tagName") String tagName, @Param("userId") Long userId);

}
