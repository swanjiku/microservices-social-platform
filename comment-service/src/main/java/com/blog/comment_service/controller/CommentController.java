package com.blog.comment_service.controller;

import com.blog.comment_service.model.Comment;
import com.blog.comment_service.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/post/{postId}")
    public List<Comment> getCommentsByPostId(@PathVariable Long postId) {
        return commentRepository.findByPostId(postId);
    }

    @PostMapping
    public ResponseEntity<String> createComment(@RequestBody Comment comment) {
        commentRepository.save(comment);
        return ResponseEntity.ok("Comment created successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        commentRepository.deleteById(id);
        return ResponseEntity.ok("Comment deleted successfully");
    }
}
