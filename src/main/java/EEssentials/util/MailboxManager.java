package EEssentials.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages the storage and retrieval of player mail.
 */
public class MailboxManager {

    // Maximum number of messages each player can store in their mailbox.
    private static final int DEFAULT_MAX_MESSAGES = 30;

    // Path to the directory where mail is stored.
    private final Path storagePath;

    // Store player mailboxes in memory for quick access.
    private final Map<UUID, List<MailMessage>> playerMailboxes = new HashMap<>();

    // Gson instance for serializing and deserializing.
    private final Gson gson;

    // Logger instance for logging messages related to EEssentials.
    public static final Logger LOGGER = LoggerFactory.getLogger("EEssentials");

    // Singleton instance of MailboxManager
    private static MailboxManager instance;

    /**
     * Initializes the MailboxManager with a given storage path.
     *
     * @param storagePath The path to the directory where mail should be saved.
     */
    public MailboxManager(Path storagePath) {
        this.storagePath = storagePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadAllMailboxes();
    }

    /**
     * Get the singleton instance of MailboxManager.
     *
     * @return The instance of MailboxManager.
     */
    public static MailboxManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MailboxManager has not been initialized.");
        }
        return instance;
    }

    /**
     * Initialize the singleton instance of MailboxManager.
     *
     * @param storagePath The path to the directory where mail should be saved.
     */
    public static void initialize(Path storagePath) {
        if (instance == null) {
            instance = new MailboxManager(storagePath);
        }
    }

    /**
     * Send a mail message to a player's mailbox.
     *
     * @param sender      The player sending the message.
     * @param receiverUuid The UUID of the player receiving the message.
     * @param message     The message content.
     */
    public void sendMail(ServerPlayerEntity sender, UUID receiverUuid, String message) {
        String senderName = sender != null ? sender.getName().getString() : "Console";
        MailMessage mailMessage = new MailMessage(senderName, message, System.currentTimeMillis());

        // Get the receiver's mailbox, or create a new one if it doesn't exist.
        List<MailMessage> mailbox = playerMailboxes.getOrDefault(receiverUuid, new ArrayList<>());

        // Ensure the mailbox doesn't exceed the maximum allowed size.
        if (mailbox.size() >= DEFAULT_MAX_MESSAGES) {
            mailbox.remove(0); // Remove the oldest message to make space.
        }

        // Add the new message and update the mailbox.
        mailbox.add(mailMessage);
        playerMailboxes.put(receiverUuid, mailbox);

        // Save the mailbox to file.
        saveMailbox(receiverUuid, mailbox);
    }

    /**
     * Retrieve all mail messages for a player.
     *
     * @param playerUuid The UUID of the player.
     * @return A list of mail messages for the player.
     */
    public List<MailMessage> getMail(UUID playerUuid) {
        return playerMailboxes.getOrDefault(playerUuid, new ArrayList<>());
    }

    /**
     * Clear all messages from a player's mailbox.
     *
     * @param playerUuid The UUID of the player.
     */
    public void clearAllMail(UUID playerUuid) {
        playerMailboxes.remove(playerUuid);
        saveMailbox(playerUuid, new ArrayList<>());
    }

    /**
     * Clear a specific message from a player's mailbox.
     *
     * @param playerUuid  The UUID of the player.
     * @param messageIndex The index of the message to clear.
     * @return true if the message was successfully cleared, false otherwise.
     */
    public boolean clearMessage(UUID playerUuid, int messageIndex) {
        List<MailMessage> mailbox = playerMailboxes.get(playerUuid);
        if (mailbox != null && messageIndex >= 0 && messageIndex < mailbox.size()) {
            mailbox.remove(messageIndex);
            saveMailbox(playerUuid, mailbox);
            return true;
        }
        return false;
    }

    /**
     * Loads all player mailboxes from storage.
     */
    private void loadAllMailboxes() {
        File[] files = storagePath.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    UUID playerUuid = UUID.fromString(file.getName().replace(".json", ""));
                    List<MailMessage> mailbox = gson.fromJson(reader, new TypeToken<List<MailMessage>>() {}.getType());
                    playerMailboxes.put(playerUuid, mailbox != null ? mailbox : new ArrayList<>());
                } catch (IOException | JsonParseException e) {
                    LOGGER.warn("Failed to load mailbox for player: " + file.getName(), e);
                }
            }
        }
    }

    /**
     * Save a player's mailbox to file.
     *
     * @param playerUuid The UUID of the player.
     * @param mailbox    The mailbox to save.
     */
    private void saveMailbox(UUID playerUuid, List<MailMessage> mailbox) {
        File saveFile = new File(storagePath.toFile(), playerUuid + ".json");
        try (Writer writer = new FileWriter(saveFile)) {
            gson.toJson(mailbox, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save mailbox for player: " + playerUuid, e);
        }
    }

    /**
     * Represents a mail message.
     */
    public record MailMessage(String sender, String content, long timestamp) {
    }
}
