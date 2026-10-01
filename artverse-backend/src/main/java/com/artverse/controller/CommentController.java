package com.artverse.controller;

import com.artverse.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.artverse.dto.CommentResponse;

@RestController
@RequestMapping("/api/artworks")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/{artworkId}/comments")
    public String addComment(@PathVariable Long artworkId,
                             @RequestBody String text,
                             Authentication authentication) {

        return commentService.addComment(
                artworkId,
                authentication.getName(),
                text
        );
    }
    @GetMapping("/{artworkId}/comments")
    public List<CommentResponse> getComments(@PathVariable Long artworkId) {

        return commentService.getComments(artworkId);
    }
    @DeleteMapping("/comments/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                Authentication authentication) {

        return commentService.deleteComment(
                commentId,
                authentication.getName()
        );
    }
}