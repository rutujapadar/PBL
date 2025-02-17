package com.example.pbl;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.common.InputImage;
import java.io.IOException;

public class ImageDisplayActivity extends AppCompatActivity {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        imageView = findViewById(R.id.image_view);

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
        TextRecognizer textRecognizer = TextRecognition.getClient();
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        textRecognizer.process(inputImage)
                .addOnSuccessListener(visionText -> {
                    String extractedText = visionText.getText();
                    Toast.makeText(this, "Extracted Text: " + extractedText, Toast.LENGTH_LONG).show();
                    // Optionally, display the extracted text in a TextView
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show();
                });
    }
}
