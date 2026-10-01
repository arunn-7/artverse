package com.artverse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "artwork_id")
    private Artwork artwork;

    public Like() {
    }

    public Like(User user, Artwork artwork) {
        this.user = user;
        this.artwork = artwork;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }
}