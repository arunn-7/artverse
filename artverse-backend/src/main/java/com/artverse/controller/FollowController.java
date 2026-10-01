package com.artverse.controller;

import com.artverse.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.artverse.dto.UserSummaryResponse;

@RestController
@RequestMapping("/api/users")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/{id}/follow")
    public String followUser(@PathVariable Long id,
                             Authentication authentication) {

        return followService.followUser(id, authentication);
    }
    @DeleteMapping("/{id}/follow")
    public String unfollowUser(@PathVariable Long id,
                               Authentication authentication) {

        return followService.unfollowUser(id, authentication);
    }
    @GetMapping("/{id}/followers")
    public List<UserSummaryResponse> getFollowers(@PathVariable Long id) {

        return followService.getFollowers(id);
    }
    @GetMapping("/{id}/following")
    public List<UserSummaryResponse> getFollowing(@PathVariable Long id) {

        return followService.getFollowing(id);
    }
}