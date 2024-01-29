package com.example.fisherman;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class CrearCuenta extends AppCompatActivity {
    EditText email, contraseña, conf_contraseña;
    Button crearCuenta_btn;
    CheckBox checkBox;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);

        email = findViewById(R.id.email_crearcuenta);
        contraseña = findViewById(R.id.contraseña_crearcuenta);
        conf_contraseña = findViewById(R.id.repitepass_crearcuenta);
        crearCuenta_btn = findViewById(R.id.boton_crearcuenta);
        checkBox = findViewById(R.id.crearcuenta_checkbox);
        progressBar = findViewById(R.id.progressbar_crearcuenta);
        mAuth = FirebaseAuth.getInstance();

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if (b) {
                    contraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    conf_contraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    contraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    conf_contraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        crearCuenta_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email.getText().toString();
                String pass = contraseña.getText().toString();
                String confirm_pass = conf_contraseña.getText().toString();

                if (!TextUtils.isEmpty(mail) || !TextUtils.isEmpty(pass) || !TextUtils.isEmpty(confirm_pass)) {
                    if (pass.equals(confirm_pass)) {
                        progressBar.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    sendtoLogin();
                                    progressBar.setVisibility(View.INVISIBLE);

                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(CrearCuenta.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CrearCuenta.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CrearCuenta.this, "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void sendtoLogin() {
        Intent intent = new Intent(CrearCuenta.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Intent intent = new Intent(CrearCuenta.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
