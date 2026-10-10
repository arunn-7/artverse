package com.artverse.service;

import com.artverse.model.FirestoreArtwork;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.SetOptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreArtworkService {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "artworks";

    public List<FirestoreArtwork> getAllArtworks()
            throws ExecutionException, InterruptedException {

        List<FirestoreArtwork> artworks = new ArrayList<>();

        List<QueryDocumentSnapshot> documents = firestore
                .collection(COLLECTION)
                .get()
                .get()
                .getDocuments();

        for (DocumentSnapshot document : documents) {
            FirestoreArtwork artwork =
                    document.toObject(FirestoreArtwork.class);

            if (artwork != null) {
                artwork.setId(document.getId());
                artworks.add(artwork);
            }
        }

        return artworks;
    }

    public FirestoreArtwork getArtworkById(String id)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot document = firestore
                .collection(COLLECTION)
                .document(id)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        FirestoreArtwork artwork =
                document.toObject(FirestoreArtwork.class);

        if (artwork != null) {
            artwork.setId(document.getId());
        }

        return artwork;
    }

    public String createArtwork(FirestoreArtwork artwork)
            throws ExecutionException, InterruptedException {

        var documentRef = firestore.collection(COLLECTION).document();

        artwork.setId(documentRef.getId());

        Map<String, Object> data = toMap(artwork);

        documentRef.set(data).get();

        return documentRef.getId();
    }

    public void updateArtwork(String id, FirestoreArtwork artwork)
            throws ExecutionException, InterruptedException {

        var documentRef = firestore.collection(COLLECTION).document(id);

        DocumentSnapshot existing = documentRef.get().get();

        if (!existing.exists()) {
            throw new IllegalArgumentException("Artwork not found");
        }

        artwork.setId(id);

        documentRef.set(toMap(artwork), SetOptions.merge()).get();
    }

    public void deleteArtwork(String id)
            throws ExecutionException, InterruptedException {

        var documentRef = firestore.collection(COLLECTION).document(id);

        DocumentSnapshot existing = documentRef.get().get();

        if (!existing.exists()) {
            throw new IllegalArgumentException("Artwork not found");
        }

        documentRef.delete().get();
    }

    private Map<String, Object> toMap(FirestoreArtwork artwork) {

        Map<String, Object> data = new HashMap<>();

        data.put("id", artwork.getId());
        data.put("title", artwork.getTitle());
        data.put("forSale", artwork.isForSale());
        data.put("currency", artwork.getCurrency());
        data.put("status", artwork.getStatus());

        putIfNotNull(data, "description", artwork.getDescription());
        putIfNotNull(data, "imageUrl", artwork.getImageUrl());
        putIfNotNull(data, "category", artwork.getCategory());
        putIfNotNull(data, "price", artwork.getPrice());
        putIfNotNull(data, "artistUid", artwork.getArtistUid());
        putIfNotNull(data, "artistName", artwork.getArtistName());
        putIfNotNull(
                data,
                "artistProfileImageUrl",
                artwork.getArtistProfileImageUrl()
        );

        return data;
    }

    private void putIfNotNull(
            Map<String, Object> data,
            String key,
            Object value) {

        if (value != null) {
            data.put(key, value);
        }
    }
}