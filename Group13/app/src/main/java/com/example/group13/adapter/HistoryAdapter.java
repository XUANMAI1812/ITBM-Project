package com.example.group13.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.group13.R;
import com.example.group13.model.HistoryItem;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private Context context;
    private List<HistoryItem> historyList;

    public HistoryAdapter(Context context, List<HistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @Override
    public int getCount() {
        return historyList.size();
    }

    @Override
    public Object getItem(int position) {
        return historyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Cập nhật dữ liệu và refresh ListView
    public void updateData(List<HistoryItem> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
            holder = new ViewHolder();
            holder.tvType = convertView.findViewById(R.id.tvType);
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HistoryItem item = historyList.get(position);
        holder.tvType.setText(item.getType());
        holder.tvDate.setText(item.getDate());

        return convertView;
    }

    private static class ViewHolder {
        TextView tvType, tvDate;
    }
}