package com.igm.badhoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igm.badhoc.R;
import com.igm.badhoc.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>  {

    private final List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mDate.setText(notifications.get(position).getDate());
        holder.mText.setText(notifications.get(position).getText());
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mDate;
        private final TextView mText;

        ViewHolder(View view) {
            super(view);
            mDate = view.findViewById(R.id.time_notif);
            mText = view.findViewById(R.id.notification);
        }

    }
}
