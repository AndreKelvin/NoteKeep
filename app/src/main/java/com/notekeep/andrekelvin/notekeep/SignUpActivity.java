package com.notekeep.andrekelvin.notekeep;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText email, password, repeatPassword;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.sign_up_email);
        password = findViewById(R.id.sign_up_password);
        repeatPassword = findViewById(R.id.sign_up_repeat_password);
        progressBar = findViewById(R.id.signup_progress_bar);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void signUp(View view) {
        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();
        String rePassword = repeatPassword.getText().toString().trim();

        if (Email.isEmpty() || Password.isEmpty() || !rePassword.contentEquals(Password)) {
            Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            firebaseAuth.createUserWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            try {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    Toast.makeText(SignUpActivity.this, "Sign Up successful", Toast.LENGTH_LONG).show();
                                    finish();
                                    startActivity(new Intent(SignUpActivity.this,BackUpActivity.class));
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    Toast.makeText(SignUpActivity.this,
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

    public void openLoginActivity(View view) {
        finish();
        startActivity(new Intent(this,LoginActivity.class));
    }
}
