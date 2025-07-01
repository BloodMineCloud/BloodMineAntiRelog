package ru.bloodmine.bloodmineantirelog.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity().getPlayer();

        if (!PvPManager.isPvP(player))
            return;

        PvPManager.death(player);
    }
}
