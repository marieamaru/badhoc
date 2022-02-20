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

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that represents the Around Me tab of the application
 */
public class AroundMeFragment extends Fragment implements ItemClickListener {
    /**
     * RecyclerView that represents the neighbors around
     */
    private RecyclerView neighborsRecyclerView;
    /**
     * Adapter object that represents the neighbors list
     */
    private NeighborsAdapter neighborsAdapter;
    /**
     * The list of neighbors detected around
     */
    private List<Node> neighbors;

    /**
     * Method that initializes the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.neighbor_fragment, container, false);

        // recover our Neighbor object
        neighbors = new ArrayList<>();

        neighborsAdapter = new NeighborsAdapter(neighbors);
        neighborsAdapter.setClickListener(this);

        neighborsRecyclerView = view.findViewById(R.id.notif_list);
        neighborsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        neighborsRecyclerView.setAdapter(neighborsAdapter);

        return view;
    }

    /**
     * Method that listens for clicks on the devices names
     */
    @Override
    public void onItemClick(View view, int position) {
        Node node = neighbors.get(position);
        ((MainActivity) getActivity()).onItemClick(node.getId());
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
