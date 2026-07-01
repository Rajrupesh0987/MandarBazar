package com.rupesh.mandarbazar;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private final Context context;
    private List<Listing> listingList;

    public ListingAdapter(Context context, List<Listing> listingList) {
        this.context = context;
        this.listingList = listingList;
    }

    // 🔥 SEARCH LOGIC: List update karne ke liye naya method
    public void updateList(List<Listing> newList) {
        this.listingList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_listing_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Listing item = listingList.get(position);

        holder.tvAdTitle.setText(item.getTitle() != null ? item.getTitle() : "बिना नाम का Ad");
        holder.tvAdPrice.setText(item.getPrice() != null ? "₹ " + item.getPrice() : "₹ 0");

        String imageCode = "";
        if (item.getImage() != null && !item.getImage().isEmpty()) {
            imageCode = item.getImage();
        } else if (item.getImages() != null && !item.getImages().isEmpty()) {
            imageCode = item.getImages().get(0);
        }

        if (!imageCode.isEmpty()) {
            if (imageCode.startsWith("http")) {
                Glide.with(context).load(imageCode).placeholder(android.R.drawable.ic_menu_gallery).into(holder.imgAd);
            } else {
                try {
                    String pureBase64 = imageCode.contains(",") ? imageCode.split(",")[1] : imageCode;
                    byte[] imageBytes = Base64.decode(pureBase64, Base64.DEFAULT);
                    Glide.with(context).asBitmap().load(imageBytes).placeholder(android.R.drawable.ic_menu_gallery).into(holder.imgAd);
                } catch (Exception e) {
                    holder.imgAd.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        } else {
            holder.imgAd.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        final String finalImageCode = imageCode;
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, AdDetailActivity.class);
            intent.putExtra("title", item.getTitle() != null ? item.getTitle() : "No Title");
            intent.putExtra("price", item.getPrice() != null ? item.getPrice() : "0");
            intent.putExtra("desc", item.getDesc() != null ? item.getDesc() : "No description available.");
            intent.putExtra("image", finalImageCode);
            intent.putExtra("phone", item.getPhone() != null ? item.getPhone() : "");
            intent.putExtra("itemId", item.getId() != null ? item.getId() : "");
            intent.putExtra("ownerId", item.getUserId() != null ? item.getUserId() : "");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return listingList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdTitle, tvAdPrice;
        ImageView imgAd;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdTitle = itemView.findViewById(R.id.tvAdTitle);
            tvAdPrice = itemView.findViewById(R.id.tvAdPrice);
            imgAd = itemView.findViewById(R.id.imgAd);
        }
    }
}