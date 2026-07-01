package com.rupesh.mandarbazar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatMessage> messageList;
    private final String currentUserId;

    public ChatAdapter(Context context, List<ChatMessage> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setPadding(35, 20, 35, 20);
        textView.setTextSize(16f);
        textView.setMaxWidth(800); // for prevent from long message
        return new ChatViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        holder.textView.setText(msg.getText());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.textView.getLayoutParams();

        // सुंदर गोल किनारे वाला बैकग्राउंड (Bubble)
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(30);

        // 🔥 LOGIC FIX: trim() lagaya taaki spaces ka chakkar na rahe
        if (msg.getSenderId() != null && currentUserId != null &&
                msg.getSenderId().trim().equals(currentUserId.trim())) {

            //  (our Side message)
            params.gravity = Gravity.END;
            params.setMargins(100, 8, 20, 8);
            shape.setColor(Color.parseColor("#DCF8C6")); // WhatsApp Green
            holder.textView.setTextColor(Color.BLACK);
        } else {
            // other side message
            params.gravity = Gravity.START;
            params.setMargins(20, 8, 100, 8);
            shape.setColor(Color.parseColor("#FFFFFF")); // White
            holder.textView.setTextColor(Color.BLACK);
        }

        holder.textView.setBackground(shape);
        holder.textView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}