package com.igm.badhoc.listener;

import android.util.Log;

import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.Neighbor;
import com.igm.badhoc.model.Tag;

import java.util.HashMap;

public class MessageListenerImpl extends MessageListener {

    private MainActivity mainActivity;

    private final String TAG = "MessageListener";

    public MessageListenerImpl(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onMessageReceived(Message message) {
        String senderId = message.getSenderId();
        HashMap<String, String> messageContent = message.getContent();
        // direct messages carrying a Device name represent device handshakes
        if (messageContent.get(Tag.PAYLOAD_DEVICE_NAME.value) != null) {
            Neighbor newNeighbor = registerNeighborFromHandshake(senderId, messageContent);
            mainActivity.getNeighborsFragment().addNeighbor(newNeighbor);
            mainActivity.getPrivateChatFragment().addNeighborIfUnknown(newNeighbor.getId());
            Log.d(TAG, "Peer introduced itself: " + newNeighbor.getDeviceName());
            // any other direct message should be treated as such
        } else {
            String incomingMessage = (String) message.getContent().get("text");
            MessageBadhoc messageBadhoc = new MessageBadhoc(incomingMessage);
            messageBadhoc.setDirection(MessageBadhoc.INCOMING_MESSAGE);
            mainActivity.getPrivateChatFragment().addMessage(messageBadhoc, senderId);
            Log.d(TAG, "Incoming private message: " + incomingMessage);
        }
    }

    @Override
    public void onBroadcastMessageReceived(Message message) {
        // we should not expect to have connected previously to the device that originated
        // the incoming broadcast message, so device information is included in this packet=
        String incomingMsg = (String) message.getContent().get(Tag.PAYLOAD_TEXT.value);
        String deviceName = (String) message.getContent().get(Tag.PAYLOAD_DEVICE_NAME.value);

        MessageBadhoc messageBadhoc = new MessageBadhoc((String) message.getContent().get("text"));
        messageBadhoc.setDirection(MessageBadhoc.INCOMING_MESSAGE);
        messageBadhoc.setDeviceName(deviceName);
        mainActivity.getBroadcastFragment().addMessage(messageBadhoc);
        Log.d(TAG, "Incoming broadcast message: " + incomingMsg);
    }

    private Neighbor registerNeighborFromHandshake(String senderId, HashMap<String, String> messageContent) {
        Neighbor neighbor = Neighbor.builder(senderId, messageContent.get(Tag.PAYLOAD_DEVICE_NAME.value))
                .build();
        neighbor.setNearby(true);
        neighbor.setMacAddress(messageContent.get(Tag.PAYLOAD_MAC_ADDRESS.value));
        return neighbor;
    }
}
