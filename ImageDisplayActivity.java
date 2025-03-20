package com.example.pbl;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView; // Import the TextView class
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class ImageDisplayActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView extractedTextView; // Declare the TextView
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private ActivityResultLauncher<String> imagePickerLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        ImageButton logoutButton = findViewById(R.id.logout_button);
        LinearLayout homeTab = findViewById(R.id.home_tab);
        LinearLayout uploadTab = findViewById(R.id.upload_tab);
        LinearLayout historyTab = findViewById(R.id.history_tab);
        // Click Events for Bottom Navigation
        homeTab.setOnClickListener(v -> startActivity(new Intent(ImageDisplayActivity.this, HomeActivity.class))); // Reload Home
        uploadTab.setOnClickListener(v -> checkStoragePermission()); // Pick Image & Open ImageDisplayActivity
        historyTab.setOnClickListener(v -> startActivity(new Intent(ImageDisplayActivity.this, HistoryActivity.class))); // Open HistoryActivity

        // Initialize Image Picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleImageSelection
        );
        // Logout Button
        logoutButton.setOnClickListener(v -> {
            Intent logoutIntent = new Intent(ImageDisplayActivity.this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
        });
        imageView = findViewById(R.id.image_view);
        extractedTextView = findViewById(R.id.extracted_text_view); // Initialize the TextView

        // Get image URI from intent
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            try {
                // Convert image URI to bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);

                // Extract text from bitmap
                extractText(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            } else {
                pickImage();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
            Intent intent = new Intent(ImageDisplayActivity.this, ImageDisplayActivity.class);
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
    private void extractText(Bitmap bitmap) {
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        textRecognizer.process(inputImage)
                .addOnSuccessListener(visionText -> {
                    String extractedText = visionText.getText();
                    // Update the TextView with the extracted text
                    extractedTextView.setText(extractedText);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show();
                });
    }


}
