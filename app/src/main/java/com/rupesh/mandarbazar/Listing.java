package com.rupesh.mandarbazar;

import java.util.Date;
import java.util.List;

public class Listing {
    private String id, type, category, title, price, location, phone, desc, userId, userName, image;
    private boolean isPinned;
    // 🔥new date() from js
    private Date timestamp;
    private List<String> images;

    public Listing() {} // Firebase के लिए

    public String getId() { return id; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getLocation() { return location; }
    public String getPhone() { return phone; }
    public String getDesc() { return desc; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getImage() { return image; }
    public boolean isPinned() { return isPinned; }
    public Date getTimestamp() { return timestamp; }
    public List<String> getImages() { return images; }

    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(String price) { this.price = price; }
    public void setLocation(String location) { this.location = location; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDesc(String desc) { this.desc = desc; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setImage(String image) { this.image = image; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setImages(List<String> images) { this.images = images; }
}