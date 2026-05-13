package com.example.group13.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.activity.ProjectAddEditActivity;
import com.example.group13.activity.ProjectDetailActivity;
import com.example.group13.model.Project;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private final Context context;
    private final List<Project> projectList;
    private final List<String> projectDocIds;
    private final FirebaseFirestore db;

    private final Map<String, String> userNameCache = new HashMap<>();

    private final String currentUserId;
    private final boolean isAdmin;

    public ProjectAdapter(
            Context context,
            List<Project> projectList,
            List<String> projectDocIds,
            String currentUserId,
            boolean isAdmin
    ) {
        this.context = context;
        this.projectList = projectList;
        this.projectDocIds = projectDocIds;
        this.currentUserId = currentUserId;
        this.isAdmin = isAdmin;
        this.db = FirebaseFirestore.getInstance();

        loadUserCache();
    }

    private boolean canManage(Project project) {
        if (isAdmin) return true;
        return project.getManagerId() != null
                && project.getManagerId().equals(currentUserId);
    }


    private void loadUserCache() {
        db.collection("users")
                .get()
                .addOnSuccessListener(qs -> {
                    for (var doc : qs.getDocuments()) {
                        String name = doc.getString("employeeName");
                        if (name != null) {
                            userNameCache.put(doc.getId(), name);
                        }
                    }
                    notifyDataSetChanged();
                });
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_project_card, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProjectViewHolder holder,
            int position
    ) {

        Project project = projectList.get(position);
        String docId = projectDocIds.get(position);

        holder.tvProjectName.setText(project.getName());

        String managerName = userNameCache.get(project.getManagerId());
        holder.tvProjectManager.setText(
                managerName != null
                        ? "Manager: " + managerName
                        : "Manager: Loading..."
        );

        holder.tvProjectDeadline.setText(
                "Deadline: " + project.getEndDate()
        );

        boolean canManage = canManage(project);

        holder.btnEditProject.setEnabled(canManage);
        holder.btnDeleteProject.setEnabled(canManage);

        holder.btnEditProject.setAlpha(canManage ? 1f : 0.3f);
        holder.btnDeleteProject.setAlpha(canManage ? 1f : 0.3f);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProjectDetailActivity.class);
            intent.putExtra("projectId", docId);
            context.startActivity(intent);
        });

        holder.btnEditProject.setOnClickListener(v -> {
            if (!canManage) {
                Toast.makeText(
                        context,
                        "You do not have permission to access this function",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(context, ProjectAddEditActivity.class);
            intent.putExtra("projectId", docId);
            context.startActivity(intent);
        });

        holder.btnDeleteProject.setOnClickListener(v -> {
            if (!canManage) {
                Toast.makeText(
                        context,
                        "You do not have permission to access this function",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            db.collection("projects")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(
                                context,
                                "Project deleted",
                                Toast.LENGTH_SHORT
                        ).show();

                        int index = holder.getAdapterPosition();
                        if (index != RecyclerView.NO_POSITION) {
                            projectList.remove(index);
                            projectDocIds.remove(index);
                            notifyItemRemoved(index);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    context,
                                    "Delete failed",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {

        TextView tvProjectName, tvProjectManager, tvProjectDeadline;
        ImageView btnEditProject, btnDeleteProject;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectManager = itemView.findViewById(R.id.tvProjectManager);
            tvProjectDeadline = itemView.findViewById(R.id.tvProjectDeadline);
            btnEditProject = itemView.findViewById(R.id.btnEditProject);
            btnDeleteProject = itemView.findViewById(R.id.btnDeleteProject);
        }
    }
}