package com.example.demo.controller;

import com.example.demo.dto.VoteRequest;
import com.example.demo.entity.Vote;
import com.example.demo.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/votes")
@CrossOrigin(origins = "*")
public class VoteController {
    @Autowired
    private VoteService voteService;

    @PostMapping("/post")
    public ResponseEntity<Vote> voteOnPost(@Valid @RequestBody VoteRequest request) {
        Vote vote = voteService.voteOnPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vote);
    }

    @PostMapping("/comment")
    public ResponseEntity<Vote> voteOnComment(@Valid @RequestBody VoteRequest request) {
        Vote vote = voteService.voteOnComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vote);
    }

}
