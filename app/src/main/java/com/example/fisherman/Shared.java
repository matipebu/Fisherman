package com.example.fisherman;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Shared extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SharedAdapter savedAdapter;
    private List<Publicacion> savedPublications;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerViewShared);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        savedPublications = new ArrayList<>();
        savedAdapter = new SharedAdapter(savedPublications, this, db);

        recyclerView.setAdapter(savedAdapter);

        loadSavedPublications();
    }

    private void loadSavedPublications() {
        if (currentUser != null) {
            db.collection("saves")
                    .whereEqualTo("userId", currentUser.getUid())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            savedPublications.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get the postId from the "saves" collection
                                String postId = document.getString("postId");

                                // Retrieve the corresponding Publicacion from the "publicaciones" collection
                                if (postId != null) {
                                    db.collection("publicaciones")
                                            .document(postId)
                                            .get()
                                            .addOnCompleteListener(publicationTask -> {
                                                if (publicationTask.isSuccessful()) {
                                                    DocumentSnapshot publicationSnapshot = publicationTask.getResult();
                                                    if (publicationSnapshot.exists()) {
                                                        Publicacion publicacion = publicationSnapshot.toObject(Publicacion.class);
                                                        if (publicacion != null) {
                                                            savedPublications.add(publicacion);
                                                            savedAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                } else {
                                                    Log.e("SavedActivity", "Error getting saved publication", publicationTask.getException());
                                                }
                                            });
                                }
                            }
                        } else {
                            Log.e("SavedActivity", "Error getting saved publications", task.getException());
                        }
                    });
        }
    }
}
