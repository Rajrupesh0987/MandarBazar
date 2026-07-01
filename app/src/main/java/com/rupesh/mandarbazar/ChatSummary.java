package com.rupesh.mandarbazar;

public class ChatSummary {
    private String chatId;
    private String adTitle;
    private String receiverId;

    public ChatSummary() {}

    public ChatSummary(String chatId, String adTitle, String receiverId) {
        this.chatId = chatId;
        this.adTitle = adTitle;
        this.receiverId = receiverId;
    }

    public String getChatId() { return chatId; }
    public String getAdTitle() { return adTitle; }
    public String getReceiverId() { return receiverId; }
}