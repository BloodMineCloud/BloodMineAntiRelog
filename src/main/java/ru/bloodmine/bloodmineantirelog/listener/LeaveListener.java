package ru.bloodmine.bloodmineantirelog.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;

public class LeaveListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        if (!PvPManager.isPvP(e.getPlayer())) {
            PvPManager.removeFromMaps(e.getPlayer());
            return;
        }

        PvPManager.leave(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        if (!PvPManager.isPvP(e.getPlayer())) {
            PvPManager.removeFromMaps(e.getPlayer());
            return;
        }

        PvPManager.leave(e.getPlayer());
    }
}
