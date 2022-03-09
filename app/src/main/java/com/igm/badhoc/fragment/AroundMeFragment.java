package com.igm.badhoc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bridgefy.sdk.client.Device;
import com.igm.badhoc.R;
import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.adapter.NeighborsAdapter;
import com.igm.badhoc.listener.ItemClickListener;
import com.igm.badhoc.model.Node;

/**
 * Fragment that represents the Around Me tab of the application
 */
public class AroundMeFragment extends Fragment implements ItemClickListener {
    /**
     * Adapter object that represents the neighbors list
     */
    private NeighborsAdapter neighborsAdapter;

    /**
     * Method that initializes the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.neighbor_fragment, container, false);

        neighborsAdapter = new NeighborsAdapter();
        neighborsAdapter.setClickListener(this);

        RecyclerView neighborsRecyclerView = view.findViewById(R.id.notif_list);
        neighborsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        neighborsRecyclerView.setAdapter(neighborsAdapter);

        return view;
    }

    /**
     * Method that listens for clicks on the devices names
     */
    @Override
    public void onItemClick(View view, int position) {
        Node node = neighborsAdapter.getNeighbors().get(position);
        ((MainActivity) requireActivity()).onItemClick(node.getId());
    }

    /**
     * Method that adds a neighbor to the list in the adapter and updates it
     */
    public void addNeighborToConversations(Node node) {
        neighborsAdapter.addNeighbor(node);
    }

    /**
     * Method that removes a neighbor from the list in the adapter and updates it
     */
    public void removeNeighborFromConversations(Device lostNeighbor) {
        neighborsAdapter.removeNeighbor(lostNeighbor);
    }
}
