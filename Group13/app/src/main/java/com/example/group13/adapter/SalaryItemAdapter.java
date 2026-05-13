package com.example.group13.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.model.SalaryItem;

import java.util.List;

public class SalaryItemAdapter
        extends RecyclerView.Adapter<SalaryItemAdapter.ViewHolder> {

    public interface OnItemChangedListener {
        void onChanged();
    }

    private List<SalaryItem> items;
    private OnItemChangedListener listener;

    public SalaryItemAdapter(List<SalaryItem> items,
                             OnItemChangedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_salary, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        SalaryItem item = items.get(pos);

        h.edtName.setText(item.getName());
        h.edtAmount.setText(item.getAmount() == 0 ? "" : String.valueOf(item.getAmount()));

        h.edtName.addTextChangedListener(new SimpleWatcher(s ->
                item.setName(s)));

        h.edtAmount.addTextChangedListener(new SimpleWatcher(s -> {
            try {
                item.setAmount(Long.parseLong(s));
            } catch (Exception e) {
                item.setAmount(0);
            }
            if (listener != null) listener.onChanged();
        }));

        h.btnDelete.setOnClickListener(v -> {
            items.remove(pos);
            notifyItemRemoved(pos);
            if (listener != null) listener.onChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText edtName, edtAmount;
        ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            edtName = v.findViewById(R.id.edtSalaryName);
            edtAmount = v.findViewById(R.id.edtSalaryAmount);
            btnDelete = v.findViewById(R.id.btnDeleteSalary);
        }
    }

    static class SimpleWatcher implements TextWatcher {

        interface Callback {
            void call(String s);
        }

        Callback cb;

        SimpleWatcher(Callback cb) {
            this.cb = cb;
        }

        @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
        @Override public void afterTextChanged(Editable e){}

        @Override
        public void onTextChanged(CharSequence s,int a,int b,int c){
            cb.call(s.toString());
        }
    }
}