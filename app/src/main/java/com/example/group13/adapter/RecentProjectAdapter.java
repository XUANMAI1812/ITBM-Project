package com.example.group13.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.activity.ProjectDetailActivity;
import com.example.group13.model.Project;

import java.util.List;

public class RecentProjectAdapter extends RecyclerView.Adapter<RecentProjectAdapter.ProjectViewHolder> {

    private final Context context;
    private final List<Project> projectList;

    public RecentProjectAdapter(Context context, List<Project> projectList) {
        this.context = context;
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_project_card, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);

        holder.tvTitle.setText(project.getName());
        holder.tvDesc.setText(project.getDescription() != null ? project.getDescription() : "");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProjectDetailActivity.class);
            intent.putExtra("PROJECT_ID", project.getProjectId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
        }
    }
}