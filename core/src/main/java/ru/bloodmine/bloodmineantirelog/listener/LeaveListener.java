package ru.bloodmine.bloodmineantirelog.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;

public class LeaveListener implements Listener {

    private final PvPManager pvpManager;

    public LeaveListener(PvPManager pvpManager) {
        this.pvpManager = pvpManager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        if (!pvpManager.isPvP(e.getPlayer())) {
            pvpManager.removeFromMaps(e.getPlayer());
            return;
        }

        pvpManager.leave(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        if (!pvpManager.isPvP(e.getPlayer())) {
            pvpManager.removeFromMaps(e.getPlayer());
            return;
        }

        pvpManager.leave(e.getPlayer());
    }
}
