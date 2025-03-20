package com.example.pbl;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Navbar Buttons
        ImageButton logoutButton = findViewById(R.id.logout_button);

        // Bottom Navigation Tabs
        LinearLayout homeTab = findViewById(R.id.home_tab);
        LinearLayout uploadTab = findViewById(R.id.upload_tab);
        LinearLayout historyTab = findViewById(R.id.history_tab);

        // Initialize Image Picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleImageSelection
        );

        // Click Events for Bottom Navigation
        homeTab.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, HomeActivity.class))); // Reload Home
        uploadTab.setOnClickListener(v -> checkStoragePermission()); // Pick Image & Open ImageDisplayActivity
        historyTab.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, HistoryActivity.class))); // Open HistoryActivity

        // Logout Button
        logoutButton.setOnClickListener(v -> {
            Intent logoutIntent = new Intent(HomeActivity.this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
        });
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            } else {
                pickImage();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            } else {
                pickImage();
            }
        }
    }

    private void pickImage() {
        imagePickerLauncher.launch("image/*");
    }

    private void handleImageSelection(Uri selectedImageUri) {
        if (selectedImageUri != null) {
            Intent intent = new Intent(HomeActivity.this, ImageDisplayActivity.class);
            intent.putExtra("imageUri", selectedImageUri.toString());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access files.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
