package com.example.group13.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.model.Employee;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private Context context;
    private List<Employee> employeeList;
    private OnEmployeeActionListener listener;

    public void setOnEmployeeActionListener(OnEmployeeActionListener listener) {
        this.listener = listener;
    }

    public EmployeeAdapter(Context context, List<Employee> employeeList) {
        this.context = context;
        this.employeeList = employeeList;
    }

    public void updateList(List<Employee> newList) {
        this.employeeList = newList;
        notifyDataSetChanged();
    }

    public interface OnEmployeeActionListener {
        void onView(Employee employee);
        void onEdit(Employee employee);
        void onDelete(Employee employee);
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_employee_card, parent, false);
        return new EmployeeViewHolder(view);

    }

    @Override
    public void onBindViewHolder(
            @NonNull EmployeeViewHolder holder, int position) {

        Employee e = employeeList.get(position);

        holder.tvName.setText(
                e.getEmployeeName() != null ? e.getEmployeeName() : ""
        );

        holder.tvPosition.setText(
                e.getPosition() != null ? e.getPosition() : ""
        );

        holder.tvPhone.setText(
                e.getPhone() != null ? "📞 " + e.getPhone() : ""
        );

        holder.tvEmail.setText(
                e.getEmail() != null ? "✉️ " + e.getEmail() : ""
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onView(e);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(e);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(e);
        });
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPosition, tvPhone, tvEmail;
        ImageView btnEdit, btnDelete;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnEdit.setClickable(true);
            btnDelete.setClickable(true);

        }
    }
}