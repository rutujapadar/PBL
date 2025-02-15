package com.example.pbl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText nameInput, emailInput, passwordInput;
    private CheckBox termsCheckbox;
    private MaterialButton signupButton;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        termsCheckbox = findViewById(R.id.terms_checkbox);
        signupButton = findViewById(R.id.signup_button);
        loginLink = findViewById(R.id.login_link);

        signupButton.setOnClickListener(v -> {
            if (!termsCheckbox.isChecked()) {
                Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading
            signupButton.setEnabled(false);
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Creating account...");
            progressDialog.show();

            // Simulate signup process
            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                finish();
            }, 1500);
        });

        loginLink.setOnClickListener(v -> {
            // Navigate back to login screen
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }
}