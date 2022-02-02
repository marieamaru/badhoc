package com.igm.badhoc.fragment;

import android.os.Build;
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
import com.igm.badhoc.adapter.MessagesBadhocAdapter;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BroadcastFragment extends Fragment {

    private RecyclerView broadcastRecyclerView;
    private MessagesBadhocAdapter messagesBadhocAdapter;
    private List<MessageBadhoc> messageBadhocs;
    private String conversationId;

    EditText txtMessage;
    ImageView btnSend;

    public static BroadcastFragment newInstance(Bundle bundle) {
        BroadcastFragment fragment = new BroadcastFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_chat, container, false);
        txtMessage = view.findViewById(R.id.txtMessage);
        btnSend = view.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(this::onMessageSend);

        conversationId = Tag.BROADCAST_CHAT.value;
        messageBadhocs = new ArrayList<>();
        broadcastRecyclerView = view.findViewById(R.id.message_list);
        broadcastRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        messagesBadhocAdapter = new MessagesBadhocAdapter(messageBadhocs, conversationId);
        broadcastRecyclerView.setAdapter(messagesBadhocAdapter);

        return view;
    }

    public void onMessageSend(View v) {
        // close keyboard after send
        txtMessage.onEditorAction(EditorInfo.IME_ACTION_DONE);
        // get the message and push it to the views
        String messageString = txtMessage.getText().toString().trim();
        if (messageString.length() > 0) {
            // update the views
            txtMessage.setText("");
            MessageBadhoc message = new MessageBadhoc(messageString);
            message.setDirection(MessageBadhoc.OUTGOING_MESSAGE);
            // create a HashMap object to send
            HashMap<String, Object> content = new HashMap<>();
            content.put(Tag.PAYLOAD_TEXT.value, messageString);
            // Obligatoirement dans BROADCAST CHAT
            content.put(Tag.PAYLOAD_DEVICE_NAME.value, Build.MANUFACTURER + " " + Build.MODEL);
            //content.put(IntentTag.PAYLOAD_DEVICE_TYPE.value, Neighbor.DeviceType.ANDROID.ordinal());

            Message.Builder builder = new Message.Builder();
            builder.setContent(content);

            Bridgefy.sendBroadcastMessage(builder.build(),
                    BFEngineProfile.BFConfigProfileLongReach);
            addMessage(message);
        }
    }

    public void addMessage(MessageBadhoc message) {
        messageBadhocs.add(message);
        messagesBadhocAdapter.notifyItemInserted(messageBadhocs.size() - 1);
    }

    public void setConversationId(String conversationId) {
        messagesBadhocAdapter.setConversationId(conversationId);
    }
}