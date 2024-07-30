package com.example.fisherman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class Posts extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;

    private EditText contenidoEditText;
    private Button publicarButton, adjuntarImagenButton, adjuntarVideoButton;
    private ImageView imageView;
    private Uri imageUri, videoUri;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseUser currentUser;
    private String uniqueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("publicaciones");
        currentUser = auth.getCurrentUser();
        contenidoEditText = findViewById(R.id.editTextContenido);
        publicarButton = findViewById(R.id.btnPublicar);
        adjuntarImagenButton = findViewById(R.id.btnAdjuntarImagen);
        adjuntarVideoButton = findViewById(R.id.btnAdjuntarVideo);
        imageView = findViewById(R.id.imageViewAdjunta);

        publicarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publicarContenido();
            }
        });

        adjuntarImagenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirGaleria(PICK_IMAGE_REQUEST);
            }
        });

        adjuntarVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirGaleria(PICK_VIDEO_REQUEST);
            }
        });
    }

    private void abrirGaleria(int requestCode) {
        Intent intent;
        if (requestCode == PICK_IMAGE_REQUEST) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
        }

        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
                imageView.setVisibility(View.VISIBLE); // Mostrar la vista previa de la imagen
            } else if (requestCode == PICK_VIDEO_REQUEST) {
                videoUri = data.getData();
            }
        }
    }


    private void publicarContenido() {
        String contenido = contenidoEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(contenido)) {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                if (imageUri != null) {
                    subirArchivoMedia(imageUri, "imagenes");
                } else if (videoUri != null) {
                    subirArchivoMedia(videoUri, "videos");
                } else {
                    // Si no hay archivo multimedia adjunto, publicar sin URL de multimedia
                    Publicacion publicacion = new Publicacion();
                    publicacion.setContenido(contenido);
                    publicacion.setUserId(currentUser.getUid());

                    // Guardar publicación
                    guardarPublicacion(publicacion);
                }
            }
        } else {
            Toast.makeText(this, "Por favor, ingrese contenido", Toast.LENGTH_SHORT).show();
        }
    }




    private void subirArchivoMedia(Uri fileUri, String folderName) {
        StorageReference fileReference = storageReference.child(folderName).child(System.currentTimeMillis() + "." + getFileExtension(fileUri));
        UploadTask uploadTask = fileReference.putFile(fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String mediaUrl = uri.toString();

                // Use the class-level uniqueId as the document ID when creating the Publicacion instance
                Publicacion publicacion = new Publicacion(uniqueId, contenidoEditText.getText().toString().trim(), currentUser.getUid(), mediaUrl, null,0);
                guardarPublicacion(publicacion);
            });
        });
    }


    private void guardarPublicacion(Publicacion publicacion) {
        db.collection("publicaciones")
                .add(publicacion)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            DocumentReference documentReference = task.getResult();
                            if (documentReference != null) {
                                String nuevoDocumentId = documentReference.getId();
                                // Establecer el nuevo ID del documento en la Publicacion
                                publicacion.setDocumentId(nuevoDocumentId);
                                uniqueId = nuevoDocumentId;
                            }
                        } else {
                            Toast.makeText(Posts.this, "Error al publicar", Toast.LENGTH_SHORT).show();
                            Log.e("Posts", "Error al publicar", task.getException());
                        }
                    }
                });
    }






    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }
}
