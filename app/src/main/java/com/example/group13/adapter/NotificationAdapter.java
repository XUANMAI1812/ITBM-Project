package com.example.group13.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> list;

    public NotificationAdapter(List<Notification> list) {
        this.list = list;
    }
    public void updateData(List<Notification> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification item = list.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvContent.setText(item.getContent());
        holder.tvTime.setText(item.getTime());

        // Set icon theo loại notification (nếu muốn)
        switch (item.getTitle()) {
            case "Check In":
                holder.imgIcon.setImageResource(R.drawable.ic_bell);
                break;
            case "Check Out":
                holder.imgIcon.setImageResource(R.drawable.ic_bell);
                break;
            case "Leave Request":
                holder.imgIcon.setImageResource(R.drawable.ic_bell);
                break;
            case "Overtime":
                holder.imgIcon.setImageResource(R.drawable.ic_bell);
                break;
            default:
                holder.imgIcon.setImageResource(R.drawable.ic_bell);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView tvTitle, tvContent, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}