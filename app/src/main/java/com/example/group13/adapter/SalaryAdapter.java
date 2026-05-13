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
import com.example.group13.model.Salary;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(Salary salary);
        void onEdit(Salary salary);
        void onDelete(Salary salary);
    }

    private Context context;
    private List<Salary> list;
    private OnItemClickListener listener;

    public SalaryAdapter(Context context, List<Salary> list,
                         OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_salary_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Salary s = list.get(pos);

        h.tvName.setText(s.getEmployeeName());
        h.tvPosition.setText(s.getPosition());
        h.tvSalary.setText(formatMoney(s.getTotal()));

        h.itemView.setOnClickListener(v -> listener.onClick(s));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(s));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(s));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPosition, tvSalary;
        ImageView btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvPosition = v.findViewById(R.id.tvPosition);
            tvSalary = v.findViewById(R.id.tvSalary);
            btnEdit = v.findViewById(R.id.btnEditSalary);
            btnDelete = v.findViewById(R.id.btnDeleteSalary);
        }
    }

    private String formatMoney(long value) {
        return NumberFormat
                .getInstance(new Locale("vi", "VN"))
                .format(value) + " ₫";
    }

    public void update(List<Salary> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }
}