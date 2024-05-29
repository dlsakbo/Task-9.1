package com.example.sit708_task_91p;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

public class LostFoundAdapter extends RecyclerView.Adapter<LostFoundAdapter.ViewHolder> {

    private final List<LostFoundItem> lostFoundItems;
    private final LayoutInflater inflater;
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {

        void onItemClick(int position);

    }

    // Constructor that is used to initialize the adapter with context, item list, and click listener

    public LostFoundAdapter(Context context, List<LostFoundItem> items, OnItemClickListener listener) {

        this.lostFoundItems = items;
        this.inflater = LayoutInflater.from(context);
        this.clickListener = listener;

    }

    // Creates the new item view and then returns its ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_lost_found, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        LostFoundItem item = lostFoundItems.get(position);
        holder.titleTextView.setText(item.getTitle());

    }

    // Returns total number of items in list
    @Override
    public int getItemCount() {

        return lostFoundItems.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.itemTitleTextView);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(position);
                }
            });
        }

    }

    // Remove item from list and adpter is notified
    public void removeItem(int position) {

        lostFoundItems.remove(position);
        notifyItemRemoved(position);
        saveItems(inflater.getContext());

    }

    // Save current items list to the SharedPreferences
    private void saveItems(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("LostFoundPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(lostFoundItems);
        editor.putString("lostFoundItems", json);
        editor.apply();

    }
}
