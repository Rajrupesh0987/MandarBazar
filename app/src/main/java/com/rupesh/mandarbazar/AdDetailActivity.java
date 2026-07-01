package com.rupesh.mandarbazar;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdDetailActivity extends AppCompatActivity {

    private boolean isSaved = false;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_detail);

        db = FirebaseFirestore.getInstance();

        // 1. Views Binding
        ImageView imgDetailAd = findViewById(R.id.imgDetailAd);
        TextView tvDetailTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDetailPrice = findViewById(R.id.tvDetailPrice);
        TextView tvDetailDesc = findViewById(R.id.tvDetailDesc);
        MaterialButton btnDetailCall = findViewById(R.id.btnDetailCall);
        MaterialButton btnDetailChat = findViewById(R.id.btnDetailChat);
        MaterialButton btnDeleteAd = findViewById(R.id.btnDeleteAd);
        FloatingActionButton btnLikeAd = findViewById(R.id.btnLikeAd); // 🔥 Like Button
        Toolbar toolbar = findViewById(R.id.toolbarDetail);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // 2. Getting Data
        String title = getIntent().getStringExtra("title");
        String price = getIntent().getStringExtra("price");
        String desc = getIntent().getStringExtra("desc");
        String imageCode = getIntent().getStringExtra("image");
        String phone = getIntent().getStringExtra("phone");
        String itemId = getIntent().getStringExtra("itemId");
        String ownerId = getIntent().getStringExtra("ownerId");

        tvDetailTitle.setText(title != null ? title : "No Title");
        tvDetailPrice.setText("₹ " + (price != null ? price : "0"));
        tvDetailDesc.setText(desc != null ? desc : "No description available.");

        // 3. Ownership & Login Logic
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : "";

        if (!currentUserId.isEmpty() && currentUserId.equals(ownerId)) {
            btnDeleteAd.setVisibility(View.VISIBLE);
            btnDetailChat.setVisibility(View.GONE);
            btnDetailCall.setVisibility(View.GONE);
        } else {
            btnDeleteAd.setVisibility(View.GONE);
            btnDetailChat.setVisibility(View.VISIBLE);
            btnDetailCall.setVisibility(View.VISIBLE);
        }

        // 🔥 4. SAVE / LIKE LOGIC
        if (currentUser != null && itemId != null) {
            // Check if already saved
            db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    List<String> favs = (List<String>) doc.get("favorites");
                    if (favs != null && favs.contains(itemId)) {
                        isSaved = true;
                        btnLikeAd.setImageResource(android.R.drawable.btn_star_big_on);
                        btnLikeAd.setSupportImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700")));
                    }
                }
            });

            btnLikeAd.setOnClickListener(v -> {
                if (isSaved) {
                    // Remove from favorites
                    db.collection("users").document(currentUserId)
                            .update("favorites", FieldValue.arrayRemove(itemId))
                            .addOnSuccessListener(aVoid -> {
                                isSaved = false;
                                btnLikeAd.setImageResource(android.R.drawable.btn_star_big_off);
                                btnLikeAd.setSupportImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#757575")));
                                Toast.makeText(this, "Removed from saved", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Add to favorites
                    db.collection("users").document(currentUserId)
                            .update("favorites", FieldValue.arrayUnion(itemId))
                            .addOnSuccessListener(aVoid -> {
                                isSaved = true;
                                btnLikeAd.setImageResource(android.R.drawable.btn_star_big_on);
                                btnLikeAd.setSupportImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700")));
                                Toast.makeText(this, "Ad Saved!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Agar document nahi hai to create karo
                                Map<String, Object> data = new HashMap<>();
                                data.put("favorites", Arrays.asList(itemId));
                                db.collection("users").document(currentUserId).set(data, SetOptions.merge());
                            });
                }
            });
        }

        // 5. Photo Load
        if (imageCode != null && !imageCode.isEmpty()) {
            if (imageCode.startsWith("http")) {
                Glide.with(this).load(imageCode).into(imgDetailAd);
            } else {
                try {
                    String pureBase64 = imageCode.contains(",") ? imageCode.split(",")[1] : imageCode;
                    byte[] imageBytes = Base64.decode(pureBase64, Base64.DEFAULT);
                    Glide.with(this).asBitmap().load(imageBytes).into(imgDetailAd);
                } catch (Exception e) {
                    imgDetailAd.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        }

        // 6. Delete Ad
        btnDeleteAd.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Ad?")
                    .setMessage("Kya aap pakka is ad ko delete karna chahte hain?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (itemId != null) {
                            db.collection("listings").document(itemId).delete().addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Ad Deleted!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // 7. Chat & Call
        btnDetailCall.setOnClickListener(v -> {
            if (phone != null && !phone.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
            }
        });

        btnDetailChat.setOnClickListener(v -> {
            if (currentUser == null) {
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            String chatId = itemId + "_" + currentUserId;
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("users", Arrays.asList(currentUserId, ownerId));
            chatData.put("adTitle", title);
            chatData.put("itemId", itemId);
            chatData.put("updatedAt", FieldValue.serverTimestamp());

            db.collection("chats").document(chatId).set(chatData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.putExtra("chatId", chatId);
                        intent.putExtra("receiverId", ownerId);
                        intent.putExtra("adTitle", title);
                        startActivity(intent);
                    });
        });
    }
}