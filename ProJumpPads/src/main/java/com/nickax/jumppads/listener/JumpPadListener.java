package com.nickax.jumppads.listener;

import com.nickax.jumppads.JumpPads;
import com.nickax.jumppads.util.Variable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Listener class that handles events related to jump pad functionality
 */
public class JumpPadListener implements Listener {

    private final JumpPads plugin;

    /**
     * Constructor for JumpPadListener
     *
     * @param plugin The main plugin instance
     */
    public JumpPadListener(JumpPads plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles when a player dismounts from an entity while using a jump pad
     * Ensures the player stays mounted on the entity
     *
     * @param e The EntityDismountEvent
     */
    @EventHandler
    public void onEntityDismount(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!Variable.isJumping.containsKey(player)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.eject();
            e.getDismounted().addPassenger(player);
        }, 1L);
    }

    /**
     * Handles player teleportation while using a jump pad
     * Removes the player from the jumping list if they teleport too far
     *
     * @param e The PlayerTeleportEvent
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if (from.getWorld() == null || to == null || to.getWorld() == null) return;
        if (!from.getWorld().equals(to.getWorld()) || from.distance(to) > 10.0D) {
            Variable.isJumping.remove(e.getPlayer());
        }
    }

    /**
     * Cancels damage taken by players while using a jump pad
     *
     * @param e The EntityDamageEvent
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!Variable.isJumping.containsKey(player)) return;
        e.setCancelled(true);
    }
}