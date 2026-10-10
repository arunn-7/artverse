package com.artverse.controller;

import com.artverse.dto.CommentResponse;
import com.artverse.service.FirestoreCommentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artworks")
public class CommentController {

    private final FirestoreCommentService commentService;

    public CommentController(
            FirestoreCommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{artworkId}/comments")
    public String addComment(
            @PathVariable String artworkId,
            @RequestBody String text,
            Authentication authentication) {

        return commentService.addComment(
                artworkId, authentication.getName(), text);
    }

    @GetMapping("/{artworkId}/comments")
    public List<CommentResponse> getComments(
            @PathVariable String artworkId) {

        return commentService.getComments(artworkId);
    }

    @DeleteMapping("/comments/{commentId}")
    public String deleteComment(
            @PathVariable String commentId,
            Authentication authentication) {

        return commentService.deleteComment(
                commentId, authentication.getName());
    }
}