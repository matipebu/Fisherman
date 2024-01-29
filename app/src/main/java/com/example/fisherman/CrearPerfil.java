package com.example.fisherman;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class CrearPerfil extends AppCompatActivity {

    private EditText nombreEditText, biografiaEditText, emailEditText, cañaEditText, carreteEditText, otrosAccesoriosEditText, tiposPescaEditText;
    private CircleImageView circleImageView;
    private ProgressBar progressBar;
    private Button guardarButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_perfil);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Inicializar vistas
        nombreEditText = findViewById(R.id.er_nombre_cp);
        biografiaEditText = findViewById(R.id.er_bio_cp);
        emailEditText = findViewById(R.id.er_email_cp);
        cañaEditText = findViewById(R.id.er_caña_cp);
        carreteEditText = findViewById(R.id.er_carrete_cp);
        otrosAccesoriosEditText = findViewById(R.id.er_otros_cp);
        tiposPescaEditText = findViewById(R.id.er_tpesca_cp);
        circleImageView = findViewById(R.id.iv_cp);
        progressBar = findViewById(R.id.progressbar_cp);
        guardarButton = findViewById(R.id.btn_cp);

        // Obtener datos del usuario actual
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            emailEditText.setText(currentUser.getEmail());

            // Obtener la referencia del documento del usuario actual en Firestore
            DocumentReference userRef = db.collection("usuarios").document(currentUser.getUid());

            // Leer datos del usuario desde Firestore
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Obtener datos del documento y llenar los campos
                            Perfil perfil = document.toObject(Perfil.class);
                            if (perfil != null) {
                                nombreEditText.setText(perfil.getName());
                                biografiaEditText.setText(perfil.getBio());
                                cañaEditText.setText(perfil.getCaña());
                                carreteEditText.setText(perfil.getCarrete());
                                otrosAccesoriosEditText.setText(perfil.getOtros());
                                tiposPescaEditText.setText(perfil.getTpesca());

                                // Cargar la imagen utilizando Glide
                                if (perfil.getUrl() != null && !perfil.getUrl().isEmpty()) {
                                    Glide.with(CrearPerfil.this).load(perfil.getUrl()).into(circleImageView);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(CrearPerfil.this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Lógica para actualizar el usuario en Firestore al hacer clic en el botón
        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actualizarUsuarioFirestore();
            }
        });

        // Lógica para seleccionar una imagen al hacer clic en el ImageView
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarSelectorImagen();
            }
        });
    }

    private void mostrarSelectorImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            circleImageView.setImageURI(imageUri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // Aquí puedes hacer algo con la imagen capturada, por ejemplo, mostrarla en el ImageView
            circleImageView.setImageBitmap(imageBitmap);
        }
    }

    private void actualizarUsuarioFirestore() {
        // Mostrar ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Obtener datos del formulario
        String nombre = nombreEditText.getText().toString().trim();
        String biografia = biografiaEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String caña = cañaEditText.getText().toString().trim();
        String carrete = carreteEditText.getText().toString().trim();
        String otrosAccesorios = otrosAccesoriosEditText.getText().toString().trim();
        String tiposPesca = tiposPescaEditText.getText().toString().trim();

        // Crear objeto Perfil
        Perfil perfil = new Perfil();
        perfil.setName(nombre);
        perfil.setEmail(email);
        perfil.setBio(biografia);
        perfil.setCaña(caña);
        perfil.setCarrete(carrete);
        perfil.setOtros(otrosAccesorios);
        perfil.setTpesca(tiposPesca);

        // Subir la imagen a Firebase Storage
        if (imageUri != null) {
            subirImagenFirebaseStorage(perfil);
        } else {
            // Si no hay imagen seleccionada, simplemente actualiza Firestore
            actualizarFirestore(perfil);
        }
    }

    private void subirImagenFirebaseStorage(Perfil perfil) {
        // Obtén la referencia del Storage para el usuario actual
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("imagenesPerfil").child(auth.getCurrentUser().getUid());

        // Sube la imagen al Storage
        UploadTask uploadTask = storageRef.putFile(imageUri);

        // Maneja el resultado de la subida
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // La imagen se subió correctamente, obtén la URL de descarga
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            // Actualiza Firestore con la URL de la imagen
                            perfil.setUrl(imageUrl);
                            // Completa la actualización del perfil en Firestore
                            actualizarFirestore(perfil);
                        }
                    });
                } else {
                    // Error al subir la imagen
                    Toast.makeText(CrearPerfil.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void actualizarFirestore(Perfil perfil) {
        // Obtener la referencia del documento del usuario actual en Firestore
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            perfil.setUid(currentUser.getUid());
            DocumentReference userRef = db.collection("usuarios").document(currentUser.getUid());

            // Actualizar datos del usuario en Firestore
            userRef.set(perfil)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Ocultar ProgressBar
                            progressBar.setVisibility(View.INVISIBLE);

                            if (task.isSuccessful()) {
                                Toast.makeText(CrearPerfil.this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CrearPerfil.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(CrearPerfil.this, "Error al actualizar usuario", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
