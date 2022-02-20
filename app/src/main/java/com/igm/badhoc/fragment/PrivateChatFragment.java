package com.igm.badhoc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Message;
import com.igm.badhoc.R;
import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.adapter.MessagesBadhocAdapter;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment that represents the Private Chat tab of the application
 */
public class PrivateChatFragment extends Fragment {

    /**
     * Debug Tag used in logging
     */
    private final String TAG = "PrivateChatFragment";
    /**
     * RecyclerView that represents the messages in the list
     */
    private RecyclerView privateChatRecyclerView;
    /**
     * Adapter object that represents the messages list
     */
    private MessagesBadhocAdapter messagesBadhocAdapter;
    /**
     * The map object of messages sent and received associated to its conversation id
     */
    private Map<String, List<MessageBadhoc>> conversationsMap;

    /**
     * The id of the current conversation
     */
    private String currentConversationId;
    /**
     * The text zone corresponding to where the message is edited
     */
    private EditText txtMessage;
    /**
     * The image on the send button of the fragment
     */
    private ImageView btnSend;

    /**
     * Method that initializes the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_chat, container, false);
        txtMessage = view.findViewById(R.id.txtMessage);
        btnSend = view.findViewById(R.id.btnSend);
        privateChatRecyclerView = view.findViewById(R.id.message_list);

        messagesBadhocAdapter = new MessagesBadhocAdapter(currentConversationId);
        conversationsMap = new HashMap<>();

        btnSend.setOnClickListener(this::onMessageSend);
        privateChatRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        privateChatRecyclerView.setAdapter(messagesBadhocAdapter);

        return view;
    }

    /**
     * Method to send a message to other devices using Bridgefy
     */
    public void onMessageSend(View v) {
        txtMessage.onEditorAction(EditorInfo.IME_ACTION_DONE);
        MainActivity mainActivity = (MainActivity) requireActivity();
        String messageString = txtMessage.getText().toString().trim();
        if (messageString.length() > 0) {
            txtMessage.setText("");
            MessageBadhoc message = new MessageBadhoc(messageString);
            message.setDirection(MessageBadhoc.OUTGOING_MESSAGE);

            HashMap<String, Object> content = new HashMap<>();
            content.put(Tag.PAYLOAD_TEXT.value, messageString);
            Message.Builder builder = new Message.Builder();
            builder.setContent(content).setReceiverId(currentConversationId);

            Bridgefy.sendMessage(builder.build(),
                    BFEngineProfile.BFConfigProfileLongReach);
            addMessage(message, currentConversationId);
        }
    }

    /**
     * Method to add a message and its conversation id to the map and update the adapter
     *
     * @param message  message to add to the list
     * @param senderId id associated to the message
     */
    public void addMessage(MessageBadhoc message, String senderId) {
        this.conversationsMap.get(senderId).add(message);
        if (senderId.equals(currentConversationId)) {
            messagesBadhocAdapter.notifyItemInserted(this.conversationsMap.get(senderId).size());
        }
    }

    /**
     * Setter method to set the conversation id of the message to display the correct fragment
     */
    public void setConversationId(String conversationId) {
        this.currentConversationId = conversationId;
        messagesBadhocAdapter.setConversationId(conversationId);
    }

    /**
     * Method that adds the list of message to the adapter
     */
    public void setBadhocMessages(String conversationId) {
        messagesBadhocAdapter.setBadhocMessages(conversationsMap.get(conversationId));
        messagesBadhocAdapter.notifyDataSetChanged();
    }

    /**
     * Method that adds an entry to the conversations map if the sender is a new neighbor
     *
     * @param senderId id of the sender
     */
    public void addNeighborToConversationsIfUnknown(String senderId) {
        if (!this.conversationsMap.containsKey(senderId)) {
            this.conversationsMap.put(senderId, new ArrayList<>());
        }
    }
}
