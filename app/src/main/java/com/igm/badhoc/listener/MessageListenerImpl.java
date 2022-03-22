package com.igm.badhoc.listener;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.model.MessageBadhoc;
import com.igm.badhoc.model.Neighbor;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.NotificationDisplay;
import com.igm.badhoc.model.Status;
import com.igm.badhoc.model.Tag;
import com.igm.badhoc.util.ParserUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.UUID;

/**
 * Implementation of the Bridgefy  message listener
 */
public class MessageListenerImpl extends MessageListener {
    /**
     * Debug Tag used in logging
     */
    private final String TAG = "MessageListener";
    /**
     * Main activity object
     */
    private final MainActivity mainActivity;

    /**
     * Constructor for the listener
     *
     * @param mainActivity main activity object
     */
    public MessageListenerImpl(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    /**
     * Method that handles the private message received : determines if it is a handshake message or a regular private message
     *
     * @param message message received
     */
    @Override
    public void onMessageReceived(Message message) {
        String senderId = message.getSenderId();
        HashMap<String, String> messageContent = message.getContent();
        // direct messages carrying a Device name represent device handshakes
        if (messageContent.get(Tag.PAYLOAD_DEVICE_NAME.value) != null) {
            Node newNode = registerNeighborFromHandshake(senderId, messageContent);
            Neighbor newNeighbor = new Neighbor(senderId, newNode.getMacAddress(), newNode.getRssi());
            mainActivity.getAroundMeFragment().addNeighborToConversations(newNode);
            mainActivity.getPrivateChatFragment().addNeighborToConversationsIfUnknown(newNode.getId());
            mainActivity.getNode().addToNeighborhood(newNeighbor);
            int neighborStatus = Integer.parseInt(getFromMessage(messageContent, Tag.PAYLOAD_IS_DOMINANT.value));
            handleDominatingStatus(senderId, newNeighbor, neighborStatus);
            handleServer(mainActivity.getNode().isDominant());
            mainActivity.broadcastIntentAction(Tag.ACTION_UPDATE_NODE_INFO.value, ParserUtil.parseNodeKeepAliveMessage(mainActivity.getNode()));
            Log.i(TAG, "Peer introduced itself: " + newNode.getMacAddress());
            // any other direct message should be treated as such
        } else {
            String messageType = messageContent.get(Tag.PAYLOAD_PRIVATE_TYPE.value);
            String incomingMessage = messageContent.get(Tag.PAYLOAD_TEXT.value);
            if (Tag.PAYLOAD_TEXT.value.equals(messageType)) {
                MessageBadhoc messageBadhoc = new MessageBadhoc(incomingMessage);
                messageBadhoc.setDirection(MessageBadhoc.INCOMING_MESSAGE);
                mainActivity.getPrivateChatFragment().addMessage(messageBadhoc, senderId);
                mainActivity.displayNotificationBadge(Tag.PRIVATE_CHAT.value);
            } else if (Tag.PAYLOAD_MESSAGE_TO_SERVER.value.equals(messageType)) {
                mainActivity.broadcastIntentAction(Tag.ACTION_SEND_MESSAGE_TO_SERVER.value, incomingMessage);
            } else {
                byte[] fileBytes = message.getData();
                MessageBadhoc messageBadhoc = new MessageBadhoc(incomingMessage);
                messageBadhoc.setData(fileBytes);
                messageBadhoc.setDirection(MessageBadhoc.INCOMING_IMAGE);
                mainActivity.getPrivateChatFragment().addMessage(messageBadhoc, senderId);
                mainActivity.displayNotificationBadge(Tag.PRIVATE_CHAT.value);
            }
            Log.d(TAG, "Incoming private message: " + incomingMessage);
        }

    }

    /**
     * Method that handles broadcast messages received : determines if it is a regular broadcast message,
     * a notification message from a dominant node,
     * an update on the dominant node status,
     * or an update on the neighbors node status
     *
     * @param message message received
     */
    @Override
    public void onBroadcastMessageReceived(Message message) {
        // we should not expect to have connected previously to the device that originated
        // the incoming broadcast message, so device information is included in this packet=
        String incomingMsg = (String) message.getContent().get(Tag.PAYLOAD_TEXT.value);
        String deviceName = (String) message.getContent().get(Tag.PAYLOAD_DEVICE_NAME.value);
        String broadcastType = (String) message.getContent().get(Tag.PAYLOAD_BROADCAST_TYPE.value);
        if (Tag.PAYLOAD_REGULAR_BROADCAST.value.equals(broadcastType)) {
            MessageBadhoc messageBadhoc = new MessageBadhoc(incomingMsg);
            messageBadhoc.setDirection(MessageBadhoc.INCOMING_MESSAGE);
            messageBadhoc.setDeviceName(deviceName);
            mainActivity.getBroadcastFragment().addMessage(messageBadhoc);
            mainActivity.displayNotificationBadge(Tag.BROADCAST_CHAT.value);
        }
        if (Tag.PAYLOAD_FROM_SERVER.value.equals(broadcastType)) {
            NotificationDisplay notificationDisplay = new NotificationDisplay(incomingMsg);
            notificationDisplay.setDirection(NotificationDisplay.INCOMING_MESSAGE);
            mainActivity.getNotificationFragment().addNotification(notificationDisplay);
        }
        if (Tag.PAYLOAD_NO_LONGER_DOMINANT.value.equals(broadcastType)) {
            Log.i(TAG, "received that my dominant is no longer dominant");
            mainActivity.getNode().setDominant(null);
        }
        if (Tag.PAYLOAD_POTENTIAL_DOMINANT.value.equals(broadcastType)) {
            for (Neighbor neighbor : mainActivity.getNode().getNeighbours()) {
                if (neighbor.getId().equals(message.getSenderId())) {
                    if (mainActivity.getNode().isDominant() == Status.DOMINATED.value) {
                        mainActivity.getNode().setDominant(neighbor);
                    } else {
                        handleDominatingStatus(message.getSenderId(), neighbor, Status.DOMINATING.value);
                    }
                    break;
                }
            }
        }
        Log.i(TAG, "Incoming broadcast message: " + incomingMsg);
    }

    /**
     * Method that determines the progress of the sending of a data type message.
     * Allows to update the progress bar in the private chat fragment.
     *
     * @param message  message sent
     * @param progress progress of the sending
     * @param fullSize total size of the message sent
     */
    @Override
    public void onMessageDataProgress(UUID message, long progress, long fullSize) {
        int currentProgress = (int) ((progress * 100) / fullSize);
        LocalBroadcastManager.getInstance(mainActivity.getBaseContext()).sendBroadcast(
                new Intent(Tag.INTENT_MAIN_ACTIVITY.value)
                        .putExtra(Tag.INTENT_MSG_PROGRESS.value, currentProgress)
        );

    }

    /**
     * Method that registers the incoming handshake as a new neighbor
     *
     * @param senderId       id of the sender
     * @param messageContent content of the message received
     * @return the new Node object created
     */
    private Node registerNeighborFromHandshake(String senderId, HashMap<String, String> messageContent) {
        Node node = Node.builder(senderId, messageContent.get(Tag.PAYLOAD_DEVICE_NAME.value))
                .build();
        node.setNearby(true);
        node.setMacAddress(messageContent.get(Tag.PAYLOAD_MAC_ADDRESS.value));
        node.setIsDominant(Integer.parseInt(getFromMessage(messageContent, Tag.PAYLOAD_IS_DOMINANT.value)));
        node.setRssi(Float.parseFloat(getFromMessage(messageContent, Tag.PAYLOAD_RSSI.value)));
        return node;
    }

    /**
     * Method that determines the status of the node by comparing it to the new neighbor's status
     *
     * @param senderId       id of the sender
     * @param neighbor       neighbor sending the message
     * @param neighborStatus status of the neighbor
     */
    private void handleDominatingStatus(String senderId, Neighbor neighbor, int neighborStatus) {
        Node node = mainActivity.getNode();
        switch (node.isDominant()) {
            case 0:
                if ((neighborStatus) == Status.DOMINATING.value) {
                    node.setDominant(neighbor);
                    Log.i(TAG, "I am dominated");
                    return;
                }
                Log.i(TAG, "Nobody is dominating");
                return;
            case 1:
                if (neighborStatus == Status.DOMINATED.value) {
                    node.addToDominating(senderId, neighbor.getMacAddress());
                    Log.i(TAG, "I am dominating someone new");
                } else {
                    if (node.getRssi() < neighbor.getRSSI()) {
                        node.setIsDominant(Status.DOMINATED.value);
                        node.setDominant(neighbor);
                        node.clearDominatingList();
                        Log.i(TAG, "I am no longer dominant because my RSSI is smaller : " + node.getRssi() + ", neighbor's : " + neighbor.getRSSI());
                    } else {
                        node.addToDominating(senderId, neighbor.getMacAddress());
                        Log.i(TAG, "I am dominating because my RSSI is better : " + node.getRssi() + ", neighbor's : " + neighbor.getRSSI());
                    }
                }
        }
    }

    /**
     * Methods that starts or stops the server service according to the node status
     *
     * @param isDominant status of the device
     */
    private void handleServer(int isDominant) {
        String tryServerConnect;
        if (isDominant == 1) {
            tryServerConnect = "connect";
        } else {
            tryServerConnect = "disconnect";
        }
        mainActivity.broadcastIntentAction(Tag.ACTION_CONNECT.value, tryServerConnect);
    }

    /**
     * Method that returns the specific payload of a message received
     *
     * @param messageContent map representing the message received
     * @param value          payload to retrieve
     * @return the payload from the message
     */
    private String getFromMessage(HashMap<String, String> messageContent, String value) {
        String content = messageContent.get(value);
        if (content != null) {
            return content;
        }
        return StringUtils.EMPTY;
    }

}
