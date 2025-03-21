
package com.example.pbl;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageDisplayActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView extractedTextView;
    private TextView analysisResultView;
    private BarChart barChart; // BarChart for visualization
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private ActivityResultLauncher<String> imagePickerLauncher;

    // API configuration
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String API_KEY = "sk-9fc1b146bbae4ec5a229adc00aa2966f";

    // Parameter mapping
    private final Map<String, String> parameterMapping = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        // Initialize parameter mapping
        parameterMapping.put("Hb", "Hemoglobin");
        parameterMapping.put("RBC Count", "RBC");
        parameterMapping.put("WBC Count", "WBC");
        parameterMapping.put("Platelet Count", "Platelets");
        parameterMapping.put("MCV", "MCV");
        parameterMapping.put("MCH", "MCH");
        parameterMapping.put("MCHC", "MCHC");

        ImageButton logoutButton = findViewById(R.id.logout_button);
        LinearLayout homeTab = findViewById(R.id.home_tab);
        LinearLayout uploadTab = findViewById(R.id.upload_tab);
        LinearLayout historyTab = findViewById(R.id.history_tab);

        // Click Events for Bottom Navigation
        homeTab.setOnClickListener(v -> startActivity(new Intent(ImageDisplayActivity.this, HomeActivity.class)));
        uploadTab.setOnClickListener(v -> checkStoragePermission());
        historyTab.setOnClickListener(v -> startActivity(new Intent(ImageDisplayActivity.this, HistoryActivity.class)));

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
        extractedTextView = findViewById(R.id.extracted_text_view);
        analysisResultView = findViewById(R.id.analysisResultView);
        barChart = findViewById(R.id.barChart); // Initialize BarChart

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

        // Create static chart for predefined parameters
        createStaticChart();
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
//                    extractedTextView.setText(extractedText);

                    // Parse and visualize the extracted text
                    Map<String, Double> parameterValues = parseExtractedText(extractedText);
                    visualizeData(parameterValues);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Double> parseExtractedText(String extractedText) {
        Map<String, Double> parameterValues = new HashMap<>();

        // Split the text into lines
        String[] lines = extractedText.split("\n");

        // Separate parameter names and values
        List<String> parameterNames = new ArrayList<>();
        List<Double> parameterValuesList = new ArrayList<>();

        boolean isParameterSection = true;

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                isParameterSection = false; // Switch to values section after an empty line
                continue;
            }

            if (isParameterSection) {
                // Add parameter names
                parameterNames.add(line.trim());
            } else {
                // Add numeric values
                try {
                    double value = Double.parseDouble(line.replaceAll("[^0-9.]", ""));
                    parameterValuesList.add(value);
                } catch (NumberFormatException e) {
                    // Handle invalid values
                }
            }
        }

        // Match parameter names with values
        for (int i = 0; i < parameterNames.size() && i < parameterValuesList.size(); i++) {
            String parameter = parameterNames.get(i);
            double value = parameterValuesList.get(i);

            // Map to standardized parameter names
            String standardizedParameter = parameterMapping.getOrDefault(parameter, parameter);
            parameterValues.put(standardizedParameter, value);
        }

        return parameterValues;
    }

    private void visualizeData(Map<String, Double> parameterValues) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Double> entry : parameterValues.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Blood Report Parameters");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getDescription().setEnabled(false);
//        barChart.invalidate(); // Refresh the chart
    }

    private void createStaticChart() {
        // Create static data for the chart
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 14.5f)); // Hemoglobin
        entries.add(new BarEntry(1, 5.2f));  // RBC
        entries.add(new BarEntry(2, 7.8f));  // WBC
        entries.add(new BarEntry(3, 250f));  // Platelets
        entries.add(new BarEntry(4, 88f));   // MCV
        entries.add(new BarEntry(5, 29f));   // MCH
        entries.add(new BarEntry(6, 32f));   // MCHC

        // Create a BarDataSet with the entries
        BarDataSet dataSet = new BarDataSet(entries, "Blood Report Parameters");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Set bar colors

        // Create a BarData object with the dataset
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f); // Set the width of the bars

        // Configure the chart
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(getXAxisLabels()));
        barChart.getXAxis().setLabelCount(7);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true); // Make the bars fit within the chart
//        barChart.animateY(1000); // Add animation to the chart
//        barChart.invalidate(); // Refresh the chart
    }

    // Labels for the X-axis
    private List<String> getXAxisLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("Hemoglobin");
        labels.add("RBC");
        labels.add("WBC");
        labels.add("Platelets");
        labels.add("MCV");
        labels.add("MCH");
        labels.add("MCHC");
        return labels;
    }
}
