package dev.skynest.xyz.listeners;

import dev.skynest.xyz.container.DatabaseContainer;
import dev.skynest.xyz.interfaces.IData;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

@RequiredArgsConstructor
public class AsyncJoinEvent implements Listener {

    private final DatabaseContainer<? extends IData> databaseContainer;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(AsyncPlayerPreLoginEvent event) {
        if(!databaseContainer.isLoaded())
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "The database has not been loaded yet");
    }

}
