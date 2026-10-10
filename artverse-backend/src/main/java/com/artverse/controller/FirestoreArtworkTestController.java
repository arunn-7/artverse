package com.artverse.controller;

import com.artverse.model.FirestoreArtwork;
import com.artverse.service.FirestoreArtworkService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/test/firestore-artworks")
public class FirestoreArtworkTestController {

    @Autowired
    private FirestoreArtworkService firestoreArtworkService;

    @GetMapping
    public List<FirestoreArtwork> getAllArtworks()
            throws ExecutionException, InterruptedException {

        return firestoreArtworkService.getAllArtworks();
    }

    @GetMapping("/{id}")
    public FirestoreArtwork getArtworkById(
            @PathVariable String id)
            throws ExecutionException, InterruptedException {

        return firestoreArtworkService.getArtworkById(id);
    }
}