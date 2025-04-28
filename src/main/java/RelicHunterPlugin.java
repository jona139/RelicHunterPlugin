// 1. RelicHunterPlugin.java (Main Plugin Class)
package net.runelite.client.plugins.relichunterpoc;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar; // Import ClientToolbar
import net.runelite.client.ui.NavigationButton; // Import NavigationButton
import net.runelite.client.util.ImageUtil; // Import ImageUtil for icon loading

import java.awt.image.BufferedImage; // Import BufferedImage
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@PluginDescriptor(
        name = "Relic Hunter PoC",
        description = "Proof-of-concept for Relic Hunter unlock logic via Copper Mining.",
        tags = {"relic", "hunter", "ironman", "poc", "unlock", "panel"}
)
public class RelicHunterPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private RelicHunterConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ClientToolbar clientToolbar; // Inject toolbar

    private RelicHunterState state;
    private RelicHunterPanel panel; // Add panel variable
    private NavigationButton navButton; // Add nav button variable
    private final Random random = new Random();
    private boolean needsStateLoad = false;

    private static final String THIEVING_NOVICE = RelicHunterState.THIEVING_NOVICE;
    private static final String THIEVING_APPRENTICE = RelicHunterState.THIEVING_APPRENTICE;

    // Path to the icon resource (place icon.png in src/main/resources)
    private static final String ICON_PATH = "/icon.png";

    @Override
    protected void startUp() throws Exception {
        log.info("Relic Hunter PoC starting!");
        state = new RelicHunterState(configManager);
        panel = new RelicHunterPanel(state); // Initialize the panel

        // Load the icon for the sidebar button
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), ICON_PATH);
        if (icon == null) {
            log.warn("Failed to load sidebar icon!");
        }

        // Build the navigation button
        navButton = NavigationButton.builder()
                .tooltip("Relic Hunter PoC")
                .icon(icon) // Set the loaded icon (can be null if loading failed)
                .priority(8) // Adjust priority as needed
                .panel(panel)
                .build();

        // Add the button to the client toolbar
        clientToolbar.addNavigation(navButton);

        if (client.getGameState() == GameState.LOGGED_IN) {
            needsStateLoad = true;
        }
        sendChatMessage("Relic Hunter PoC Plugin enabled.");
        // Initial panel update if state is already loaded (e.g., plugin restart while logged in)
        if (state.isUsernameSet()) {
            panel.updateContent();
        }
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Relic Hunter PoC stopped!");
        // Remove the navigation button from the toolbar
        clientToolbar.removeNavigation(navButton);
        // Clear state and panel references
        state = null;
        panel = null;
        navButton = null;
    }

    // --- Event Subscription ---

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            log.debug("Player logged in, scheduling state load.");
            needsStateLoad = true;
        } else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.HOPPING) {
            needsStateLoad = false;
            if (state != null) {
                state.clearLoadedData();
                log.info("Player logged out/hopped, cleared loaded state.");
                if (panel != null) {
                    panel.updateContent(); // Update panel to show empty state
                }
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        if (needsStateLoad) {
            String username = client.getUsername();
            if (username != null && !username.isEmpty() && !username.trim().isEmpty()) {
                log.info("Game tick after login, attempting to load state for user: {}", username);
                if (state != null) {
                    state.setPlayerUsername(username);
                    state.loadState();
                    log.info("Relic Hunter state loaded for user: {}. Unlocks: {}", username, state.getUnlockedContent());
                    sendChatMessage("Loaded unlocks: " + state.getFormattedUnlockedContent()); // Use formatted list
                    needsStateLoad = false;
                    if (panel != null) {
                        panel.updateContent(); // Update panel after loading state
                    }
                } else {
                    log.error("State object is null during GameTick load attempt.");
                    needsStateLoad = false;
                }
            } else {
                log.debug("Username not available yet on GameTick, waiting...");
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (state == null || !state.isUsernameSet()) {
            return;
        }
        if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }
        if (event.getMessage().equals("You manage to mine some copper.")) {
            handleCopperOreMined();
        }
    }

    // --- Core Logic ---

    private void handleCopperOreMined() {
        if (state == null || !state.isUsernameSet()) {
            log.warn("handleCopperOreMined called but state is not ready.");
            return;
        }
        log.debug("Copper ore mined message detected by user: {}", state.getPlayerUsername());
        double relicChance = config.copperRelicChance() / 100.0;

        if (random.nextDouble() < relicChance) {
            sendChatMessage("Success! A Skilling Relic was found!");
            log.info("Skilling Relic found from copper mining for user: {}", state.getPlayerUsername());
            handleSkillingRelicFound();
        } else {
            log.debug("No relic found from this copper ore for user: {}", state.getPlayerUsername());
        }
    }

    private void handleSkillingRelicFound() {
        if (state == null || !state.isUsernameSet()) {
            log.error("Relic Hunter State is not ready in handleSkillingRelicFound!");
            return;
        }
        List<String> potentialUnlocks = new ArrayList<>();
        if (!state.isUnlocked(THIEVING_NOVICE)) {
            potentialUnlocks.add(THIEVING_NOVICE);
        } else if (state.isUnlocked(THIEVING_NOVICE) && !state.isUnlocked(THIEVING_APPRENTICE)) {
            potentialUnlocks.add(THIEVING_APPRENTICE);
        }

        if (potentialUnlocks.isEmpty()) {
            sendChatMessage("Found a Skilling Relic, but no new Thieving unlocks are available from this pool right now.");
            log.info("Skilling Relic found, but no valid unlocks in PoC pool for user: {}", state.getPlayerUsername());
        } else {
            String offeredUnlock = potentialUnlocks.get(0);
            sendChatMessage("Potential unlock from Skilling Relic: " + RelicHunterState.formatUnlockName(offeredUnlock)); // Use static method
            log.info("Offering unlock: {} to user: {}", offeredUnlock, state.getPlayerUsername());

            sendChatMessage("Simulating player choosing: " + RelicHunterState.formatUnlockName(offeredUnlock));
            boolean newlyUnlocked = state.unlockContent(offeredUnlock);
            if (newlyUnlocked) {
                sendChatMessage("Unlocked: " + RelicHunterState.formatUnlockName(offeredUnlock) + ". New unlocks: " + state.getFormattedUnlockedContent()); // Use formatted list
                log.info("Content unlocked: {}. Current unlocks for {}: {}", offeredUnlock, state.getPlayerUsername(), state.getUnlockedContent());
                if (panel != null) {
                    panel.updateContent(); // Update panel after unlocking content
                }
            } else {
                sendChatMessage(RelicHunterState.formatUnlockName(offeredUnlock) + " was already unlocked.");
                log.warn("Attempted to unlock {}, but it was already unlocked for user: {}", offeredUnlock, state.getPlayerUsername());
            }
        }
    }

    private void sendChatMessage(String message) {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "[Relic Hunter PoC] " + message, null);
    }

    // formatUnlockName removed, use RelicHunterState.formatUnlockName directly

    @Provides
    RelicHunterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(RelicHunterConfig.class);
    }
}
