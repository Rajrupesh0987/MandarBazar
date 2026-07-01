package com.rupesh.mandarbazar;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private FirebaseFirestore db;
    private String currentUserId, chatId, receiverId, adTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        // Getting data from Intent
        chatId = getIntent().getStringExtra("chatId");
        receiverId = getIntent().getStringExtra("receiverId");
        adTitle = getIntent().getStringExtra("adTitle");

        // View Binding
        Toolbar toolbar = findViewById(R.id.chatToolbar);
        TextView tvChatTitle = findViewById(R.id.tvChatTitle);
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            tvChatTitle.setText(adTitle != null ? adTitle : "Chat");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // RecyclerView Setup
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Naye messages niche dikhenge
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);

        // 🔥 KEYBOARD FIX: Scroll to bottom when keyboard opens
        rvChat.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                if (messageList.size() > 0) {
                    rvChat.smoothScrollToPosition(messageList.size() - 1);
                }
            }
        });

        if (chatId != null) { loadMessages(); }
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                ChatMessage msg = dc.getDocument().toObject(ChatMessage.class);
                                messageList.add(msg);
                                chatAdapter.notifyItemInserted(messageList.size() - 1);
                                rvChat.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty() || chatId == null) return;
        etMessage.setText("");

        Map<String, Object> message = new HashMap<>();
        message.put("text", text);
        message.put("senderId", currentUserId);
        message.put("createdAt", FieldValue.serverTimestamp());

        db.collection("chats").document(chatId).collection("messages").add(message);

        Map<String, Object> chatUpdate = new HashMap<>();
        chatUpdate.put("updatedAt", FieldValue.serverTimestamp());
        chatUpdate.put("unreadBy", receiverId);
        db.collection("chats").document(chatId).update(chatUpdate);
    }
}