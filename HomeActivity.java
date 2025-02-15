package com.example.pbl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button profileButton, historyButton, docsButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize buttons
        profileButton = findViewById(R.id.profile_button);
        historyButton = findViewById(R.id.history_button);
        docsButton = findViewById(R.id.docs_button);
        logoutButton = findViewById(R.id.logout_button);

        // Set click listeners
        profileButton.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        historyButton.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "History Clicked", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
        });

        docsButton.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Docs Clicked", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(HomeActivity.this, DocsActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish(); // Closes HomeActivity
        });
    }
}
