package com.example.pbl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        signupLink = findViewById(R.id.signup_link);


        loginButton.setOnClickListener(v -> {
            // Show loading
            //loginButton.setEnabled(false);
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Logging in...");

            // Simulate login process
            String e = "patient@gmail.com";
            String p = "pass";
            if(e.equals(emailInput.getText().toString()) && p.equals(passwordInput.getText().toString())){
                progressDialog.show();
                new Handler().postDelayed(() -> {
                    progressDialog.dismiss();
                    Intent intent = getIntent();
    //                String e = intent.getStringExtra("email");
    //                String p = intent.getStringExtra("pass");

                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));

                    finish();
                }, 1500);
        }
            else{
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_LONG).show();
            }
        });

        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }
}
