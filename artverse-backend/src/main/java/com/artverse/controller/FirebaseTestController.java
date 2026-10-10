
package com.artverse.controller;

import com.google.cloud.firestore.Firestore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class FirebaseTestController {

    private final Firestore firestore;

    public FirebaseTestController(Firestore firestore) {
        this.firestore = firestore;
    }

    @GetMapping("/api/test/firestore")
    public ResponseEntity<Map<String, Object>> testFirestore()
            throws Exception {

        var snapshot = firestore.collection("users")
                .limit(1)
                .get()
                .get(10, TimeUnit.SECONDS);

        return ResponseEntity.ok(Map.of(
                "status", "connected",
                "collection", "users",
                "documentsFound", snapshot.size()
        ));
    }
}
