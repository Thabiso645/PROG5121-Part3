package za.ac.iie.prog5121.part3;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * QuickChat console application: Part 1 (register / login), Part 2 (send
 * messages) and Part 3 (arrays, JSON storage, Stored Messages menu).
 */
public class Main {

    private static final String JSON_FILE = "storedMessages.json";

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==============================");
        System.out.println("         CHAT APP            ");
        System.out.println("   REGISTRATION AND LOGIN   ");
        System.out.println("==============================");

        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

        System.out.println("\n========== REGISTRATION ==========");
        System.out.print("Enter a username: ");
        String username = scanner.nextLine();
        System.out.print("Enter a password: ");
        String password = scanner.nextLine();
        System.out.print("Enter Cellphone Number: ");
        String cell = scanner.nextLine();

        Login user = new Login(username, password, cell, firstName, lastName);
        System.out.println("\n" + user.registerUser());

        if (!user.checkUserName() || !user.checkPasswordComplexity() || !user.checkCellPhoneNumber()) {
            System.out.println("Registration failed due to incorrect formatting. Terminating program.");
            return;
        }

        System.out.println("\n============= LOGIN =============");
        System.out.print("Enter Username: ");
        String loginUser = scanner.nextLine();
        System.out.print("Enter Password: ");
        String loginPass = scanner.nextLine();

        boolean isAuthed = user.loginUser(loginUser, loginPass);
        System.out.println(user.returnLoginStatus(isAuthed));
        if (!isAuthed) {
            return;
        }

        // ---------------- QUICKCHAT ----------------
        MessageManager manager = new MessageManager();

        // Part 3: populate the stored-messages array from the JSON file.
        try {
            int loaded = manager.loadStoredFromJson(JSON_FILE);
            if (loaded > 0) {
                System.out.println("\nLoaded " + loaded + " stored message(s) from " + JSON_FILE + ".");
            }
        } catch (RuntimeException e) {
            System.out.println("\nCould not read " + JSON_FILE + ": " + e.getMessage());
        }

        System.out.print("\nEnter the number of messages you wish to send: ");
        int totalLimit = readInt(scanner);

        int currentMessageCount = 0;
        boolean appRunning = true;

        while (appRunning) {
            System.out.println("\nMAIN MENU:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");

            switch (readInt(scanner)) {
                case 1 -> {
                    if (currentMessageCount >= totalLimit) {
                        System.out.println("Limit reached! You can only enter " + totalLimit + " messages.");
                        break;
                    }
                    System.out.print("\nEnter Recipient Number (e.g. +27718693002): ");
                    String recipient = scanner.nextLine();
                    System.out.print("Enter Message Content: ");
                    String content = scanner.nextLine();

                    Message msg = new Message(currentMessageCount, recipient, content);

                    String lengthCheck = msg.checkMessageLength();
                    if (lengthCheck.contains("exceeds")) {
                        System.out.println("[Error] " + lengthCheck);
                        break;
                    }
                    System.out.println("[Status] " + msg.checkRecipientCell());

                    System.out.println("\nProcessing Status Options:");
                    System.out.println("1 - Send Message");
                    System.out.println("2 - Disregard Message");
                    System.out.println("3 - Store Message to send later");
                    System.out.print("Select action: ");
                    int choice = readInt(scanner);

                    System.out.println("\n" + msg.SentMessage(choice));
                    System.out.println(msg.printMessages());

                    manager.addMessage(msg);
                    if (Message.STORED.equals(msg.getDeliveryStatus())) {
                        save(manager);
                    }
                    currentMessageCount++;
                }
                case 2 -> System.out.println("\n" + manager.sentReport());
                case 3 -> {
                    System.out.println("\n=======================================================");
                    System.out.println("Total successful messages processed: " + Message.returnTotalMessagess());
                    System.out.println("Exiting QuickChat. Goodbye!");
                    System.out.println("=======================================================");
                    appRunning = false;
                }
                case 4 -> storedMessagesMenu(scanner, manager, user);
                default -> System.out.println("Invalid selection. Please try again.");
            }
        }
        scanner.close();
    }

    private static void storedMessagesMenu(Scanner scanner, MessageManager manager, Login user) {
        boolean back = false;
        while (!back) {
            System.out.println("\nSTORED MESSAGES:");
            System.out.println("a) Display sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search all messages stored for a recipient");
            System.out.println("e) Delete a message using its message hash");
            System.out.println("f) Display report of all stored messages");
            System.out.println("g) Back to main menu");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim().toLowerCase()) {
                case "a" -> System.out.println("\n" + manager.displaySenderAndRecipient(user.getCellPhoneNumber()));
                case "b" -> {
                    String longest = manager.longestStoredMessage();
                    System.out.println("\nLongest stored message: "
                            + (longest.isEmpty() ? "none" : longest));
                }
                case "c" -> {
                    System.out.print("Enter message ID: ");
                    System.out.println("\n" + manager.searchByMessageIdFull(scanner.nextLine().trim()));
                }
                case "d" -> {
                    System.out.print("Enter recipient number: ");
                    List<String> found = manager.searchByRecipient(scanner.nextLine().trim());
                    if (found.isEmpty()) {
                        System.out.println("\nNo messages found for that recipient.");
                    } else {
                        System.out.println();
                        found.forEach(m -> System.out.println("- " + m));
                    }
                }
                case "e" -> {
                    System.out.print("Enter message hash: ");
                    System.out.println("\n" + manager.deleteByHash(scanner.nextLine().trim().toUpperCase()));
                    save(manager); // keep the JSON file in step with the array
                }
                case "f" -> System.out.println("\n" + manager.storedReport());
                case "g" -> back = true;
                default -> System.out.println("Invalid selection. Please try again.");
            }
        }
    }

    private static void save(MessageManager manager) {
        try {
            manager.saveStoredToJson(JSON_FILE);
        } catch (IOException e) {
            System.out.println("Could not write " + JSON_FILE + ": " + e.getMessage());
        }
    }

    /** Reads a whole line and parses it, so bad input doesn't crash the menu. */
    private static int readInt(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a number: ");
            }
        }
    }
}