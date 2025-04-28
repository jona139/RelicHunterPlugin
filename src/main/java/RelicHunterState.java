// 3. RelicHunterState.java (State Management Class)
// Added getFormattedUnlockedContent()
package net.runelite.client.plugins.relichunterpoc;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class RelicHunterState {

    private static final String CONFIG_GROUP = "relichunterpoc";
    private static final String UNLOCKED_CONTENT_KEY = "unlockedContent";

    private final ConfigManager configManager;

    @Getter
    private String playerUsername;

    @Getter
    private Set<String> unlockedContent = new HashSet<>();

    public static final String THIEVING_NOVICE = "THIEVING_NOVICE";
    public static final String THIEVING_APPRENTICE = "THIEVING_APPRENTICE";


    public RelicHunterState(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void setPlayerUsername(String username) {
        if (username == null || username.isEmpty() || username.trim().isEmpty()) {
            log.warn("Attempted to set an invalid username.");
            return;
        }
        if (!username.equals(this.playerUsername)) {
            log.info("Setting Relic Hunter state username to: {}", username);
            this.playerUsername = username;
        }
    }

    public boolean isUsernameSet() {
        return this.playerUsername != null && !this.playerUsername.isEmpty();
    }

    public void loadState() {
        if (!isUsernameSet()) {
            log.warn("Cannot load Relic Hunter state: Username not set.");
            unlockedContent = new HashSet<>();
            return;
        }
        String storedValue = configManager.getRSProfileConfiguration(CONFIG_GROUP, UNLOCKED_CONTENT_KEY);
        if (storedValue != null && !storedValue.isEmpty()) {
            try {
                unlockedContent = new HashSet<>(Arrays.asList(storedValue.split(",")));
                log.debug("Loaded unlocked content for {}: {}", playerUsername, unlockedContent);
            } catch (Exception e) {
                log.error("Failed to parse stored unlocked content for user {}: {}", playerUsername, storedValue, e);
                unlockedContent = new HashSet<>();
            }
        } else {
            unlockedContent = new HashSet<>();
            log.debug("No saved state found for {}, initializing empty set.", playerUsername);
        }
    }

    private void saveState() {
        if (!isUsernameSet()) {
            log.error("Cannot save Relic Hunter state: Username not set.");
            return;
        }
        if (unlockedContent == null) {
            log.error("Cannot save Relic Hunter state: unlockedContent set is null!");
            return;
        }
        if (unlockedContent.isEmpty()) {
            configManager.unsetRSProfileConfiguration(CONFIG_GROUP, UNLOCKED_CONTENT_KEY);
            log.debug("Saved empty unlocked content state for {}.", playerUsername);
        } else {
            String valueToStore = String.join(",", unlockedContent);
            configManager.setRSProfileConfiguration(CONFIG_GROUP, UNLOCKED_CONTENT_KEY, valueToStore);
            log.debug("Saved unlocked content state for {}: {}", playerUsername, valueToStore);
        }
    }

    public boolean isUnlocked(String contentId) {
        if (unlockedContent == null) {
            log.warn("isUnlocked check failed: unlockedContent set is null!");
            return false;
        }
        return unlockedContent.contains(contentId);
    }

    public boolean unlockContent(String contentId) {
        if (!isUsernameSet()) {
            log.error("Cannot unlock content '{}': Username not set.", contentId);
            return false;
        }
        if (unlockedContent == null) {
            log.error("Cannot unlock content '{}': unlockedContent set is null!", contentId);
            return false;
        }
        if (unlockedContent.add(contentId)) {
            log.info("Unlocking content: {}", contentId);
            saveState();
            return true;
        }
        return false;
    }

    public void resetState() {
        if (!isUsernameSet()) {
            log.warn("Cannot reset state: Username not set.");
            if (unlockedContent != null) unlockedContent.clear();
            return;
        }
        if (unlockedContent != null) {
            unlockedContent.clear();
            saveState();
            log.info("Relic Hunter state reset for user {}.", playerUsername);
        } else {
            log.warn("Cannot reset state: unlockedContent set is null!");
        }
    }

    public void clearLoadedData() {
        if (unlockedContent != null) {
            unlockedContent.clear();
        }
        log.debug("Cleared loaded Relic Hunter state data.");
    }

    /**
     * Gets the set of unlocked content formatted for display.
     * @return A string representation of the unlocked content, or "None" if empty.
     */
    public String getFormattedUnlockedContent() {
        if (unlockedContent == null || unlockedContent.isEmpty()) {
            return "None";
        }
        // Format each ID using formatUnlockName and join them
        return unlockedContent.stream()
                .map(RelicHunterState::formatUnlockName) // Use the static formatter
                .sorted() // Sort alphabetically for consistency
                .collect(Collectors.joining(", "));
    }

    /**
     * Helper method to make unlock names more readable (static version).
     * @param contentId The identifier string.
     * @return A formatted string.
     */
    public static String formatUnlockName(String contentId) {
        if (contentId == null) return "Unknown";
        switch (contentId) {
            case THIEVING_NOVICE: return "Thieving (Novice)";
            case THIEVING_APPRENTICE: return "Thieving (Apprentice)";
            default: return contentId; // Fallback
        }
    }
}