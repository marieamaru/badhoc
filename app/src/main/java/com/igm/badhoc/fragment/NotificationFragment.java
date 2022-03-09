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
import android.widget.ImageView;
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
import com.igm.badhoc.util.DeviceUtil;
import com.igm.badhoc.util.ParserUtil;

/**
 * Fragment that represents the Notifications tab of the application
 */
public class NotificationFragment extends Fragment {

    /**
     * The text view corresponding to the title at the top of the fragment
     */
    private TextView title;
    /**
     * The image corresponding to the connection status
     */
    private ImageView statusIcon;
    /**
     * The intent of the ServerService
     */
    private Intent intentService;
    /**
     * RecyclerView that represents the notifications in the list
     */
    private RecyclerView notificationRecyclerView;
    /**
     * Adapter object that represents the notifications list
     */
    private NotificationAdapter notificationAdapter;

    /**
     * Method that initializes the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.notification_fragment, container, false);
        title = view.findViewById(R.id.txt_server);
        statusIcon = view.findViewById(R.id.status_icon);
        notificationAdapter = new NotificationAdapter();

        notificationRecyclerView = view.findViewById(R.id.notif_list);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        notificationRecyclerView.setAdapter(notificationAdapter);

        intentService = new Intent(requireActivity(), ServerService.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Tag.INTENT_SERVER_SERVICE.value);
        intentFilter.addAction(Tag.INTENT_MAIN_ACTIVITY.value);
        requireActivity().registerReceiver(receiver, intentFilter);
        return view;
    }

    /**
     * Method that adds a notification to the list of notifications in the adapter
     */
    public void addNotification(Notification notification) {
        notificationAdapter.addNotification(notification);
        notificationRecyclerView.scrollToPosition(notificationAdapter.getItemCount() - 1);
    }


    /**
     * Broadcast Receiver object that listens for inputs from the main activity or the ServerService
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            MainActivity mainActivity = (MainActivity) requireActivity();
            if (intent.getAction().equals(Tag.INTENT_SERVER_SERVICE.value)) {
                String notificationAction = intent.getStringExtra(Tag.ACTION_NOTIFICATION_RECEIVED.value);
                if (notificationAction != null) {
                    addNotification(new Notification(notificationAction));
                }
                String connectedAction = intent.getStringExtra(Tag.ACTION_CHANGE_TITLE.value);
                if (connectedAction != null) {
                    title.setText(connectedAction);
                    if (connectedAction.equals(Tag.TITLE_DOMINANT.value)) {
                        statusIcon.setImageResource(R.drawable.ic_dominant);
                    } else {
                        statusIcon.setImageResource(R.drawable.ic_domine);
                    }
                }
            } else if (intent.getAction().equals(Tag.INTENT_MAIN_ACTIVITY.value)) {
                String action = intent.getStringExtra(Tag.ACTION_CONNECT.value);
                if (action.equals("disconnect") && DeviceUtil.isServiceRunning(ServerService.class, requireActivity())) {
                    requireActivity().stopService(intentService);
                } else if (action.equals("connect") && !DeviceUtil.isServiceRunning(ServerService.class, requireActivity())) {
                    intentService.putExtra(Tag.ACTION_UPDATE_NODE_INFO.value, ParserUtil.parseNodeKeepAliveMessage(mainActivity.getNode()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intentService);
                    } else {
                        context.startService(intentService);
                    }
                }
            }
        }
    };

    /**
     * Getter for the BroadcastReceiver object
     */
    public BroadcastReceiver getReceiver() {
        return receiver;
    }

    /**
     * Getter for the ServerService intent
     */
    public Intent getIntentService() {
        return intentService;
    }
}