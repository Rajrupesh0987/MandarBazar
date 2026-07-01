package com.rupesh.mandarbazar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatSummaryAdapter extends RecyclerView.Adapter<ChatSummaryAdapter.ViewHolder> {
    private Context context;
    private List<ChatSummary> chatList;

    public ChatSummaryAdapter(Context context, List<ChatSummary> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // हम यहाँ simple list item use कर रहे हैं
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatSummary chat = chatList.get(position);
        holder.text1.setText(chat.getAdTitle());
        holder.text2.setText("Click to open conversation");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("adTitle", chat.getAdTitle());
            intent.putExtra("receiverId", chat.getReceiverId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}