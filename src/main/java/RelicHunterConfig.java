// 2. RelicHunterConfig.java (Configuration Interface)
package net.runelite.client.plugins.relichunterpoc;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("relichunterpoc")
public interface RelicHunterConfig extends Config {

    // Example config item - not used in current logic but shows how
    @Range(
            min = 0,
            max = 100
    )
    @ConfigItem(
            keyName = "copperRelicChance",
            name = "Copper Relic Chance (%)",
            description = "The percentage chance to get a Skilling Relic from mining copper.",
            position = 1
    )
    default int copperRelicChance() {
        return 50; // Default to 50%
    }

    @ConfigItem(
            keyName = "showDebugMessages",
            name = "Show Debug Messages",
            description = "Log debug messages to the console for troubleshooting.",
            position = 2
    )
    default boolean showDebugMessages() {
        return false;
    }
}

