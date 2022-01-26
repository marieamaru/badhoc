package com.igm.badhoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igm.badhoc.R;
import com.igm.badhoc.model.Tag;
import com.igm.badhoc.model.MessageBadhoc;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.List;

public class MessagesBadhocAdapter extends RecyclerView.Adapter<MessagesBadhocAdapter.MessageViewHolder> implements Serializable {

    private List<MessageBadhoc> messages;
    private String conversationId;

    public MessagesBadhocAdapter(String conversationId) {
        this.conversationId = conversationId;
    }

    public MessagesBadhocAdapter(List<MessageBadhoc> messages, String conversationId) {
        this.messages = messages;
        this.conversationId = conversationId;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getDirection();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View messageView = null;

        switch (viewType) {
            case MessageBadhoc.INCOMING_MESSAGE:
                messageView = LayoutInflater.from(viewGroup.getContext()).
                        inflate((R.layout.message_row_incoming), viewGroup, false);
                break;
            case MessageBadhoc.OUTGOING_MESSAGE:
                messageView = LayoutInflater.from(viewGroup.getContext()).
                        inflate((R.layout.message_row_outgoing), viewGroup, false);
                break;
        }

        return new MessageViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder messageHolder, int position) {
        messageHolder.setMessage(messages.get(position));
    }

    public void setMessages(List<MessageBadhoc> messages) {
        this.messages = messages;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView txtMessage;
        final TextView deviceMessageName;
        MessageBadhoc message;

        MessageViewHolder(View view) {
            super(view);
            txtMessage = view.findViewById(R.id.txtMessage);
            deviceMessageName = view.findViewById(R.id.deviceMessageName);
        }

        void setMessage(MessageBadhoc message) {
            this.message = message;
            if (message.getDirection() == MessageBadhoc.INCOMING_MESSAGE &&
                    conversationId.equals(Tag.BROADCAST_CHAT.value)) {
                this.deviceMessageName.setText(message.getDeviceName());
                this.txtMessage.setText(message.getText());
            } else {
                this.txtMessage.setText(message.getText());
            }
        }
    }
}
