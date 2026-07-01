package com.rupesh.mandarbazar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvListings;
    private TabLayout tabLayoutMain;
    private ExtendedFloatingActionButton fabPostAd;
    private ProgressBar progressBarMain;
    private ImageView imgUserProfile;
    private EditText etSearch;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListingAdapter adapter;
    private List<Listing> listingList;
    private List<Listing> fullListingList;
    private String currentFilter = "all";

    // 🔥 Pagination Variables
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;
    private boolean isLastItemReached = false;
    private final int PAGE_SIZE = 10; //showing 10 ads aksath

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvListings = findViewById(R.id.rvListings);
        tabLayoutMain = findViewById(R.id.tabLayoutMain);
        fabPostAd = findViewById(R.id.fabPostAd);
        progressBarMain = findViewById(R.id.progressBarMain);
        imgUserProfile = findViewById(R.id.imgUserProfile);
        etSearch = findViewById(R.id.etSearch);

        listingList = new ArrayList<>();
        fullListingList = new ArrayList<>();
        adapter = new ListingAdapter(this, listingList);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvListings.setLayoutManager(layoutManager);
        rvListings.setAdapter(adapter);

        // 🔥 Standard Scroll Listener for Pagination
        rvListings.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // dy > 0 means user scrolling bottom
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && !isLastItemReached) {
                        // for showing new data
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loadMoreListings();
                        }
                    }
                }
            }
        });

        loadListings();
        updateUserProfileImage();

        // 🔥 Search Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        imgUserProfile.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        fabPostAd.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(MainActivity.this, PostAdActivity.class));
            } else {
                Toast.makeText(this, "Pehle login karein!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        tabLayoutMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (etSearch != null) etSearch.setText("");

                switch (tab.getPosition()) {
                    case 0: currentFilter = "all"; break;
                    case 1: currentFilter = "sell"; break;
                    case 2: currentFilter = "job"; break;
                    case 3: currentFilter = "promotion"; break;
                }
                loadListings();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // 🔥 Search Filter Method
    private void filter(String text) {
        List<Listing> filteredList = new ArrayList<>();
        for (Listing item : fullListingList) {
            if (item.getTitle() != null && item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.updateList(filteredList);
    }

    private void updateUserProfileImage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.baseline_login_24)
                    .circleCrop()
                    .into(imgUserProfile);
        } else {
            imgUserProfile.setImageResource(R.drawable.baseline_login_24);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserProfileImage();
    }

    // 🔥 Initial Data Load
    private void loadListings() {
        progressBarMain.setVisibility(View.VISIBLE);
        listingList.clear();
        fullListingList.clear();
        adapter.notifyDataSetChanged();

        isLastItemReached = false;
        lastVisible = null;

        Query q = db.collection("listings");
        if (!currentFilter.equals("all")) {
            q = q.whereEqualTo("type", currentFilter);
        }

        q.limit(PAGE_SIZE).get()
                .addOnSuccessListener(snapshots -> {
                    progressBarMain.setVisibility(View.GONE);
                    if (snapshots.isEmpty()) {
                        Toast.makeText(this, "No Ads Found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
                    if (snapshots.size() < PAGE_SIZE) isLastItemReached = true;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            Listing item = doc.toObject(Listing.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                listingList.add(item);
                                fullListingList.add(item);
                            }
                        } catch (Exception e) {
                            Log.e("MANDAR_DEBUG", "Object Mapping Error: ", e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBarMain.setVisibility(View.GONE);
                    Toast.makeText(this, "Check Network", Toast.LENGTH_SHORT).show();
                });
    }

    // 🔥 Load More Data on Scroll
    private void loadMoreListings() {
        isLoading = true;

        Query q = db.collection("listings");
        if (!currentFilter.equals("all")) {
            q = q.whereEqualTo("type", currentFilter);
        }

        q.startAfter(lastVisible).limit(PAGE_SIZE).get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        isLastItemReached = true;
                        isLoading = false;
                        return;
                    }

                    lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
                    if (snapshots.size() < PAGE_SIZE) isLastItemReached = true;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            Listing item = doc.toObject(Listing.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                listingList.add(item);
                                fullListingList.add(item);
                            }
                        } catch (Exception e) {
                            Log.e("MANDAR_DEBUG", "Object Mapping Error: ", e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    isLoading = false;
                })
                .addOnFailureListener(e -> isLoading = false);
    }
}