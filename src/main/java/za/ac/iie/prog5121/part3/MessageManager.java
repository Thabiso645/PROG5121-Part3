package za.ac.iie.prog5121.part3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 3: holds the message arrays and implements the "Stored Messages"
 * features. Stored messages are persisted to a JSON file using Gson.
 */
public class MessageManager {

    // The five arrays required by the brief. All are populated at runtime.
    private final ArrayList<String> sentMessages = new ArrayList<>();
    private final ArrayList<String> disregardedMessages = new ArrayList<>();
    private final ArrayList<String> storedMessages = new ArrayList<>();
    private final ArrayList<String> messageHashes = new ArrayList<>();
    private final ArrayList<String> messageIds = new ArrayList<>();

    // Full Message objects, kept so recipient/hash/ID lookups are possible.
    private final ArrayList<Message> allMessages = new ArrayList<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Flat record written to / read from the JSON file. */
    private static class StoredRecord {
        String messageId;
        int messageNumber;
        String recipient;
        String message;
        String messageHash;
    }

    // ------------------------------------------------------------------
    // Populating the arrays
    // ------------------------------------------------------------------

    /** Adds a message and files it into the correct arrays by its status. */
    public void addMessage(Message msg) {
        String status = msg.getDeliveryStatus();
        if (status == null) {
            return; // invalid selection, nothing to record
        }
        allMessages.add(msg);
        messageHashes.add(msg.getMessageHash());
        messageIds.add(msg.getMessageId());

        switch (status) {
            case Message.SENT -> sentMessages.add(msg.getMessageContent());
            case Message.DISREGARDED -> disregardedMessages.add(msg.getMessageContent());
            case Message.STORED -> storedMessages.add(msg.getMessageContent());
            default -> { }
        }
    }

    // ------------------------------------------------------------------
    // JSON storage
    // ------------------------------------------------------------------

    /** Writes every stored message to the JSON file (overwrites the file). */
    public void saveStoredToJson(String filePath) throws IOException {
        List<StoredRecord> records = new ArrayList<>();
        for (Message m : allMessages) {
            if (Message.STORED.equals(m.getDeliveryStatus())) {
                StoredRecord r = new StoredRecord();
                r.messageId = m.getMessageId();
                r.messageNumber = m.getMessageNumber();
                r.recipient = m.getRecipientNumber();
                r.message = m.getMessageContent();
                r.messageHash = m.getMessageHash();
                records.add(r);
            }
        }
        Files.write(Paths.get(filePath),
                GSON.toJson(records).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reads the JSON file into the stored-messages array (and the hash / ID
     * arrays). Messages already loaded (same ID) are skipped. A missing file
     * is not an error: it just means nothing has been stored yet.
     *
     * @return number of messages loaded
     */
    public int loadStoredFromJson(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return 0;
        }
        String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        Type listType = new TypeToken<List<StoredRecord>>() { }.getType();
        List<StoredRecord> records = GSON.fromJson(json, listType);
        if (records == null) {
            return 0;
        }
        int loaded = 0;
        for (StoredRecord r : records) {
            if (messageIds.contains(r.messageId)) {
                continue;
            }
            addMessage(new Message(r.messageId, r.messageNumber, r.recipient,
                    r.message, Message.STORED));
            loaded++;
        }
        return loaded;
    }

    // ------------------------------------------------------------------
    // Stored Messages menu features
    // ------------------------------------------------------------------

    /** 2a. Sender and recipient of every stored message. */
    public String displaySenderAndRecipient(String sender) {
        StringBuilder sb = new StringBuilder();
        for (Message m : allMessages) {
            if (Message.STORED.equals(m.getDeliveryStatus())) {
                sb.append("Sender: ").append(sender)
                  .append(" | Recipient: ").append(m.getRecipientNumber()).append("\n");
            }
        }
        return sb.length() == 0 ? "No stored messages." : sb.toString().trim();
    }

    /** Longest message across all arrays (used by the unit test). */
    public String longestMessage() {
        return longestOf(allMessages, null);
    }

    /** 2b. Longest stored message. */
    public String longestStoredMessage() {
        return longestOf(allMessages, Message.STORED);
    }

    private String longestOf(List<Message> list, String status) {
        String longest = "";
        for (Message m : list) {
            if (status != null && !status.equals(m.getDeliveryStatus())) {
                continue;
            }
            if (m.getMessageContent().length() > longest.length()) {
                longest = m.getMessageContent();
            }
        }
        return longest;
    }

    /** 2c. Search by message ID; shows recipient and message. */
    public String searchByMessageId(String id) {
        for (Message m : allMessages) {
            if (m.getMessageId().equals(id)) {
                return m.getMessageContent();
            }
        }
        return "Message ID not found.";
    }

    /** Recipient for a message ID (the brief wants recipient AND message). */
    public String searchByMessageIdFull(String id) {
        for (Message m : allMessages) {
            if (m.getMessageId().equals(id)) {
                return "Recipient: " + m.getRecipientNumber()
                        + " | Message: " + m.getMessageContent();
            }
        }
        return "Message ID not found.";
    }

    /** 2d. All sent or stored messages for one recipient. */
    public List<String> searchByRecipient(String recipient) {
        List<String> results = new ArrayList<>();
        for (Message m : allMessages) {
            boolean relevant = Message.SENT.equals(m.getDeliveryStatus())
                    || Message.STORED.equals(m.getDeliveryStatus());
            if (relevant && m.getRecipientNumber().equals(recipient)) {
                results.add(m.getMessageContent());
            }
        }
        return results;
    }

    /** 2e. Delete a message by hash, from every array. */
    public String deleteByHash(String hash) {
        for (int i = 0; i < allMessages.size(); i++) {
            Message m = allMessages.get(i);
            if (m.getMessageHash().equals(hash)) {
                String content = m.getMessageContent();
                sentMessages.remove(content);
                disregardedMessages.remove(content);
                storedMessages.remove(content);
                messageHashes.remove(m.getMessageHash());
                messageIds.remove(m.getMessageId());
                allMessages.remove(i);
                return "Message \"" + content + "\" successfully deleted.";
            }
        }
        return "Message hash not found.";
    }

    /** Report of all sent messages: hash, recipient, message. */
    public String sentReport() {
        return buildReport(Message.SENT, "SENT MESSAGES REPORT");
    }

    /** 2f. Report of the full details of all stored messages. */
    public String storedReport() {
        return buildReport(Message.STORED, "STORED MESSAGES REPORT");
    }

    private String buildReport(String status, String title) {
        StringBuilder sb = new StringBuilder(title).append("\n");
        boolean any = false;
        for (Message m : allMessages) {
            if (status.equals(m.getDeliveryStatus())) {
                any = true;
                sb.append("-------------------------------------------\n")
                  .append("Message ID: ").append(m.getMessageId()).append("\n")
                  .append("Message Hash: ").append(m.getMessageHash()).append("\n")
                  .append("Recipient: ").append(m.getRecipientNumber()).append("\n")
                  .append("Message: ").append(m.getMessageContent()).append("\n");
            }
        }
        if (!any) {
            sb.append("No messages to display.\n");
        }
        return sb.toString().trim();
    }

    // ------------------------------------------------------------------
    // Array getters (used by the unit tests)
    // ------------------------------------------------------------------

    public ArrayList<String> getSentMessages() { return sentMessages; }
    public ArrayList<String> getDisregardedMessages() { return disregardedMessages; }
    public ArrayList<String> getStoredMessages() { return storedMessages; }
    public ArrayList<String> getMessageHashes() { return messageHashes; }
    public ArrayList<String> getMessageIds() { return messageIds; }
}