package com.example.group13.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.model.Cost;

import java.util.ArrayList;

public class CostAdapter extends RecyclerView.Adapter<CostAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Cost cost);
    }

    private final ArrayList<Cost> list;
    private final OnItemClickListener listener;

    public CostAdapter(ArrayList<Cost> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cost, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Cost c = list.get(position);
        h.tvName.setText(c.getName());
        h.tvAmount.setText(c.getAmount() + " đ");

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCostName);
            tvAmount = itemView.findViewById(R.id.tvCostAmount);
        }
    }
}