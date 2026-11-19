package quickchatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class MessageJsonStore {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private MessageJsonStore() {
    }

    static List<Message> readStoredMessages(Path file) throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }

        JsonNode root = JSON.readTree(file.toFile());
        if (root == null || !root.isArray()) {
            throw new IOException("The messages JSON file must contain an array.");
        }

        List<Message> messages = new ArrayList<>();
        int fallbackNumber = 1;
        for (JsonNode row : root) {
            if (!row.isObject()) {
                throw new IOException("Each stored message must be a JSON object.");
            }

            String id = requiredText(row, "messageID");
            String recipient = requiredText(row, "recipient");
            String text = requiredText(row, "message");
            String hash = row.path("messageHash").asText("");
            String sender = row.path("sender").asText("Developer");
            int messageNumber = extractMessageNumber(hash, fallbackNumber);

            Message message = new Message(
                    id,
                    messageNumber,
                    recipient,
                    text,
                    sender,
                    MessageStatus.STORED
            );
            messages.add(message);
            fallbackNumber++;
        }
        return messages;
    }

    static void writeStoredMessages(Path file, List<Message> messages)
            throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        ArrayNode rows = JSON.createArrayNode();
        for (Message message : messages) {
            ObjectNode row = rows.addObject();
            row.put("messageID", message.getMessageID());
            row.put("messageHash", message.getMessageHash());
            row.put("sender", message.getSender());
            row.put("recipient", message.getRecipient());
            row.put("message", message.getMessageText());
        }
        JSON.writeValue(file.toFile(), rows);
    }

    private static String requiredText(JsonNode row, String field)
            throws IOException {
        JsonNode value = row.get(field);
        if (value == null || value.isNull() || !value.isValueNode()
                || value.asText().isBlank()) {
            throw new IOException("Stored message is missing its " + field + ".");
        }
        return value.asText();
    }

    private static int extractMessageNumber(String hash, int fallback) {
        String[] parts = hash.split(":", -1);
        if (parts.length >= 3) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
                // Older or edited files may have a hash that cannot supply this value.
            }
        }
        return fallback;
    }
}
