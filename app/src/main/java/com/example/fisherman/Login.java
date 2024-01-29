package com.example.fisherman;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    EditText email, contraseña;
    TextView crearCuenta;
    Button ini_sesion;
    CheckBox checkBox;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);





        email = findViewById(R.id.email_login);
        contraseña = findViewById(R.id.contraseña_login);
        crearCuenta = findViewById(R.id.btn_crearcuenta);
        checkBox = findViewById(R.id.login_checkbox);
        progressBar = findViewById(R.id.progressbar_login);
        ini_sesion = findViewById(R.id.iniciar_sesion);

        mAuth = FirebaseAuth.getInstance();
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if (b) {
                    contraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                } else {
                    contraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());

                }
            }
        });



        crearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendtoCrearCuenta();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ini_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email.getText().toString();
                String pass = contraseña.getText().toString();
                if(!TextUtils.isEmpty(mail)||!TextUtils.isEmpty(pass)) {
                        progressBar.setVisibility(View.VISIBLE);
                        mAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    sendtoMain();
                                    progressBar.setVisibility(View.INVISIBLE);

                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(Login.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                }else{
                    Toast.makeText(Login.this, "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }

    private void sendtoCrearCuenta() {
        Log.d("TAG", "sendtoCrearCuenta() llamado");
        Intent intent = new Intent(Login.this, CrearCuenta.class);
        startActivity(intent);
        finish();
    }

    private void sendtoMain() {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            sendtoMain();
        }

    }
}