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

public class NeighborsFragment extends Fragment implements ItemClickListener {

    private RecyclerView neighborsRecyclerView;
    private NeighborsAdapter neighborsAdapter;
    private List<Node> neighbors;

    public static NeighborsFragment newInstance(Bundle bundle) {
        NeighborsFragment fragment = new NeighborsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.neighbor_fragment, container, false);

        // recover our Neighbor object
        neighbors = new ArrayList<>();

        neighborsAdapter = new NeighborsAdapter(neighbors);
        neighborsAdapter.setClickListener(this);

        neighborsRecyclerView = view.findViewById(R.id.peer_list);
        neighborsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        neighborsRecyclerView.setAdapter(neighborsAdapter);

        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        Node node = neighbors.get(position);
        ((MainActivity) getActivity()).onItemClick(node.getId());
    }

    public void addNeighborToConversations(Node node) {
        neighborsAdapter.addNeighbor(node);
    }

    public void removeNeighborFromConversations(Device lostNeighbor) {
        neighborsAdapter.removeNeighbor(lostNeighbor);
    }
}
