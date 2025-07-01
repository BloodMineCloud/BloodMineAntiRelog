package ru.bloodmine.bloodmineantirelog.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;
import ru.bloodmine.bloodmineantirelog.utility.StringUtility;

public class CommandListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();

        if (!PvPManager.isPvP(player)) return;
        if (!AntiRelog.getInstance().getConfig().getBoolean("settings.cancel.command")) return;

        String command = e.getMessage().split(" ")[0].replaceFirst("/", "");

        if (isWhitelistCommand(command)) {
            return;
        }

        e.setCancelled(true);
        player.sendMessage(StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.block")));
    }

    private static boolean isWhitelistCommand(String command) {
        for (String string : AntiRelog.getInstance().getConfig().getStringList("settings.command-whitelist")) {
            if (string.contains(command)) {
                return true;
            }
        }
        return false;
    }
}
