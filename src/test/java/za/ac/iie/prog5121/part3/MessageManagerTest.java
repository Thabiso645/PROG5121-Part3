package za.ac.iie.prog5121.part3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Part 3 unit tests, using the test data from the brief.
 */
public class MessageManagerTest {

    private MessageManager manager;

    @BeforeEach
    public void setUp() {
        manager = new MessageManager();

        // Test Data Message 1 - Sent
        manager.addMessage(new Message("1111111111", 0, "+27834557896",
                "Did you get the cake?", Message.SENT));
        // Test Data Message 2 - Stored
        manager.addMessage(new Message("2222222222", 1, "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", Message.STORED));
        // Test Data Message 3 - Disregard
        manager.addMessage(new Message("3333333333", 2, "+27834484567",
                "Yohoooo, I am at your gate.", Message.DISREGARDED));
        // Test Data Message 4 - Sent
        manager.addMessage(new Message("0838884567", 3, "0838884567",
                "It is dinner time!", Message.SENT));
        // Test Data Message 5 - Stored
        manager.addMessage(new Message("5555555555", 4, "+27838884567",
                "Ok, I am leaving without you.", Message.STORED));
    }

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        List<String> expected = Arrays.asList("Did you get the cake?", "It is dinner time!");
        assertEquals(expected, manager.getSentMessages());
    }

    @Test
    public void testDisregardedAndStoredArraysPopulated() {
        assertEquals(Arrays.asList("Yohoooo, I am at your gate."), manager.getDisregardedMessages());
        assertEquals(Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."), manager.getStoredMessages());
    }

    @Test
    public void testHashAndIdArraysPopulated() {
        assertEquals(5, manager.getMessageHashes().size());
        assertEquals(5, manager.getMessageIds().size());
        assertTrue(manager.getMessageIds().contains("0838884567"));
    }

    @Test
    public void testDisplayLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                manager.longestMessage());
    }

    @Test
    public void testSearchForMessageId() {
        assertEquals("It is dinner time!", manager.searchByMessageId("0838884567"));
    }

    @Test
    public void testSearchForMessageIdShowsRecipientAndMessage() {
        assertEquals("Recipient: 0838884567 | Message: It is dinner time!",
                manager.searchByMessageIdFull("0838884567"));
    }

    @Test
    public void testSearchMessagesForRecipient() {
        List<String> expected = Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you.");
        assertEquals(expected, manager.searchByRecipient("+27838884567"));
    }

    @Test
    public void testDeleteMessageUsingMessageHash() {
        String hash = manager.getMessageHashes().get(1); // Test Message 2
        assertEquals("22:1:WHERETIME", hash);
        assertEquals("Message \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                manager.deleteByHash(hash));
        assertFalse(manager.getStoredMessages().contains(
                "Where are you? You are late! I have asked you to be on time."));
        assertEquals(4, manager.getMessageHashes().size());
    }

    @Test
    public void testDeleteUnknownHash() {
        assertEquals("Message hash not found.", manager.deleteByHash("NOPE"));
    }

    @Test
    public void testDisplayReport() {
        String report = manager.sentReport();
        assertTrue(report.contains("0838884567"));
        assertTrue(report.contains("It is dinner time!"));
        assertTrue(report.contains(manager.getMessageHashes().get(3)));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("Did you get the cake?"));
        assertFalse(report.contains("Ok, I am leaving without you."));
    }

    @Test
    public void testStoredMessagesSurviveJsonRoundTrip(@TempDir Path dir) throws IOException {
        String file = dir.resolve("stored.json").toString();
        manager.saveStoredToJson(file);

        MessageManager fresh = new MessageManager();
        assertEquals(2, fresh.loadStoredFromJson(file));
        assertEquals(manager.getStoredMessages(), fresh.getStoredMessages());
        assertEquals(2, fresh.searchByRecipient("+27838884567").size());
    }
}
