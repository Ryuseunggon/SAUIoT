package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewHolder> {

    private ArrayList<TimeInfo> timeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TimeAdapter(ArrayList<TimeInfo> timeList) {
        this.timeList = timeList;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_item, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        TimeInfo timeInfo = timeList.get(position);
        holder.timeText.setText(timeInfo.getHour() + ":" + timeInfo.getMinute());
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < timeList.size()) {
            timeList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class TimeViewHolder extends RecyclerView.ViewHolder {
        public TextView timeText;
        public ImageButton deleteButton;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.timeItemText);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }


    }
}
