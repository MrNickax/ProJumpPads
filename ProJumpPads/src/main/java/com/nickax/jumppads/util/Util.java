package com.nickax.jumppads.util;

import com.nickax.jumppads.pad.JumpPad;
import com.nickax.jumppads.server.BukkitVersion;
import fr.mrmicky.fastparticles.ParticleType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class containing static helper methods
 */
public class Util {

    /**
     * Private constructor to prevent instantiation
     */
    private Util() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Plays a sound to a player based on configuration settings
     *
     * @param player The player to play the sound to
     * @param text   The identifier for the sound configuration
     */
    public static void playSound(Player player, String text) {
        if (!player.isOnline()) return;

        ConfigurationSection section = Variable.settings.getConfigurationSection("sounds." + text);
        if (section == null) return;
        if (!section.getBoolean("enabled")) return;

        String name = section.getString("name");
        if (name == null || name.isEmpty()) return;

        Sound sound = BukkitVersion.current().isAtLeast(BukkitVersion.V1_21_3) ? Registry.SOUNDS.match(name) : Sound.valueOf(name);
        if (sound == null) return;

        player.playSound(player, sound, Math.max(section.getInt("volume"), 1), (float) Math.max(section.getDouble("pitch"), 1));
    }

    /**
     * Sends a message to a command sender, either as a single message or a list of messages
     *
     * @param player The command sender to send messages to
     * @param text   The message key to look up
     * @param list   Whether to send it as a list of messages
     */
    public static void sendMessage(CommandSender player, String text, Boolean list) {
        if (list) {
            List<String> messages = Variable.messages.getStringList(text);
            messages.stream().map(Util::color).forEach(player::sendMessage);
        } else {
            String message = Variable.messages.getString(text);
            player.sendMessage(color(message));
        }
    }

    /**
     * Converts color codes and hex colors in text to actual colors
     *
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String color(String text) {
        if (text == null || text.isEmpty()) return "";

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String hex = text.substring(matcher.start(), matcher.end());
            text = text.replace(hex, ChatColor.of(hex).toString());
            matcher = pattern.matcher(text);
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Shows particles at a location based on configuration settings
     *
     * @param type     The particle configuration type
     * @param location The location to spawn particles at
     */
    public static void showParticle(String type, Location location) {
        ConfigurationSection section = Variable.settings.getConfigurationSection("particles." + type);
        if (section == null || !section.getBoolean("enabled")) return;

        String name = section.getString("name");
        if (name == null || name.isEmpty()) return;

        ParticleType effect = ParticleType.of(name);

        effect.spawn(
                location.getWorld(),
                location,
                section.getInt("amount"),
                section.getDouble("x-offset"),
                section.getDouble("y-offset"),
                section.getDouble("z-offset"),
                section.getDouble("speed")
        );
    }

    /**
     * Lists all jump pads to a command sender
     *
     * @param sender The command sender to show the list to
     */
    public static void getList(CommandSender sender) {
        int count = 1;
        if (Variable.jumpPads.isEmpty()) {
            sendMessage(sender, "no-jump-pad", false);
        } else {
            for (JumpPad pad : Variable.jumpPads) {
                sender.sendMessage(
                        color(
                                Variable.messages.getString("list", "&a%count%&8- &a%name%")
                                        .replace("%count%", String.valueOf(count))
                                        .replace("%name%", pad.getName())
                        )
                );
                count++;
            }
        }
    }
}