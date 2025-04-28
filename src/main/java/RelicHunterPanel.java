// 4. RelicHunterPanel.java (NEW FILE - Sidebar Panel UI)
package net.runelite.client.plugins.relichunterpoc;

import net.runelite.client.ui.PluginPanel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RelicHunterPanel extends PluginPanel {

    private final RelicHunterState state;
    private final JLabel unlockedLabel; // Label to display unlocked content

    // Constructor takes the state object to access data
    RelicHunterPanel(RelicHunterState state) {
        super(false); // 'false' means the panel is not wrapped in a scroll pane automatically
        this.state = state;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        // --- Title Panel ---
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Bottom margin

        JLabel title = new JLabel("Relic Hunter PoC");
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(title, BorderLayout.CENTER);

        // --- Content Panel ---
        JPanel contentPanel = new JPanel();
        // Use BoxLayout for simple vertical stacking
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Label for unlocked items
        contentPanel.add(new JLabel("Unlocked Relics:"));
        unlockedLabel = new JLabel("Loading..."); // Initial text
        unlockedLabel.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 0)); // Wrap text
        unlockedLabel.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 0)); // Wrap text
        contentPanel.add(unlockedLabel);

        // Add components to the main panel
        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Initial update
        updateContent();
    }

    /**
     * Updates the displayed content based on the current state.
     */
    public void updateContent() {
        SwingUtilities.invokeLater(() -> { // Ensure UI updates happen on the EDT
            if (state != null && state.isUsernameSet()) {
                String text = "<html>" + state.getFormattedUnlockedContent() + "</html>"; // Use HTML for wrapping
                unlockedLabel.setText(text);
            } else {
                unlockedLabel.setText("Not logged in or state not loaded.");
            }
        });
    }
}

