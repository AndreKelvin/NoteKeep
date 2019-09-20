package com.notekeep.andrekelvin.notekeep;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.login_progress_bar);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void Login(View view) {
        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();

        if (Email.isEmpty() || Password.isEmpty()) {
            Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            firebaseAuth.signInWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            try {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                                    finish();
                                    startActivity(new Intent(LoginActivity.this,BackUpActivity.class));
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    Toast.makeText(LoginActivity.this,
                                            task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public void forgetPassword(View view) {
        startActivity(new Intent(this,ResetPasswordActivity.class));
    }
}
