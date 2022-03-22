package com.igm.badhoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igm.badhoc.R;
import com.igm.badhoc.model.NotificationDisplay;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<NotificationDisplay> notifications;

    public NotificationAdapter() {
        this.notifications = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void addNotification(NotificationDisplay notification) {
        this.notifications.add(notification);
        notifyItemInserted(this.notifications.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return notifications.get(position).getDirection();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View messageView = null;

        switch (viewType) {
            case NotificationDisplay.INCOMING_MESSAGE:
                messageView = LayoutInflater.from(parent.getContext()).
                        inflate((R.layout.notification_row_incoming), parent, false);
                break;
            case NotificationDisplay.OUTGOING_MESSAGE:
                messageView = LayoutInflater.from(parent.getContext()).
                        inflate((R.layout.notification_row_outgoing), parent, false);
                break;
        }
        return new ViewHolder(messageView);
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
