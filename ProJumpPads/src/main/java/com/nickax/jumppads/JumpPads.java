package com.nickax.jumppads;

import com.nickax.jumppads.command.JumpPadCommand;
import com.nickax.jumppads.listener.JumpPadListener;
import com.nickax.jumppads.hook.Placeholders;
import com.nickax.jumppads.pad.JumpPad;
import com.nickax.jumppads.util.Variable;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main plugin class for JumpPads
 */
public class JumpPads extends JavaPlugin {

    /**
     * Called when the plugin is enabled
     * Loads configuration, jump pads, hooks, commands and listeners
     */
    @Override
    public void onEnable() {
        loadConfig();
        loadJumpPads();
        loadHooks();
        loadCommands();
        registerListeners();
        getLogger().info("Loaded " + Variable.jumpPads.size() + " jump pads successfully!");
    }

    /**
     * Called when the plugin is disabled
     * Saves all jump pads
     */
    @Override
    public void onDisable() {
        saveJumpPads();
    }

    /**
     * Reloads the plugin by saving and reloading jump pads
     */
    public void reload() {
        saveJumpPads();
        loadJumpPads();
    }

    /**
     * Loads plugin configuration from config.yml
     * Initializes messages and settings variables
     */
    private void loadConfig() {
        saveDefaultConfig();
        Variable.messages = getConfig().getConfigurationSection("messages");
        Variable.settings = getConfig().getConfigurationSection("settings");
    }

    /**
     * Loads PlaceholderAPI hook if the plugin is present
     */
    private void loadHooks() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
            getLogger().info("Hooked into PlaceholderAPI!");
        }
    }

    /**
     * Loads all jump pad configurations from files
     * Jump pads are stored in the plugin's List directory
     */
    private void loadJumpPads() {
        Variable.jumpPads = new ArrayList<>();

        File folder = new File(getDataFolder() + File.separator + "List");
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) return;

        List<JumpPad> pads = new ArrayList<>();
        Arrays.stream(files).filter(file -> file.getName().endsWith(".yml")).forEach(file -> pads.add(new JumpPad(this, file)));

        Variable.jumpPads = pads;
    }

    /**
     * Registers the main plugin command executor
     */
    private void loadCommands() {
        PluginCommand command = getCommand("jumppads");
        if (command == null) return;
        command.setExecutor(new JumpPadCommand(this));
    }

    /**
     * Registers event listeners for the plugin
     */
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new JumpPadListener(this), this);
    }

    /**
     * Saves all jump pads
     * Called on plugin disable and reload
     */
    private void saveJumpPads() {
        Variable.jumpPads.forEach(pad -> pad.delete(true));
    }
}