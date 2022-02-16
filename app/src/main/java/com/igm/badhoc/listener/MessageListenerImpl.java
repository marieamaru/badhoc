package com.igm.badhoc.listener;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.Neighbor;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.Notification;
import com.igm.badhoc.model.Status;
import com.igm.badhoc.model.Tag;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Optional;

public class MessageListenerImpl extends MessageListener {

    private final MainActivity mainActivity;

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
            Neighbor newNeighbor = new Neighbor(senderId, newNode.getMacAddress(), newNode.getRssi());
            mainActivity.getNeighborsFragment().addNeighborToConversations(newNode);
            mainActivity.getPrivateChatFragment().addNeighborToConversationsIfUnknown(newNode.getId());
            mainActivity.getNode().addToNeighborhood(newNeighbor);
            int neighborStatus = Integer.parseInt(getFromMessage(messageContent, Tag.PAYLOAD_IS_DOMINANT.value));
            handleDominatingStatus(senderId, newNeighbor, neighborStatus);
            handleServer(mainActivity.getNode().isDominant());
            mainActivity.broadcastIntentAction(Tag.ACTION_UPDATE_NODE_INFO.value, mainActivity.getNode().nodeKeepAliveMessage());
            Log.e(TAG, "Peer introduced itself: " + newNode.getMacAddress() + " " + mainActivity.getNode().nodeKeepAliveMessage());
            // any other direct message should be treated as such
        } else {
            String incomingMessage = (String) message.getContent().get("text");
            MessageBadhoc messageBadhoc = new MessageBadhoc(incomingMessage);
            messageBadhoc.setDirection(MessageBadhoc.INCOMING_MESSAGE);
            mainActivity.getPrivateChatFragment().addMessage(messageBadhoc, senderId);
            Log.d(TAG, "Incoming private message: " + incomingMessage);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBroadcastMessageReceived(Message message) {
        // we should not expect to have connected previously to the device that originated
        // the incoming broadcast message, so device information is included in this packet=
        String incomingMsg = (String) message.getContent().get(Tag.PAYLOAD_TEXT.value);
        String deviceName = (String) message.getContent().get(Tag.PAYLOAD_DEVICE_NAME.value);
        String broadcastType = (String) message.getContent().get(Tag.PAYLOAD_BROADCAST_TYPE.value);

        if (broadcastType.equals(Tag.PAYLOAD_REGULAR_BROADCAST.value)) {
            MessageBadhoc messageBadhoc = new MessageBadhoc(incomingMsg);
            messageBadhoc.setDirection(MessageBadhoc.INCOMING_MESSAGE);
            messageBadhoc.setDeviceName(deviceName);
            mainActivity.getBroadcastFragment().addMessage(messageBadhoc);
        }
        if (broadcastType.equals(Tag.PAYLOAD_FROM_SERVER.value)) {
            Log.e(TAG, "received notif from my dominant");
            mainActivity.getNotificationFragment().addNotification(new Notification(incomingMsg));
        }
        if (broadcastType.equals(Tag.PAYLOAD_NO_LONGER_DOMINANT.value)) {
            Log.e(TAG, "received that my dominant is no longer dominant");
            mainActivity.getNode().setDominant(null);
        }
        if (broadcastType.equals(Tag.PAYLOAD_DOMINANT.value)) {
            Optional<Neighbor> neighbor = mainActivity.getNode().getNeighbours().stream()
                    .filter(neighbor1 -> neighbor1.getId().equals(message.getSenderId())).findFirst();
            if (mainActivity.getNode().isDominant() == Status.DOMINATED.value) {
                neighbor.ifPresent(value -> mainActivity.getNode().setDominant(value));
            } else {
                neighbor.ifPresent(value -> handleDominatingStatus(message.getSenderId(), value, Status.DOMINATING.value));
            }
        }
        Log.d(TAG, "Incoming broadcast message: " + incomingMsg);
    }

    private Node registerNeighborFromHandshake(String senderId, HashMap<String, String> messageContent) {
        Node node = Node.builder(senderId, messageContent.get(Tag.PAYLOAD_DEVICE_NAME.value))
                .build();
        node.setNearby(true);
        node.setMacAddress(messageContent.get(Tag.PAYLOAD_MAC_ADDRESS.value));
        node.setIsDominant(Integer.parseInt(getFromMessage(messageContent, Tag.PAYLOAD_IS_DOMINANT.value)));
        node.setRssi(Float.parseFloat(getFromMessage(messageContent, Tag.PAYLOAD_RSSI.value)));
        Log.e(TAG, "mon handshake est " + messageContent.get(Tag.PAYLOAD_IS_DOMINANT.value) + " son rssi est " + messageContent.get(Tag.PAYLOAD_RSSI.value));
        return node;
    }

    private void handleDominatingStatus(String senderId, Neighbor neighbor, int neighborStatus) {
        Node node = mainActivity.getNode();
        switch (node.isDominant()) {
            case 0:
                if ((neighborStatus) == Status.DOMINATING.value) {
                    node.setDominant(neighbor);
                    Log.e(TAG, "JE SUIS DOMINE");
                    return;
                }
                Log.e(TAG, "PERSONNE NE DOMINE");
                return;
            case 1:
                if (neighborStatus == Status.DOMINATED.value) {
                    node.addToDominating(senderId, neighbor.getMacAddress());
                    Log.e(TAG, "JE DOMINE QUELQU'UN DE NOUVEAU");
                } else {
                    if (node.getRssi() < neighbor.getRSSI()) {
                        node.setIsDominant(Status.DOMINATED.value);
                        node.setDominant(neighbor);
                        node.clearDominatingList();
                        Log.e(TAG, "JE NE SUIS PLUS DOMINANT CAR MON RSSI EST PETIT, MOI : " + node.getRssi() + " mon voisin : " + neighbor.getRSSI());
                    } else {
                        node.addToDominating(senderId, neighbor.getMacAddress());
                        Log.e(TAG, "JE SUIS DOMINANT CAR MON RSSI EST MEILLEUR, MOI : " + node.getRssi() + " mon voisin : " + neighbor.getRSSI());
                    }
                }
        }
    }

    private void handleServer(int isDominant) {
        String tryServerConnect;
        if (isDominant == 1) {
            tryServerConnect = "connect";
        } else {
            tryServerConnect = "disconnect";
        }
        mainActivity.broadcastIntentAction(Tag.ACTION_CONNECT.value, tryServerConnect);
    }

    private String getFromMessage(HashMap<String, String> messageContent, String value) {
        String content = messageContent.get(value);
        if (content != null) {
            return content;
        }
        return StringUtils.EMPTY;
    }

}
