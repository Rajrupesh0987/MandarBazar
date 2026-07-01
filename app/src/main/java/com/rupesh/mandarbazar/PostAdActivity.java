package com.rupesh.mandarbazar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PostAdActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etPrice, etDesc, etPhone;
    private Spinner spinMain, spinSub;
    private ImageView imgSelected;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;
    private String encodedImage = "";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // 🔥 JS wala Category Data
    private String[] mainCats = {"Market", "Jobs", "Prachar"};
    private String[] marketSub = {"Bike/Scooty", "Car/Vehicle", "Cycle", "Mobile/Electronics", "Land/Plot", "Hardware/Tools", "Furniture", "Others"};
    private String[] jobsSub = {"Labour/Worker", "Mistri (Raj/Tile)", "Data Entry/Computer", "Driver", "Cook/Helper", "Teacher/Tutor", "Others"};
    private String[] pracharSub = {"Shop Banner", "Coaching", "Service Center", "Clinic/Doctor", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ad);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        imgSelected = findViewById(R.id.imgSelected);
        etTitle = findViewById(R.id.etPostTitle);
        etPrice = findViewById(R.id.etPostPrice);
        etPhone = findViewById(R.id.etPostPhone);
        etDesc = findViewById(R.id.etPostDesc);
        spinMain = findViewById(R.id.spinnerMainCategory);
        spinSub = findViewById(R.id.spinnerSubCategory);
        btnSubmit = findViewById(R.id.btnSubmitAd);
        progressBar = findViewById(R.id.postProgressBar);

        // Image Selection
        findViewById(R.id.cardSelectImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        });

        // 🔥 JS wala Dynamic Logic
        ArrayAdapter<String> mainAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mainCats);
        spinMain.setAdapter(mainAdapter);

        spinMain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSubSpinner(mainCats[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSubmit.setOnClickListener(v -> uploadData());
    }

    private void updateSubSpinner(String type) {
        String[] currentSub;
        if (type.equals("Market")) currentSub = marketSub;
        else if (type.equals("Jobs")) currentSub = jobsSub;
        else currentSub = pracharSub;

        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currentSub);
        spinSub.setAdapter(subAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imgSelected.setImageBitmap(bitmap);
                imgSelected.setPadding(0,0,0,0);
                encodedImage = encodeImage(bitmap);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 480;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void uploadData() {
        String title = etTitle.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String mainCat = spinMain.getSelectedItem().toString();
        String subCat = spinSub.getSelectedItem().toString();

        if (title.isEmpty() || encodedImage.isEmpty() || phone.isEmpty() || mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Sab details bhariye aur login rahein!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // JS Compatibility Mapping
        String finalType = "sell";
        if(mainCat.equals("Jobs")) finalType = "job";
        else if(mainCat.equals("Prachar")) finalType = "promotion";

        Map<String, Object> ad = new HashMap<>();
        ad.put("title", title);
        ad.put("price", price);
        ad.put("phone", phone);
        ad.put("desc", desc);
        ad.put("type", finalType); // 'sell', 'job', 'promotion'
        ad.put("category", subCat); // 'Bike', 'Driver', etc.
        ad.put("image", encodedImage);
        ad.put("userId", mAuth.getCurrentUser().getUid());
        ad.put("userName", mAuth.getCurrentUser().getDisplayName());
        ad.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection("listings").add(ad).addOnSuccessListener(doc -> {
            Toast.makeText(this, "Ad Live Ho Gaya!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}