package za.ac.iie.prog5121.part3;

import java.util.Random;

/**
 * A single QuickChat message (Parts 2 and 3).
 */
public final class Message {

    public static final String SENT = "Sent";
    public static final String DISREGARDED = "Disregarded";
    public static final String STORED = "Stored";

    private final String messageId;
    private final int messageNumber;
    private final String recipientNumber;
    private final String messageContent;
    private String messageHash;
    private String deliveryStatus;

    // Total messages sent across the application
    private static int totalMessagesSent = 0;

    /** Part 2 constructor: generates a random 10-digit ID. */
    public Message(int messageNumber, String recipientNumber, String messageContent) {
        this.messageNumber = messageNumber;
        this.recipientNumber = recipientNumber;
        this.messageContent = messageContent;
        this.messageId = generateRandomID();
        this.messageHash = createMessageHash();
    }

    /**
     * Part 3 constructor: used when loading from JSON and for the unit-test
     * data, where the ID and status are already known.
     */
    public Message(String messageId, int messageNumber, String recipientNumber,
                   String messageContent, String deliveryStatus) {
        this.messageId = messageId;
        this.messageNumber = messageNumber;
        this.recipientNumber = recipientNumber;
        this.messageContent = messageContent;
        this.deliveryStatus = deliveryStatus;
        this.messageHash = createMessageHash();
    }

    private String generateRandomID() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }

    public boolean checkMessageID() {
        return messageId != null && messageId.length() <= 10;
    }

    public String checkRecipientCell() {
        if (recipientNumber.startsWith("+") && recipientNumber.length() >= 10) {
            return "Cell phone number successfully captured.";
        } else if (!recipientNumber.startsWith("+") && recipientNumber.length() == 10) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
    }

    public String checkMessageLength() {
        if (messageContent.length() <= 250) {
            return "Message ready to send.";
        }
        int exceededBy = messageContent.length() - 250;
        return "Message exceeds 250 characters by " + exceededBy + ", please reduce the size.";
    }

    /** Hash format: first two ID digits : message number : FIRSTWORDLASTWORD. */
    public String createMessageHash() {
        if (messageId == null || messageId.length() < 2
                || messageContent == null || messageContent.trim().isEmpty()) {
            return "00:0:UNKNOWN";
        }
        String idPrefix = messageId.substring(0, 2);
        String[] words = messageContent.trim().split("\\s+");
        // Fixed from Part 2: the old character class "[^a-zA-Z0-String]" was a
        // typo that let some punctuation through. Keep letters and digits only.
        String firstWord = words[0].replaceAll("[^a-zA-Z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "");
        this.messageHash = (idPrefix + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
        return this.messageHash;
    }

    public String SentMessage(int choice) {
        switch (choice) {
            case 1 -> {
                totalMessagesSent++;
                this.deliveryStatus = SENT;
                return "Message successfully sent.";
            }
            case 2 -> {
                this.deliveryStatus = DISREGARDED;
                return "Press 0 to delete the message.";
            }
            case 3 -> {
                this.deliveryStatus = STORED;
                return "Message successfully stored.";
            }
            default -> {
                return "Invalid selection.";
            }
        }
    }

    public String printMessages() {
        return "-------------------------------------------\n"
                + "Message ID generated: <" + messageId + ">\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipientNumber + "\n"
                + "Message: " + messageContent + "\n"
                + "Status: " + deliveryStatus + "\n"
                + "-------------------------------------------";
    }

    public static int returnTotalMessagess() {
        return totalMessagesSent;
    }

    public String getMessageId() { return messageId; }
    public int getMessageNumber() { return messageNumber; }
    public String getMessageHash() { return messageHash; }
    public String getRecipientNumber() { return recipientNumber; }
    public String getMessageContent() { return messageContent; }
    public String getDeliveryStatus() { return deliveryStatus; }
}
