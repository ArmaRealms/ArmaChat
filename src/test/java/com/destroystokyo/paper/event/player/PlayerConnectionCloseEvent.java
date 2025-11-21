package com.destroystokyo.paper.event.player;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

// Minimal stub to satisfy MockBukkit lookups for Paper-specific event type during tests.
public class PlayerConnectionCloseEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public PlayerConnectionCloseEvent() {
        // no-op
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

