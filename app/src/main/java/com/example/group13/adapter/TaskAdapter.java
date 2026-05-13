package com.example.group13.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.activity.TaskDetailActivity;
import com.example.group13.model.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private List<String> taskIdList;
    private FirebaseFirestore db;

    private Map<String, String> employeeNameCache = new HashMap<>();

    public TaskAdapter(Context context, List<Task> taskList, List<String> taskIdList) {
        this.context = context;
        this.taskList = taskList;
        this.taskIdList = taskIdList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        Task task = taskList.get(position);
        String taskId = taskIdList.get(position);

        holder.tvTaskTitle.setText(task.getTitle());

        String employeeId = task.getEmployeeId();

        if (employeeId == null) {
            holder.tvAssignee.setText("Unassigned");
        } else if (employeeNameCache.containsKey(employeeId)) {
            holder.tvAssignee.setText(employeeNameCache.get(employeeId));
        } else {
            holder.tvAssignee.setText("Loading...");
            db.collection("users")
                    .document(employeeId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("employeeName");
                        if (name != null) {
                            employeeNameCache.put(employeeId, name);
                            holder.tvAssignee.setText(name);
                        }
                    });
        }

        if (task.getDeadline() > 0) {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvDeadline.setText(
                    sdf.format(new Date(task.getDeadline()))
            );
        } else {
            holder.tvDeadline.setText("No deadline");
        }

        holder.checkboxDone.setOnCheckedChangeListener(null);
        holder.checkboxDone.setChecked(task.isDone());
        holder.tvTaskTitle.setAlpha(task.isDone() ? 0.5f : 1f);

        holder.checkboxDone.setOnCheckedChangeListener((btn, isChecked) -> {
            task.setDone(isChecked);
            holder.tvTaskTitle.setAlpha(isChecked ? 0.5f : 1f);

            db.collection("tasks")
                    .document(taskId)
                    .update("done", isChecked)
                    .addOnFailureListener(e ->
                            Toast.makeText(context,
                                    "Failed to update status",
                                    Toast.LENGTH_SHORT).show()
                    );
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaskDetailActivity.class);
            intent.putExtra("taskId", taskId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void update(List<Task> newTaskList, List<String> newTaskIdList) {
        this.taskList = newTaskList;
        this.taskIdList = newTaskIdList;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView tvTaskTitle, tvAssignee, tvDeadline;
        CheckBox checkboxDone;
        ImageView imgAssignee;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvAssignee = itemView.findViewById(R.id.tvAssignee);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            checkboxDone = itemView.findViewById(R.id.checkboxDone);
            imgAssignee = itemView.findViewById(R.id.imgAssignee);
        }
    }
}