package com.igm.badhoc.listener;

import android.util.Log;

import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.Neighbor;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.Status;
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
            Node newNode = registerNeighborFromHandshake(senderId, messageContent);
            Neighbor newNeighbor = new Neighbor(newNode.getMacAddress(), newNode.getRssi());
            mainActivity.getNeighborsFragment().addNeighborToConversations(newNode);
            mainActivity.getPrivateChatFragment().addNeighborToConversationsIfUnknown(newNode.getId());
            mainActivity.getNode().addToNeighborhood(senderId, newNeighbor);
            handleStatus(senderId, newNeighbor, messageContent);
            Log.d(TAG, "Peer introduced itself: " + newNode.getMacAddress());
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

    private Node registerNeighborFromHandshake(String senderId, HashMap<String, String> messageContent) {
        Node node = Node.builder(senderId, messageContent.get(Tag.PAYLOAD_DEVICE_NAME.value))
                .build();
        node.setNearby(true);
        node.setMacAddress(messageContent.get(Tag.PAYLOAD_MAC_ADDRESS.value));
        node.setIsDominant(Integer.parseInt(messageContent.get(Tag.PAYLOAD_IS_DOMINANT.value)));
        node.setRssi(Float.parseFloat(messageContent.get(Tag.PAYLOAD_RSSI.value)));
        Log.e(TAG, "mon handshake est " + messageContent.get(Tag.PAYLOAD_IS_DOMINANT.value) + " son rssi est " + messageContent.get(Tag.PAYLOAD_RSSI.value));
        return node;
    }

    private void handleStatus(String senderId, Neighbor neighbor, HashMap<String, String> messageContent) {
        Node node = mainActivity.getNode();
        int neighborStatus = Integer.parseInt(messageContent.get(Tag.PAYLOAD_IS_DOMINANT.value));
        switch (node.isDominant()) {
            case 0:
                if ((neighborStatus) == Status.DOMINATING.value) {
                    node.setDominant(neighbor);
                    Log.e(TAG, "JE SUIS DOMINE");
                } else {
                    Log.e(TAG, "PERSONNE NE DOMINE");
                }
                return;
            case 1:
                if ((neighborStatus) == Status.DOMINATED.value) {
                    node.addToDominating(senderId, neighbor.getMacAddress());
                    Log.e(TAG, "JE DOMINE QUELQUUN DE NOUVEAU");
                } else {
                    if (node.getRssi() < neighbor.getRssi()) {
                        node.setIsDominant(Status.DOMINATED.value);
                        node.setDominant(neighbor);
                        node.clearDominatingList();
                        Log.e(TAG, "JE NE SUIS PLUS DOMINANT CAR MON RSSI EST PETIT, MOI : " + node.getRssi() + " mon voisin : " + neighbor.getRssi());
                        //TODO
                        // envoyer sa liste de dominés??
                    } else {
                        node.setDominant(null);
                        node.addToDominating(senderId, neighbor.getMacAddress());
                        Log.e(TAG, "JE SUIS DOMINANT CAR MON RSSI EST MEILLEUR, MOI : " + node.getRssi() + " mon voisin : " + neighbor.getRssi());
                    }
                }
        }
    }
}
