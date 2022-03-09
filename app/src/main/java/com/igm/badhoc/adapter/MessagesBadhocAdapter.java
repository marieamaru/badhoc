package com.igm.badhoc.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igm.badhoc.R;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessagesBadhocAdapter extends RecyclerView.Adapter<MessagesBadhocAdapter.MessageViewHolder> implements Serializable {

    private List<MessageBadhoc> badhocMessages;
    private String conversationId;

    public MessagesBadhocAdapter(String conversationId) {
        this.badhocMessages = new ArrayList<>();
        this.conversationId = conversationId;
    }

    public void addMessage(MessageBadhoc message) {
        this.badhocMessages.add(message);
        notifyItemInserted(badhocMessages.size() - 1);
    }

    @Override
    public int getItemCount() {
        return badhocMessages.size();
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public int getItemViewType(int position) {
        return badhocMessages.get(position).getDirection();
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
            case MessageBadhoc.INCOMING_IMAGE:
                messageView = LayoutInflater.from(viewGroup.getContext()).
                        inflate((R.layout.message_row_image_incoming), viewGroup, false);
                break;
            case MessageBadhoc.OUTGOING_IMAGE:
                messageView = LayoutInflater.from(viewGroup.getContext()).
                        inflate((R.layout.message_row_image_outgoing), viewGroup, false);
        }

        return new MessageViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder messageHolder, int position) {
        messageHolder.setMessage(badhocMessages.get(position));
    }

    public void setBadhocMessages(List<MessageBadhoc> badhocMessages) {
        this.badhocMessages = badhocMessages;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        final View txtMessage;
        final TextView deviceMessageName;
        MessageBadhoc message;

        MessageViewHolder(View view) {
            super(view);
            txtMessage = view.findViewById(R.id.txtMessage);
            deviceMessageName = view.findViewById(R.id.deviceMessageName);
        }

        void setMessage(MessageBadhoc message) {
            this.message = message;
            int direction = message.getDirection();
            if (direction == MessageBadhoc.INCOMING_IMAGE || direction == MessageBadhoc.OUTGOING_IMAGE) {
                ImageView imageMessage = (ImageView) txtMessage;
                Bitmap bm = BitmapFactory.decodeByteArray(message.getData(), 0, message.getData().length);
                imageMessage.setImageBitmap(bm);
            } else {
                if (direction == MessageBadhoc.INCOMING_MESSAGE &&
                        conversationId.equals(Tag.BROADCAST_CHAT.value)) {
                    this.deviceMessageName.setText(message.getDeviceName());
                    this.deviceMessageName.setVisibility(View.VISIBLE);
                }
                TextView texteMessage = (TextView) txtMessage;
                texteMessage.setText(message.getText());
            }

        }
    }
}
