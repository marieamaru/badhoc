package com.igm.badhoc.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Message;
import com.igm.badhoc.R;
import com.igm.badhoc.adapter.MessagesBadhocAdapter;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Fragment that represents the Broadcast tab of the application
 */
public class BroadcastChatFragment extends Fragment {

    private final String TAG = "BroadcastChatFragment";
    /**
     * RecyclerView that represents the messages in the list
     */
    private RecyclerView broadcastRecyclerView;
    /**
     * Adapter object that represents the messages list
     */
    private MessagesBadhocAdapter messagesBadhocAdapter;
    /**
     * The list of messages sent and received
     */
    private List<MessageBadhoc> badhocMessages;
    /**
     * The id of the Broadcast conversation
     */
    private String conversationId;
    /**
     * The text zone corresponding to where the message is edited
     */
    private EditText txtMessage;
    /**
     * The image on the send button of the fragment
     */
    private ImageView btnSend;
    /**
     * The image on the image button of the fragment
     */
    private ImageView btnImage;
    private ProgressBar progressBar;

    /**
     * Method that initializes the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_chat, container, false);
        txtMessage = view.findViewById(R.id.txtMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnImage = view.findViewById(R.id.btnImage);
        broadcastRecyclerView = view.findViewById(R.id.message_list);
        progressBar = view.findViewById(R.id.progressBar);
        btnSend.setOnClickListener(this::onMessageSend);
        btnImage.setVisibility(View.GONE);
        conversationId = Tag.BROADCAST_CHAT.value;
        badhocMessages = new ArrayList<>();
        broadcastRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        messagesBadhocAdapter = new MessagesBadhocAdapter(badhocMessages, conversationId);
        broadcastRecyclerView.setAdapter(messagesBadhocAdapter);
        return view;
    }

    /**
     * Method to send a message to other devices using Bridgefy
     */
    public void onMessageSend(View v) {
        txtMessage.onEditorAction(EditorInfo.IME_ACTION_DONE);
        String messageString = txtMessage.getText().toString().trim();
        if (messageString.length() > 0) {
            txtMessage.setText("");
            MessageBadhoc message = new MessageBadhoc(messageString);
            message.setDirection(MessageBadhoc.OUTGOING_MESSAGE);

            HashMap<String, Object> content = new HashMap<>();
            content.put(Tag.PAYLOAD_TEXT.value, messageString);
            content.put(Tag.PAYLOAD_DEVICE_NAME.value, Build.MANUFACTURER + " " + Build.MODEL);
            content.put(Tag.PAYLOAD_BROADCAST_TYPE.value, Tag.PAYLOAD_REGULAR_BROADCAST.value);
            Message.Builder builder = new Message.Builder();
            builder.setContent(content);

            Bridgefy.sendBroadcastMessage(builder.build(),
                    BFEngineProfile.BFConfigProfileLongReach);
            addMessage(message);
        }
    }

    /**
     * Method that add a message to the message list in the adapter
     */
    public void addMessage(MessageBadhoc message) {
        messagesBadhocAdapter.addMessage(message);
        broadcastRecyclerView.scrollToPosition(messagesBadhocAdapter.getItemCount() - 1);
    }
}
