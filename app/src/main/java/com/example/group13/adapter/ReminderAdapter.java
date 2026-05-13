package com.example.group13.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.model.Reminder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private final Context context;
    private final List<Reminder> reminderList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ReminderAdapter(Context context, List<Reminder> reminderList) {
        this.context = context;
        this.reminderList = reminderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Reminder r = reminderList.get(position);

        holder.tvTitle.setText(r.getTitle());

        Date date = new Date(r.getDeadlineMillis());
        holder.tvDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date));
        holder.tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date));

        updateUI(holder, r);

        holder.circleCheck.setOnClickListener(v -> {

            if (r.getId() == null) {
                Toast.makeText(context, "Invalid reminder id", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean newState = !r.isDone();
            r.setDone(newState);
            updateUI(holder, r);

            db.collection("reminders")
                    .document(r.getId())
                    .update("done", newState);
        });
    }

    private void updateUI(ViewHolder holder, Reminder r) {

        // Done / Undone
        if (r.isDone()) {
            holder.circleCheck.setBackgroundResource(R.drawable.circle_checked);
            holder.tvTitle.setAlpha(0.5f);
        } else {
            holder.circleCheck.setBackgroundResource(R.drawable.circle_unchecked);
            holder.tvTitle.setAlpha(1f);
        }

        // 🎯 PRIORITY COLOR
        switch (r.getPriority()) {
            case "High":
                holder.statusBar.setBackgroundColor(context.getColor(R.color.red));
                break;
            case "Medium":
                holder.statusBar.setBackgroundColor(context.getColor(R.color.orange));
                break;
            case "Low":
                holder.statusBar.setBackgroundColor(context.getColor(R.color.green));
                break;
            default:
                holder.statusBar.setBackgroundColor(context.getColor(R.color.gray));
        }
    }


    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDate, tvTime;
        View circleCheck, statusBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            circleCheck = itemView.findViewById(R.id.circleCheck);
            statusBar = itemView.findViewById(R.id.statusBar);
        }
    }
}