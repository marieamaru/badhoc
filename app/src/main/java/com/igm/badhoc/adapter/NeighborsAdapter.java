package com.igm.badhoc.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bridgefy.sdk.client.Device;
import com.igm.badhoc.listener.ItemClickListener;
import com.igm.badhoc.R;
import com.igm.badhoc.model.Neighbor;

import java.io.Serializable;
import java.util.List;

public class NeighborsAdapter extends RecyclerView.Adapter<NeighborsAdapter.ViewHolder> implements Serializable {

    private final List<Neighbor> neighbors;
    private ItemClickListener mClickListener;

    public NeighborsAdapter(List<Neighbor> neighbors) {
        this.neighbors = neighbors;
    }

    @Override
    public int getItemCount() {
        return neighbors.size();
    }

    public void addNeighbor(Neighbor neighbor) {
        int position = getNeighborPosition(neighbor.getId());
        if (position > -1) {
            this.neighbors.set(position, neighbor);
            notifyItemChanged(position);
        } else {
            this.neighbors.add(neighbor);
            notifyItemInserted(this.neighbors.size() - 1);
        }
    }

    public void removeNeighbor(Device lostNeighbor) {
        int position = getNeighborPosition(lostNeighbor.getUserId());
        if (position > -1) {
            Neighbor neighbor = this.neighbors.get(position);
            neighbor.setNearby(false);
            this.neighbors.set(position, neighbor);
            notifyItemChanged(position);
        }
    }

    private int getNeighborPosition(String neighborId) {
        for (int i = 0; i < neighbors.size(); i++) {
            if (neighbors.get(i).getId().equals(neighborId))
                return i;
        }
        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.peer_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder neighborHolder, int position) {
        neighborHolder.setNeighbor(neighbors.get(position));
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mContentView;
        private final ImageView mAvatar;

        ViewHolder(View view) {
            super(view);
            mAvatar = view.findViewById(R.id.peerAvatar);
            mContentView = view.findViewById(R.id.peerName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.onItemClick(view, getAdapterPosition());
                    Toast.makeText(view.getContext(), "CLICK ON NEIGHBOR", Toast.LENGTH_SHORT).show();
                }
            });
        }

        void setNeighbor(Neighbor neighbor) {
            /*
            switch (neighbor.getDeviceType()) {
                case ANDROID:
                    this.mContentView.setText(neighbor.getDeviceName() + " (android)");
                    break;

                case IPHONE:
                    this.mContentView.setText(neighbor.getDeviceName() + " (iPhone)");
                    break;
            }
             */
            this.mContentView.setText(neighbor.getDeviceName());
            if (neighbor.isNearby()) {
                this.mAvatar.setImageResource(R.drawable.user_nearby);
                this.mContentView.setTextColor(Color.BLACK);
            } else {
                this.mAvatar.setImageResource(R.drawable.user_not_nearby);
                this.mContentView.setTextColor(Color.LTGRAY);
            }
        }
    }
}