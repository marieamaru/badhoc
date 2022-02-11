package com.igm.badhoc.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igm.badhoc.R;
import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.adapter.NeighborsAdapter;
import com.igm.badhoc.adapter.NotificationAdapter;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.Notification;
import com.igm.badhoc.model.Tag;
import com.igm.badhoc.service.ServerService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationFragment extends Fragment {

    private final String TAG = "NotificationFragment";
    private TextView title;
    private Intent intentService;
    private RecyclerView notificationRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notifications;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.notification_fragment, container, false);
        title = view.findViewById(R.id.txt_server);

        notifications = new ArrayList<>();
        notifications.add(new Notification("ce soir ", "première notification"));
        notificationAdapter = new NotificationAdapter(notifications);

        notificationRecyclerView = view.findViewById(R.id.notif_list);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        notificationRecyclerView.setAdapter(notificationAdapter);

        intentService = new Intent(getActivity(), ServerService.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Tag.INTENT_SERVER_SERVICE.value);
        intentFilter.addAction(Tag.INTENT_MAIN_ACTIVITY.value);
        getActivity().registerReceiver(receiver, intentFilter);
        return view;
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Tag.INTENT_SERVER_SERVICE.value)) {
                //title.setText(intent.getStringExtra("publish"));
            } else if (intent.getAction().equals(Tag.INTENT_MAIN_ACTIVITY.value)) {
                String action = intent.getStringExtra(Tag.ACTION_CONNECT.value);
                MainActivity mainActivity = (MainActivity) getActivity();
                if (action.equals("disconnect") && mainActivity.isServiceRunning(ServerService.class)) {
                    getActivity().stopService(intentService);
                    boolean isStillRunning = mainActivity.isServiceRunning(ServerService.class);
                    title.setText("no longer connected to server " + isStillRunning);
                } else if (action.equals("connect") && !mainActivity.isServiceRunning(ServerService.class)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getActivity().startForegroundService(intentService);
                    }
                    boolean isStillRunning = mainActivity.isServiceRunning(ServerService.class);
                    title.setText("is now connected to server " + isStillRunning);
                }
            }
        }
    };
}