package com.nickax.jumppads.command;

import com.nickax.jumppads.JumpPads;
import com.nickax.jumppads.api.JumpPadsAPI;
import com.nickax.jumppads.pad.JumpPad;
import com.nickax.jumppads.util.Util;
import com.nickax.jumppads.util.Variable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Command executor for handling all jump pad related commands
 */
@SuppressWarnings("ConstantConditions")
public class JumpPadCommand implements CommandExecutor {

    private final JumpPads plugin;

    /**
     * Creates a new jump pad command executor
     *
     * @param plugin The plugin instance
     */
    public JumpPadCommand(JumpPads plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the jump pad command
     *
     * @param commandSender The command sender
     * @param command       The command being executed
     * @param label         The command label
     * @param args          The command arguments
     * @return true if command was successful, false otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!Variable.settings.getString("permission").isEmpty()) {
            if (!commandSender.hasPermission(Variable.settings.getString("permission"))) {
                Util.sendMessage(commandSender, "no-permission", false);
                return false;
            }
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                Variable.settings = plugin.getConfig().getConfigurationSection("settings");
                Variable.messages = plugin.getConfig().getConfigurationSection("messages");
                plugin.reload();
                Util.sendMessage(commandSender, "reloaded", false);
                return true;
            }

            if (args[0].equalsIgnoreCase("list")) {
                Util.getList(commandSender);
                return true;
            }

            if (!(commandSender instanceof Player player)) {
                Util.sendMessage(commandSender, "not-player", false);
                return false;
            }

            if (args[0].equalsIgnoreCase("guide")) {
                Util.sendMessage(commandSender, "guide", true);
                return true;
            }

            if (args.length < 2) {
                Util.sendMessage(commandSender, "player-usage", true);
                return false;
            }

            if (args[0].equalsIgnoreCase("create")) {
                new JumpPad(plugin, player, args[1]);
                return true;
            }

            JumpPad pad = JumpPadsAPI.getByName(args[1]);
            if (pad == null) {
                Util.sendMessage(commandSender, "wrong-pad", false);
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "tp":
                case "teleport":
                    Location location = pad.getCenter();
                    if (location == null) {
                        Util.sendMessage(commandSender, "fly-location-not-set", false);
                        return false;
                    }
                    player.teleport(location.clone().add(0, 10, 0));

                    Util.sendMessage(commandSender, "teleported", false);
                    return true;
                case "view":
                case "info":
                    pad.getInfo(player);
                    return true;
                case "pos-1":
                case "pos-2":
                case "pos1":
                case "pos2":
                    Set<Material> nullSet = null;
                    Block block = player.getTargetBlock(nullSet, 5);
                    if (block == null || block.getType().equals(Material.AIR)) {
                        Util.sendMessage(commandSender, "look-to-correct-block", false);
                        return false;
                    }

                    String name = block.getType().name();
                    if (!Variable.settings.getString("block", "SLIME_BLOCK").equalsIgnoreCase(name)) {
                        Util.sendMessage(commandSender, "look-to-correct-block", false);
                        return false;
                    }

                    if (args[0].equalsIgnoreCase("pos1"))
                        pad.setPos1(block.getLocation());
                    else
                        pad.setPos2(block.getLocation());
                    Util.sendMessage(commandSender, args[0].toLowerCase() + "-set", false);
                    return true;
                case "set-fly-location":
                case "flylocation":
                case "setflylocation":
                    pad.setTargetLocation(player.getLocation());
                    Util.sendMessage(commandSender, "fly-location-set", false);
                    return true;
                case "delete":
                case "remove":
                    pad.delete(false);
                    pad.getFile().delete();
                    Variable.jumpPads.remove(pad);

                    Util.sendMessage(commandSender, "deleted", false);
                    return true;
            }

            if (args.length < 3) {
                Util.sendMessage(commandSender, "player-usage", true);
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "add-command":
                case "addcommand":
                    Util.sendMessage(commandSender, pad.addCommand(getMessage(args.clone())) ? "added-command" : "already-added", false);
                    return true;
                case "remove-command":
                case "removecommand":
                    Util.sendMessage(commandSender, pad.removeCommand(getMessage(args.clone())) ? "removed-command" : "not-added", false);
                    return true;
                case "setpermission-1":
                case "setpermission1":
                case "set-permission-1":
                case "setpermission2":
                case "setpermission-2":
                case "set-permission-2":
                    pad.setPermission(args[0].contains("1") ? 1 : 2, args[2]);

                    Util.sendMessage(commandSender, "permission-set", false);
                    return true;
                case "permissionmessage":
                case "permission-message":
                case "setpermissionmessage":
                case "set-permission-message":
                    pad.setPermissionMessage(getMessage(args.clone()));
                    Util.sendMessage(commandSender, "permission-message-set", false);
                    return true;
            }
        }

        if (commandSender instanceof ConsoleCommandSender)
            Util.sendMessage(commandSender, "console-usage", true);
        else
            Util.sendMessage(commandSender, "player-usage", true);
        return false;
    }

    /**
     * Extracts the message text from command arguments by removing the first two args
     *
     * @param args The command arguments
     * @return The message text
     */
    public String getMessage(String[] args) {
        List<String> newArgs = Arrays.asList(args);
        StringBuilder message = new StringBuilder();

        for (String text : newArgs) {
            if (args[0].equalsIgnoreCase(text)) continue;
            if (args[1].equalsIgnoreCase(text)) continue;

            if (newArgs.getLast().equalsIgnoreCase(text)) message.append(text);
            else message.append(text).append(" ");
        }

        return message.toString();
    }
}