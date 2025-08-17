package com.nickax.jumppads.util;

import com.nickax.jumppads.pad.JumpPad;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains static variables used throughout the plugin
 */
public class Variable {

    /**
     * List of all jump pads in the plugin
     */
    public static List<JumpPad> jumpPads = new ArrayList<>();

    /**
     * Configuration section containing message strings
     */
    public static ConfigurationSection messages;

    /**
     * Configuration section containing plugin settings
     */
    public static ConfigurationSection settings;

    /**
     * Map tracking which players are currently using a jump pad
     * Key: The player
     * Value: The armor stand entity used for the jump effect
     */
    public static Map<Player, ArmorStand> isJumping = new HashMap<>();
}