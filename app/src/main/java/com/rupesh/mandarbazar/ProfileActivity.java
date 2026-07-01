package com.rupesh.mandarbazar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView rvProfileContent;
    private ListingAdapter adsAdapter;
    private ChatSummaryAdapter chatListAdapter;
    private List<Listing> adList;
    private List<ChatSummary> chatList;
    private TextView tvEmptyMessage;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) { finish(); return; }
        currentUserId = user.getUid();

        Toolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageView imgProfilePic = findViewById(R.id.imgProfilePic);
        TextView tvProfileName = findViewById(R.id.tvProfileName);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        TabLayout tabLayout = findViewById(R.id.profileTabLayout);
        rvProfileContent = findViewById(R.id.rvProfileContent);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        tvProfileName.setText(user.getDisplayName());
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(imgProfilePic);
        }

        // Setup Lists
        adList = new ArrayList<>();
        chatList = new ArrayList<>();
        adsAdapter = new ListingAdapter(this, adList);
        chatListAdapter = new ChatSummaryAdapter(this, chatList);

        rvProfileContent.setLayoutManager(new GridLayoutManager(this, 2));
        rvProfileContent.setAdapter(adsAdapter);

        loadUserAds(); // Default Tab

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tvEmptyMessage.setVisibility(View.GONE);
                if (tab.getPosition() == 0) {
                    rvProfileContent.setLayoutManager(new GridLayoutManager(ProfileActivity.this, 2));
                    rvProfileContent.setAdapter(adsAdapter);
                    loadUserAds();
                } else if (tab.getPosition() == 1) {
                    loadSavedAds();
                } else {
                    rvProfileContent.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
                    rvProfileContent.setAdapter(chatListAdapter);
                    loadMyChats();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().addOnCompleteListener(t -> {
                startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            });
        });
    }

    private void loadUserAds() {
        adList.clear();
        db.collection("listings").whereEqualTo("userId", currentUserId).get().addOnSuccessListener(snapshots -> {
            for (DocumentSnapshot doc : snapshots) {
                Listing item = doc.toObject(Listing.class);
                if (item != null) { item.setId(doc.getId()); adList.add(item); }
            }
            adsAdapter.notifyDataSetChanged();
            if (adList.isEmpty()) tvEmptyMessage.setVisibility(View.VISIBLE);
        });
    }

    private void loadSavedAds() {
        adList.clear();
        adsAdapter.notifyDataSetChanged();
        // JS Logic: User doc se favorites array nikalna
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            List<String> favs = (List<String>) doc.get("favorites");
            if (favs == null || favs.isEmpty()) {
                tvEmptyMessage.setVisibility(View.VISIBLE);
                return;
            }
            for (String adId : favs) {
                db.collection("listings").document(adId).get().addOnSuccessListener(adDoc -> {
                    Listing item = adDoc.toObject(Listing.class);
                    if (item != null) { item.setId(adDoc.getId()); adList.add(item); adsAdapter.notifyDataSetChanged(); }
                });
            }
        });
    }

    private void loadMyChats() {
        chatList.clear();
        // 🔥 JS Logic: chats collection me check karna jahan current user member ho
        db.collection("chats").whereArrayContains("users", currentUserId).get().addOnSuccessListener(snapshots -> {
            for (DocumentSnapshot doc : snapshots) {
                String adTitle = doc.getString("adTitle");
                List<String> users = (List<String>) doc.get("users");
                String receiverId = "";
                if (users != null) {
                    for (String uid : users) if (!uid.equals(currentUserId)) receiverId = uid;
                }
                chatList.add(new ChatSummary(doc.getId(), adTitle, receiverId));
            }
            chatListAdapter.notifyDataSetChanged();
            if (chatList.isEmpty()) tvEmptyMessage.setVisibility(View.VISIBLE);
        });
    }
}