package ru.bloodmine.bloodmineantirelog.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.codemc.worldguardwrapper.event.WrappedDisallowedPVPEvent;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;

public class WorldGuardListener implements Listener {

    private final PvPManager pvpManager;

    public WorldGuardListener(PvPManager pvpManager) {
        this.pvpManager = pvpManager;
    }

    @EventHandler
    public void onPvP(WrappedDisallowedPVPEvent e) {
        if (!AntiRelog.getInstance().WORLDGUARD_HOOK) return;

        Player attacker = e.getAttacker();
        Player defender = e.getDefender();

        if (pvpManager.isPvP(attacker.getPlayer()) && pvpManager.isPvP(defender.getPlayer())) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
    }
}
