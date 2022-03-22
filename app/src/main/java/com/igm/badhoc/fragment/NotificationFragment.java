package com.igm.badhoc.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.igm.badhoc.R;
import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.adapter.NotificationAdapter;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.NotificationDisplay;
import com.igm.badhoc.model.Status;
import com.igm.badhoc.model.Tag;
import com.igm.badhoc.model.ToServerNotification;
import com.igm.badhoc.service.ServerService;
import com.igm.badhoc.util.DeviceUtil;
import com.igm.badhoc.util.ParserUtil;

import java.util.HashMap;

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

        FloatingActionButton button = view.findViewById(R.id.to_server_button);
        button.setOnClickListener(this::popupForMessageToServer);

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
    public void addNotification(NotificationDisplay notification) {
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
                    NotificationDisplay notificationDisplay = new NotificationDisplay(notificationAction);
                    notificationDisplay.setDirection(NotificationDisplay.INCOMING_MESSAGE);
                    addNotification(notificationDisplay);
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

    private void popupForMessageToServer(View view) {
        final EditText setTextName = new EditText(view.getContext());
        setTextName.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(view.getContext())
                .setCancelable(true)
                .setTitle("Send a message to server")
                .setView(setTextName)
                .setPositiveButton("Send",
                        (dialog, which) -> {
                            String message = setTextName.getText().toString();
                            if (message.isEmpty() || message.trim().isEmpty()) {
                                Toast.makeText(view.getContext(), "Message is empty", Toast.LENGTH_LONG).show();
                            } else {
                                handleMessageForServer(message);
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> {
                        })
                .create().show();
    }

    /**
     * Method for communication with the server :
     * if the node is dominant, directly request the server service to publish the message,
     * if the node is dominated, sends the message to the dominant node for relay
     *
     * @param message message to send
     */
    private void handleMessageForServer(final String message) {
        MainActivity mainActivity = (MainActivity) requireActivity();
        String dominantMacAddress;
        ToServerNotification toServerNotification;
        String messageToSend;
        if (mainActivity.getNode().isDominant() == Status.DOMINATING.value) {
            dominantMacAddress = mainActivity.getNode().getMacAddress();
            toServerNotification = new ToServerNotification(dominantMacAddress, message);
            messageToSend = ParserUtil.parseMessageForServer(toServerNotification);
            mainActivity.broadcastIntentAction(Tag.ACTION_SEND_MESSAGE_TO_SERVER.value, messageToSend);
        } else {
            if (mainActivity.getNode().getDominant() != null) {
                dominantMacAddress = mainActivity.getNode().getDominant().getMacAddress();
                toServerNotification = new ToServerNotification(dominantMacAddress, message);
                messageToSend = ParserUtil.parseMessageForServer(toServerNotification);
                String dominantId = mainActivity.getNode().getDominant().getId();
                onMessageSend(dominantId, messageToSend);
                Log.e("notif", messageToSend);
            }
        }

        NotificationDisplay messageSentToServer = new NotificationDisplay(message);
        messageSentToServer.setDirection(NotificationDisplay.OUTGOING_MESSAGE);
        addNotification(messageSentToServer);
    }

    /**
     * Method to send a private message to the dominant node to server as relay with the server
     *
     * @param messageToSend message sent to the dominant node
     * @param id            id of the dominant node to send the message to
     */
    public void onMessageSend(final String id, final String messageToSend) {
        if (messageToSend.length() > 0) {
            MessageBadhoc message = new MessageBadhoc(messageToSend);
            message.setDirection(MessageBadhoc.OUTGOING_MESSAGE);

            HashMap<String, Object> content = new HashMap<>();
            content.put(Tag.PAYLOAD_TEXT.value, messageToSend);
            content.put(Tag.PAYLOAD_PRIVATE_TYPE.value, Tag.PAYLOAD_MESSAGE_TO_SERVER.value);
            Message.Builder builder = new Message.Builder();
            builder.setContent(content).setReceiverId(id);

            Bridgefy.sendMessage(builder.build(),
                    BFEngineProfile.BFConfigProfileLongReach);
        }
    }

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