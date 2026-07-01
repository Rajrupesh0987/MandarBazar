package com.rupesh.mandarbazar;

import java.util.Date;

public class ChatMessage {
    private String text;
    private String senderId;
    private Date createdAt;

    public ChatMessage() {} // Need for Firebase

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}