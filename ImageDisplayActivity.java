package com.example.pbl;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView; // Import the TextView class
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class ImageDisplayActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView extractedTextView; // Declare the TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

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
