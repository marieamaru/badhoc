package com.igm.badhoc.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Message;
import com.igm.badhoc.R;
import com.igm.badhoc.adapter.MessagesBadhocAdapter;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.Tag;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment that represents the Private Chat tab of the application
 */
public class PrivateChatFragment extends Fragment {

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
     * Progress bar displayed when an image is sent
     */
    private ProgressBar progressBar;

    /**
     * Method that initializes the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_chat, container, false);
        txtMessage = view.findViewById(R.id.txtMessage);
        ImageView btnSend = view.findViewById(R.id.btnSend);
        ImageView btnImage = view.findViewById(R.id.btnImage);
        privateChatRecyclerView = view.findViewById(R.id.message_list);
        progressBar = view.findViewById(R.id.progressBar);
        messagesBadhocAdapter = new MessagesBadhocAdapter(currentConversationId);
        conversationsMap = new HashMap<>();

        btnSend.setOnClickListener(this::onMessageSend);
        btnImage.setOnClickListener(this::onImageSend);
        privateChatRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        privateChatRecyclerView.setAdapter(messagesBadhocAdapter);
        LocalBroadcastManager.getInstance(requireActivity().getBaseContext()).registerReceiver(progressReceiver, new IntentFilter(Tag.INTENT_MAIN_ACTIVITY.value));
        return view;
    }

    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(Tag.INTENT_MSG_PROGRESS.value, 0);
            if (progressBar.getVisibility() == View.GONE) {
                progressBar.setVisibility(View.VISIBLE);
            }
            progressBar.setProgress(progress);
            if (progress == 100 || progress == 0) {
                progressBar.setVisibility(View.GONE);
            }
        }
    };

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
            content.put(Tag.PAYLOAD_PRIVATE_TYPE.value, Tag.PAYLOAD_TEXT.value);
            Message.Builder builder = new Message.Builder();
            builder.setContent(content).setReceiverId(currentConversationId);

            Bridgefy.sendMessage(builder.build(),
                    BFEngineProfile.BFConfigProfileLongReach);
            addMessage(message, currentConversationId);
        }
    }

    public void onImageSend(View v) {
        getImageAndSendMessage.launch("*/*");
    }

    ActivityResultLauncher<String> getImageAndSendMessage = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    try {
                        InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
                        String filePath = uri.getPath();
                        byte[] byteArray = resizeImage(inputStream);
                        HashMap<String, Object> content = new HashMap<>();
                        content.put(Tag.PAYLOAD_TEXT.value, filePath);
                        content.put(Tag.PAYLOAD_PRIVATE_TYPE.value, Tag.PAYLOAD_IMAGE.value);
                        Message.Builder builder = new Message.Builder();
                        Message message = builder.setReceiverId(currentConversationId).setContent(content).setData(byteArray).build();
                        message.setUuid(Bridgefy.sendMessage(message));
                        MessageBadhoc messageImage = new MessageBadhoc(filePath);
                        messageImage.setDirection(MessageBadhoc.OUTGOING_IMAGE);
                        messageImage.setData(byteArray);
                        addMessage(messageImage, currentConversationId);
                        progressBar.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    public byte[] resizeImage(InputStream inputStream) {
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 5, stream);
        bitmap.recycle();
        return stream.toByteArray();
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
            privateChatRecyclerView.scrollToPosition(this.conversationsMap.get(senderId).size() - 1);
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
    @SuppressLint("NotifyDataSetChanged")
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

    /**
     * Method called when the fragment is destroyed.
     * Unregisters the BroadcastReceiver that updates the progress bar.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireActivity().getBaseContext()).unregisterReceiver(progressReceiver);
    }
}
