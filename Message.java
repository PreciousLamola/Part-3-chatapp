package quickchatapp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Message {
    public static final int ID_LENGTH = 10;
    public static final int MAX_MESSAGE_LENGTH = 250;
    public static final String JSON_FILE = "messages.json";

    private static final List<Message> SENT_MESSAGES = new ArrayList<>();
    private static final List<Message> DISREGARDED_MESSAGES = new ArrayList<>();
    private static final List<Message> STORED_MESSAGES = new ArrayList<>();

    private final String messageID;
    private final int messageNumber;
    private final String sender;
    private String recipient;
    private String message;
    private MessageStatus status = MessageStatus.PENDING;

    public Message(String messageID, int messageNumber,
                   String recipient, String message) {
        this(messageID, messageNumber, recipient, message, "Developer",
                MessageStatus.PENDING);
    }

    public Message(String messageID, int messageNumber,
                   String recipient, String message, String sender) {
        this(messageID, messageNumber, recipient, message, sender,
                MessageStatus.PENDING);
    }

    Message(String messageID, int messageNumber, String recipient,
            String message, String sender, MessageStatus status) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.message = message;
        this.sender = sender == null || sender.isBlank() ? "Developer" : sender;
        this.status = status == null ? MessageStatus.PENDING : status;
    }

    public Message(int messageNumber, String recipient, String message) {
        this(generateID(), messageNumber, recipient, message);
    }

    public Message(int messageNumber, String recipient, String message,
                   String sender) {
        this(generateID(), messageNumber, recipient, message, sender);
    }

    private static String generateID() {
        long number = ThreadLocalRandom.current().nextLong(
                1_000_000_000L, 10_000_000_000L);
        return String.format("%010d", number);
    }

    public String getMessageID() {
        return messageID;
    }

    public String getMessageHash() {
        return createMessageHash();
    }

    public String getMessage() {
        return message;
    }

    public String getMessageText() {
        return message;
    }

    public String getRecipient() {
        return recipient == null ? "" : recipient;
    }

    public String getSender() {
        return sender;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setMessageText(String message) {
        this.message = message;
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.matches("\\d{10}");
    }

    public String describeMessageID() {
        return "Message ID generated: " + messageID;
    }

    public String checkMessageLength() {
        if (message == null || message.trim().isEmpty()) {
            return "Message cannot be empty.";
        }

        if (message.length() <= MAX_MESSAGE_LENGTH) {
            return "Message ready to send.";
        }

        return "Message exceeds 250 characters by "
                + (message.length() - MAX_MESSAGE_LENGTH)
                + "; please reduce the size.";
    }

    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("^\\+\\d{10,15}$")) {
            return "Cell phone number successfully captured.";
        }

        return "Cell phone number must start with + and contain "
                + "10 to 15 digits after the plus sign.";
    }

    public String createMessageHash() {
        String prefix = messageID != null && messageID.length() >= 2
                ? messageID.substring(0, 2) : "00";

        String firstWord = "";
        String lastWord = "";
        if (message != null && !message.trim().isEmpty()) {
            String[] words = message.trim().split("\\s+");
            firstWord = words[0].replaceAll("[^a-zA-Z0-9]", "");
            lastWord = words[words.length - 1]
                    .replaceAll("[^a-zA-Z0-9]", "");
        }

        return (prefix + ":" + messageNumber + ":"
                + firstWord + lastWord).toUpperCase(Locale.ROOT);
    }

    public String sentMessage(int choice) {
        if (choice < 1 || choice > 3) {
            return "Invalid choice.";
        }

        if (!checkMessageID()) {
            return "Invalid message ID.";
        }
        if (!"Message ready to send.".equals(checkMessageLength())) {
            return checkMessageLength();
        }
        if (!"Cell phone number successfully captured."
                .equals(checkRecipientCell())) {
            return checkRecipientCell();
        }

        try {
            return switch (choice) {
                case 1 -> addToArray(this, MessageStatus.SENT,
                        SENT_MESSAGES, "Message successfully sent.");
                case 2 -> addToArray(this, MessageStatus.DISREGARDED,
                        DISREGARDED_MESSAGES, "Message disregarded.");
                case 3 -> storeMessage();
                default -> "Invalid choice.";
            };
        } catch (IOException exception) {
            return "Error storing message: " + exception.getMessage();
        }
    }

    // Maintains compatibility with the original Part 2 main class.
    public String SentMessage(int choice) {
        return sentMessage(choice);
    }

    private String storeMessage() throws IOException {
        if (containsMessageID(messageID)) {
            return "A message with this ID is already in the arrays.";
        }

        status = MessageStatus.STORED;
        STORED_MESSAGES.add(this);
        try {
            saveStoredMessages(Paths.get(JSON_FILE));
            return "Message successfully stored.";
        } catch (IOException exception) {
            STORED_MESSAGES.remove(this);
            status = MessageStatus.PENDING;
            throw exception;
        }
    }

    public String details() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + getMessageHash() + "\n"
                + "Sender: " + sender + "\n"
                + "Recipient: " + getRecipient() + "\n"
                + "Message: " + message + "\n"
                + "Status: " + status;
    }

    public static String printMessages() {
        return formatMessages(SENT_MESSAGES, "No messages have been sent yet.");
    }

    public static String printDisregardedMessages() {
        return formatMessages(DISREGARDED_MESSAGES,
                "No messages have been disregarded.");
    }

    public static String printStoredMessages() {
        return formatMessages(STORED_MESSAGES, "No messages are stored.");
    }

    public static String displayStoredSendersAndRecipients() {
        if (STORED_MESSAGES.isEmpty()) {
            return "No messages are stored.";
        }
        StringBuilder result = new StringBuilder();
        for (Message stored : STORED_MESSAGES) {
            if (!result.isEmpty()) {
                result.append(System.lineSeparator());
            }
            result.append("Sender: ").append(stored.getSender())
                    .append(" | Recipient: ").append(stored.getRecipient());
        }
        return result.toString();
    }

    public static Optional<Message> getLongestStoredMessage() {
        return STORED_MESSAGES.stream()
                .max((first, second) -> Integer.compare(
                        first.getMessageText().length(),
                        second.getMessageText().length()));
    }

    public static Optional<Message> findByMessageID(String messageID) {
        String query = normalize(messageID);
        return allMessages().stream()
                .filter(item -> normalize(item.getMessageID()).equals(query))
                .findFirst();
    }

    public static List<Message> findMessagesForRecipient(String recipient) {
        String query = normalize(recipient);
        return allMessages().stream()
                .filter(item -> item.getStatus() != MessageStatus.DISREGARDED)
                .filter(item -> normalize(item.getRecipient()).equals(query))
                .toList();
    }

    public static Optional<Message> deleteByHash(String hash) throws IOException {
        return deleteByHash(hash, Paths.get(JSON_FILE));
    }

    public static Optional<Message> deleteByHash(String hash, Path file)
            throws IOException {
        String query = normalize(hash);
        Optional<Message> match = allMessages().stream()
                .filter(item -> normalize(item.getMessageHash()).equals(query))
                .findFirst();
        if (match.isEmpty()) {
            return Optional.empty();
        }

        Message message = match.get();
        List<Message> owner = listForStatus(message.getStatus());
        owner.remove(message);
        if (message.getStatus() == MessageStatus.STORED) {
            try {
                saveStoredMessages(file);
            } catch (IOException exception) {
                owner.add(message);
                throw exception;
            }
        }
        return match;
    }

    public static String createFullReport() {
        return formatMessages(allMessages(),
                "No messages are available for the report.");
    }

    public static Message[] getSentMessagesArray() {
        return SENT_MESSAGES.toArray(Message[]::new);
    }

    public static Message[] getDisregardedMessagesArray() {
        return DISREGARDED_MESSAGES.toArray(Message[]::new);
    }

    public static Message[] getStoredMessagesArray() {
        return STORED_MESSAGES.toArray(Message[]::new);
    }

    public static String[] getMessageHashes() {
        return allMessages().stream()
                .map(Message::getMessageHash)
                .toArray(String[]::new);
    }

    public static String[] getMessageIDs() {
        return allMessages().stream()
                .map(Message::getMessageID)
                .toArray(String[]::new);
    }

    public static List<Message> getAllMessages() {
        return List.copyOf(allMessages());
    }

    public static void loadStoredMessages() throws IOException {
        loadStoredMessages(Paths.get(JSON_FILE));
    }

    public static void loadStoredMessages(Path file) throws IOException {
        STORED_MESSAGES.clear();
        STORED_MESSAGES.addAll(MessageJsonStore.readStoredMessages(file));
    }

    public static void saveStoredMessages(Path file) throws IOException {
        MessageJsonStore.writeStoredMessages(file, STORED_MESSAGES);
    }

    public static int returnTotalMessages() {
        return SENT_MESSAGES.size();
    }

    public static int getTotalSentCount() {
        return SENT_MESSAGES.size();
    }

    public static void resetAll() {
        SENT_MESSAGES.clear();
        DISREGARDED_MESSAGES.clear();
        STORED_MESSAGES.clear();
    }

    static void addMessageToArray(Message message, MessageStatus status) {
        List<Message> destination = listForStatus(status);
        if (containsMessageID(message.getMessageID())) {
            throw new IllegalArgumentException(
                    "A message with this ID is already in the arrays.");
        }
        message.status = status;
        destination.add(message);
    }

    private static String addToArray(Message message, MessageStatus status,
                                     List<Message> destination, String success) {
        if (containsMessageID(message.getMessageID())) {
            return "A message with this ID is already in the arrays.";
        }
        message.status = status;
        destination.add(message);
        return success;
    }

    private static List<Message> listForStatus(MessageStatus status) {
        return switch (status) {
            case SENT -> SENT_MESSAGES;
            case DISREGARDED -> DISREGARDED_MESSAGES;
            case STORED -> STORED_MESSAGES;
            case PENDING -> throw new IllegalArgumentException(
                    "A pending message is not in a message array.");
        };
    }

    private static boolean containsMessageID(String id) {
        return allMessages().stream()
                .anyMatch(item -> item.getMessageID().equals(id));
    }

    private static List<Message> allMessages() {
        List<Message> all = new ArrayList<>();
        all.addAll(SENT_MESSAGES);
        all.addAll(DISREGARDED_MESSAGES);
        all.addAll(STORED_MESSAGES);
        return all;
    }

    private static String formatMessages(List<Message> messages, String emptyText) {
        if (messages.isEmpty()) {
            return emptyText;
        }
        StringBuilder result = new StringBuilder();
        for (Message item : messages) {
            if (!result.isEmpty()) {
                result.append(System.lineSeparator())
                        .append(System.lineSeparator());
            }
            result.append(item.details());
        }
        return result.toString();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
