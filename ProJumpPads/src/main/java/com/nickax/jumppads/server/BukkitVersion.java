package com.nickax.jumppads.server;

import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;

/**
 * Enum representing different Minecraft server versions
 */
public enum BukkitVersion {

    /**
     * Minecraft 1.21
     */
    V1_21("1.21"),

    /**
     * Minecraft 1.21.1
     */
    V1_21_1("1.21.1"),

    /**
     * Minecraft 1.21.3
     */
    V1_21_3("1.21.3"),

    /**
     * Minecraft 1.21.4
     */
    V1_21_4("1.21.4"),

    /**
     * Minecraft 1.21.5
     */
    V1_21_5("1.21.5"),

    /**
     * Minecraft 1.21.6
     */
    V1_21_6("1.21.6"),

    /**
     * Minecraft 1.21.7
     */
    V1_21_7("1.21.7"),

    /**
     * Minecraft 1.21.8
     */
    V1_21_8("1.21.8"),

    /**
     * Unknown version
     */
    UNKNOWN(null);

    private static final List<BukkitVersion> DETECTABLES = Arrays.stream(BukkitVersion.values())
            .filter(version -> version.version != null)
            .sorted((a, b) -> b.ordinal() - a.ordinal())
            .toList();
    private static volatile BukkitVersion current;
    private final String version;

    /**
     * Constructs a ServerVersion enum with the specified version string
     *
     * @param version The version string
     */
    BukkitVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the current server version by checking the server version string
     *
     * @return The detected ServerVersion enum value
     */
    public static BukkitVersion current() {
        BukkitVersion cached = current;
        if (cached != null) {
            return cached;
        }
        synchronized (BukkitVersion.class) {
            if (current == null) {
                String version = Bukkit.getVersion();
                current = DETECTABLES.stream().filter(v -> version.contains(v.version)).findFirst().orElse(UNKNOWN);
            }
            return current;
        }
    }

    /**
     * Checks if this server version is at least the specified version
     *
     * @param version The version to check against
     * @return true if this version is equal to or newer than the specified version
     */
    public boolean isAtLeast(BukkitVersion version) {
        return this.ordinal() >= version.ordinal();
    }
}