package quickchatapp;

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

public class quickchatapp {
    private static final Scanner INPUT = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("REGISTRATION");
        System.out.print("FirstName: ");
        String firstName = INPUT.nextLine();
        System.out.print("LastName: ");
        String lastName = INPUT.nextLine();
        System.out.print("userName: ");
        String username = INPUT.nextLine();
        System.out.print("password: ");
        String password = INPUT.nextLine();
        System.out.print("Cellphone: ");
        String cellphone = INPUT.nextLine();

        Login user = new Login();
        String registrationResult = user.registerUser(
                firstName, lastName, username, password, cellphone);
        System.out.println(registrationResult);
        if (!"User registered successfully.".equals(registrationResult)) {
            System.out.println("Registration failed. Please try again.");
            return;
        }

        System.out.println("\nLOGIN");
        System.out.print("userName: ");
        String loginUsername = INPUT.nextLine();
        System.out.print("password: ");
        String loginPassword = INPUT.nextLine();
        boolean loggedIn = user.loginUser(loginUsername, loginPassword);
        System.out.println(user.returnLoginStatus(loggedIn));
        if (!loggedIn) {
            return;
        }

        try {
            Message.loadStoredMessages();
        } catch (IOException exception) {
            System.out.println("Could not read stored messages: "
                    + exception.getMessage());
            return;
        }

        System.out.println("\nWelcome to QuickChat.");
        int messageLimit = readPositiveNumber(
                "How many messages would you like to enter? ");
        int messagesEntered = 0;
        boolean running = true;

        while (running) {
            showMenu();
            int option = readOption("Choose an option: ", 1, 4);
            switch (option) {
                case 1 -> {
                    if (messagesEntered >= messageLimit) {
                        System.out.println("You have reached your message limit.");
                    } else {
                        createAndProcessMessage(messagesEntered, user);
                        messagesEntered++;
                    }
                }
                case 2 -> System.out.println(Message.printMessages());
                case 3 -> running = false;
                case 4 -> storedMessagesMenu();
                default -> System.out.println("Invalid option.");
            }
        }

        System.out.println("\nTotal messages sent: "
                + Message.getTotalSentCount());
        System.out.println("Goodbye.");
    }

    private static void showMenu() {
        System.out.println("\nQUICKCHAT MENU");
        System.out.println("1) Send Messages");
        System.out.println("2) Show recently sent messages");
        System.out.println("3) Quit");
        System.out.println("4) Stored Messages");
    }

    private static void createAndProcessMessage(int messageNumber, Login user) {
        Message message = new Message(
                messageNumber, "", "", user.getFullName());

        while (true) {
            System.out.print("Recipient cellphone number (+27...): ");
            message.setRecipient(INPUT.nextLine().trim());
            String result = message.checkRecipientCell();
            if ("Cell phone number successfully captured.".equals(result)) {
                System.out.println(result);
                break;
            }
            System.out.println(result);
        }

        while (true) {
            System.out.print("Enter your message (maximum 250 characters): ");
            message.setMessageText(INPUT.nextLine());
            String result = message.checkMessageLength();
            if ("Message ready to send.".equals(result)) {
                System.out.println(result);
                break;
            }
            System.out.println(result);
        }

        System.out.println("\nMessage ID: " + message.getMessageID());
        System.out.println("Message Hash: " + message.getMessageHash());
        System.out.println("Sender: " + message.getSender());
        System.out.println("Recipient: " + message.getRecipient());
        System.out.println("Message: " + message.getMessageText());

        System.out.println("\nChoose what to do with this message:");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message in JSON file");
        int action = readOption("Choose an option: ", 1, 3);
        System.out.println(message.sentMessage(action));
    }

    private static void storedMessagesMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\nSTORED MESSAGES");
            System.out.println("a) Display sender and recipient for all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search messages for a recipient");
            System.out.println("e) Delete a message using its message hash");
            System.out.println("f) Display the full message report");
            System.out.println("0) Return to the QuickChat menu");
            System.out.print("Choose an option: ");
            String option = INPUT.nextLine().trim().toLowerCase();

            switch (option) {
                case "a" -> System.out.println(
                        Message.displayStoredSendersAndRecipients());
                case "b" -> displayLongestStoredMessage();
                case "c" -> searchByMessageID();
                case "d" -> searchByRecipient();
                case "e" -> deleteByHash();
                case "f" -> System.out.println(Message.createFullReport());
                case "0" -> inMenu = false;
                default -> System.out.println("Choose a, b, c, d, e, f, or 0.");
            }
        }
    }

    private static void displayLongestStoredMessage() {
        Optional<Message> longest = Message.getLongestStoredMessage();
        if (longest.isEmpty()) {
            System.out.println("No messages are stored.");
            return;
        }
        Message message = longest.get();
        System.out.println("Longest stored message:");
        System.out.println(message.details());
    }

    private static void searchByMessageID() {
        System.out.print("Enter the message ID: ");
        String id = INPUT.nextLine().trim();
        Optional<Message> found = Message.findByMessageID(id);
        if (found.isEmpty()) {
            System.out.println("No message was found with that ID.");
            return;
        }
        System.out.println("Recipient: "
                + (found.get().getRecipient().isBlank()
                ? "Not provided" : found.get().getRecipient()));
        System.out.println("Message: " + found.get().getMessageText());
    }

    private static void searchByRecipient() {
        System.out.print("Enter the recipient cellphone number: ");
        String recipient = INPUT.nextLine().trim();
        var found = Message.findMessagesForRecipient(recipient);
        if (found.isEmpty()) {
            System.out.println("No sent or stored messages were found for that recipient.");
            return;
        }
        for (Message message : found) {
            System.out.println("Message: " + message.getMessageText());
        }
    }

    private static void deleteByHash() {
        System.out.print("Enter the message hash: ");
        String hash = INPUT.nextLine().trim();
        try {
            Optional<Message> deleted = Message.deleteByHash(hash);
            if (deleted.isEmpty()) {
                System.out.println("No message was found with that hash.");
            } else {
                System.out.println("Message: \"" + deleted.get().getMessageText()
                        + "\" successfully deleted.");
            }
        } catch (IOException exception) {
            System.out.println("The message could not be deleted: "
                    + exception.getMessage());
        }
    }

    private static int readPositiveNumber(String prompt) {
        while (true) {
            int number = readNumber(prompt);
            if (number > 0) {
                return number;
            }
            System.out.println("Enter a number greater than zero.");
        }
    }

    private static int readOption(String prompt, int minimum, int maximum) {
        while (true) {
            int option = readNumber(prompt);
            if (option >= minimum && option <= maximum) {
                return option;
            }
            System.out.println(
                    "Choose a number from " + minimum + " to " + maximum + ".");
        }
    }

    private static int readNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String entered = INPUT.nextLine();
            try {
                return Integer.parseInt(entered.trim());
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}
