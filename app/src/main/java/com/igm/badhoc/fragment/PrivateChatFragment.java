package com.igm.badhoc.fragment;

import android.os.Bundle;
import android.util.Log;
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

public class PrivateChatFragment extends Fragment {

    private final String TAG = "PrivateChatFragment";
    private RecyclerView privateChatRecyclerView;
    private MessagesBadhocAdapter messagesBadhocAdapter;
    Map<String, List<MessageBadhoc>> conversationsMap;

    private String currentConversationId;

    EditText txtMessage;
    ImageView btnSend;

    public static PrivateChatFragment newInstance(Bundle bundle) {
        PrivateChatFragment fragment = new PrivateChatFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

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

    public void onMessageSend(View v) {
        // close keyboard after send
        txtMessage.onEditorAction(EditorInfo.IME_ACTION_DONE);
        // get the message and push it to the views
        MainActivity mainActivity = (MainActivity) getActivity();
        String messageString = txtMessage.getText().toString().trim();
        Log.e(TAG, mainActivity.getNode().nodeKeepAliveMessage());
        if (messageString.length() > 0) {
            // update the views
            txtMessage.setText("");
            MessageBadhoc message = new MessageBadhoc(messageString);
            message.setDirection(MessageBadhoc.OUTGOING_MESSAGE);
            // create a HashMap object to send
            HashMap<String, Object> content = new HashMap<>();
            content.put(Tag.PAYLOAD_TEXT.value, messageString);
            Message.Builder builder = new Message.Builder();
            builder.setContent(content).setReceiverId(currentConversationId);
            Bridgefy.sendMessage(builder.build(),
                    BFEngineProfile.BFConfigProfileLongReach);
            addMessage(message, currentConversationId);
        }
    }

    public void addMessage(MessageBadhoc message, String senderId) {
        this.conversationsMap.get(senderId).add(message);
        if (senderId.equals(currentConversationId)) {
            messagesBadhocAdapter.notifyItemInserted(this.conversationsMap.get(senderId).size());
        }
    }

    public void setConversationId(String conversationId) {
        this.currentConversationId = conversationId;
        messagesBadhocAdapter.setConversationId(conversationId);
    }

    public void setMessageBadhocs(String convId) {
        messagesBadhocAdapter.setMessageBadhocs(conversationsMap.get(convId));
        messagesBadhocAdapter.notifyDataSetChanged();
    }

    public void addNeighborToConversationsIfUnknown(String senderId) {
        if (!this.conversationsMap.containsKey(senderId)) {
            this.conversationsMap.put(senderId, new ArrayList<>());
        }
    }
}
