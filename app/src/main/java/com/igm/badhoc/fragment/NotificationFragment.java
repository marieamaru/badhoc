package com.igm.badhoc.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.igm.badhoc.adapter.NotificationAdapter;
import com.igm.badhoc.model.Notification;
import com.igm.badhoc.model.Tag;
import com.igm.badhoc.service.ServerService;

import java.util.ArrayList;
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

    public void addNotification(Notification notification) {
        notificationAdapter.addNotification(notification);
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (intent.getAction().equals(Tag.INTENT_SERVER_SERVICE.value)) {
                String notificationAction = intent.getStringExtra(Tag.ACTION_MESSAGE_RECEIVED.value);
                if (notificationAction != null) {
                    addNotification(new Notification(notificationAction));
                }
                String connectedAction = intent.getStringExtra(Tag.ACTION_CHANGE_TITLE.value);
                if (connectedAction != null) {
                    title.setText(connectedAction);
                }
            } else if (intent.getAction().equals(Tag.INTENT_MAIN_ACTIVITY.value)) {
                String action = intent.getStringExtra(Tag.ACTION_CONNECT.value);
                if (action.equals("disconnect") && mainActivity.isServiceRunning(ServerService.class)) {
                    requireActivity().stopService(intentService);
                } else if (action.equals("connect") && !mainActivity.isServiceRunning(ServerService.class)) {
                    intentService.putExtra(Tag.ACTION_UPDATE_NODE_INFO.value, mainActivity.getNode().nodeKeepAliveMessage());
                    context.startService(intentService);
                }
            }
        }
    };
}