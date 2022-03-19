package com.igm.badhoc.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bridgefy.sdk.client.Device;
import com.igm.badhoc.R;
import com.igm.badhoc.listener.ItemClickListener;
import com.igm.badhoc.model.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NeighborsAdapter extends RecyclerView.Adapter<NeighborsAdapter.ViewHolder> implements Serializable {

    private final List<Node> neighbors;
    private ItemClickListener mClickListener;

    public NeighborsAdapter() {
        this.neighbors = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return neighbors.size();
    }

    public void addNeighbor(Node node) {
        int position = getNeighborPosition(node.getId());
        if (position > -1) {
            this.neighbors.set(position, node);
            notifyItemChanged(position);
        } else {
            this.neighbors.add(node);
            notifyItemInserted(this.neighbors.size() - 1);
        }
    }

    public void removeNeighbor(Device lostNeighbor) {
        int position = getNeighborPosition(lostNeighbor.getUserId());
        if (position > -1) {
            Node node = this.neighbors.get(position);
            node.setNearby(false);
            this.neighbors.set(position, node);
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

    public List<Node> getNeighbors() {
        return neighbors;
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
            mContentView = view.findViewById(R.id.device_name);
            itemView.setOnClickListener(view1 -> mClickListener.onItemClick(view1, getAdapterPosition()));
        }

        void setNeighbor(Node node) {
            this.mContentView.setText(node.getDeviceName());
            if (node.isNearby()) {
                this.mAvatar.setImageResource(R.drawable.user_nearby);
                this.mContentView.setTextColor(Color.BLACK);
            } else {
                this.mAvatar.setImageResource(R.drawable.user_not_nearby);
                this.mContentView.setTextColor(Color.LTGRAY);
            }
        }
    }
}