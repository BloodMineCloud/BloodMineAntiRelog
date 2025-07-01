package ru.bloodmine.bloodmineantirelog.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;

public class InteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!PvPManager.isPvP(e.getPlayer()))
            return;

        Block block = e.getClickedBlock();

        if (block == null) {
            return;
        }

        if (block.getType() == Material.ENDER_CHEST) {

            if (AntiRelog.getInstance().getConfig().getBoolean("settings.cancel.ender-chest")) {
                // e.getPlayer().sendMessage(
                // StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.block")));
                e.setCancelled(true);
            }
        } else if (AntiRelog.getInstance().getConfig().getBoolean("settings.cancel.interact")) {
            // e.getPlayer().sendMessage(
            // StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.block")));
            e.setCancelled(true);
        }
    }
}
