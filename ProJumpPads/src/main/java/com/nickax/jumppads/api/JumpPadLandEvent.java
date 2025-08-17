package com.nickax.jumppads.api;

import com.nickax.jumppads.pad.JumpPad;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is called when a player lands after using a JumpPad
 */
public class JumpPadLandEvent extends Event implements Cancellable {

    private final Player player;
    private final JumpPad jumpPad;
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    /**
     * Creates a new JumpPadLandEvent
     *
     * @param player  The player who landed
     * @param jumpPad The JumpPad that was used
     */
    public JumpPadLandEvent(Player player, JumpPad jumpPad) {
        this.player = player;
        this.jumpPad = jumpPad;
    }

    /**
     * Gets the list of handlers handling this event
     *
     * @return The list of handlers
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the static list of handlers handling this event
     *
     * @return The static list of handlers
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the JumpPad that was used
     *
     * @return The JumpPad
     */
    public JumpPad getJumpPad() {
        return jumpPad;
    }

    /**
     * Gets the player who landed
     *
     * @return The player
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Gets whether this event is cancelled
     *
     * @return True if cancelled, false otherwise
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets whether this event should be cancelled
     *
     * @param b True to cancel, false otherwise
     */
    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}