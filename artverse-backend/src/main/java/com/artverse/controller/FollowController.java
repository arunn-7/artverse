package com.artverse.controller;

import com.artverse.dto.UserSummaryResponse;
import com.artverse.service.FollowService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{id}/follow")
    public String followUser(
            @PathVariable String id,
            Authentication authentication) {

        return followService.followUser(id, authentication);
    }

    @DeleteMapping("/{id}/follow")
    public String unfollowUser(
            @PathVariable String id,
            Authentication authentication) {

        return followService.unfollowUser(id, authentication);
    }

    @GetMapping("/{id}/followers")
    public List<UserSummaryResponse> getFollowers(
            @PathVariable String id) {

        return followService.getFollowers(id);
    }

    @GetMapping("/{id}/following")
    public List<UserSummaryResponse> getFollowing(
            @PathVariable String id) {

        return followService.getFollowing(id);
    }
}