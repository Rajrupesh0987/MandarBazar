package com.rupesh.mandarbazar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.loginProgress);
        MaterialButton btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        // 1. Google Sign-In Options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 2. Button Click
        btnGoogleLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // 3. Result Handle
    private final // 3. Result Handle Karna
     ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Login API Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    // 🔥 NAYA: Pata chalega ki cancel hua hai ya fail
                    Toast.makeText(this, "Login Cancel ho gaya ya fail hua (Check SHA-1)", Toast.LENGTH_LONG).show();
                }
            }
    );

    // 4. Firebase Authentication & Firestore Entry (JS Logic)
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Auth Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔥 5. NAYA LOGIC: User ko 'users' collection me save karna
    private void saveUserToFirestore(FirebaseUser user) {
        String uid = user.getUid();

        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    // Agar user pehli baar aaya hai, to naya document banao (Web jaisa)
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", user.getEmail());
                    userData.put("favorites", new ArrayList<String>()); // Only favorites list
                    userData.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("users").document(uid).set(userData)
                            .addOnSuccessListener(aVoid -> proceedToMain(user))
                            .addOnFailureListener(e -> proceedToMain(user)); // Fail ho to bhi aage badho
                } else {
                    // old user
                    proceedToMain(user);
                }
            } else {
                proceedToMain(user);
            }
        });
    }

    private void proceedToMain(FirebaseUser user) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}