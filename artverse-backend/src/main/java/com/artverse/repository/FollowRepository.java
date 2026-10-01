package com.artverse.repository;

import com.artverse.entity.Follow;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowing(User follower, User following);

    List<Follow> findByFollowing(User following);

    List<Follow> findByFollower(User follower);

    long countByFollower(User follower);

    long countByFollowing(User following);


}