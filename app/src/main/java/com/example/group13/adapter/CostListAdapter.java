package com.example.group13.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.model.Project;

import java.util.ArrayList;

public class CostListAdapter extends RecyclerView.Adapter<CostListAdapter.ViewHolder> {

    Context context;
    ArrayList<Project> list;
    OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onEdit(Project project);
    }

    public CostListAdapter(Context context, ArrayList<Project> list, OnProjectClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cost_project, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project p = list.get(position);

        holder.tvName.setText(p.getName());
        holder.tvDate.setText("Start Date: " + p.getStartDate());

        holder.itemView.setOnClickListener(v -> listener.onEdit(p));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(ArrayList<Project> newList){
        list = newList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProjectName);
            tvDate = itemView.findViewById(R.id.tvProjectDateStart);
        }
    }
}