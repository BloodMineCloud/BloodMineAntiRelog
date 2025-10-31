package ru.bloodmine.bloodmineantirelog.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;
import ru.bloodmine.bloodmineantirelog.utility.PlayerUtility;

public class DamageListener implements Listener {

    private final PvPManager pvpManager;

    public DamageListener(PvPManager pvpManager) {
        this.pvpManager = pvpManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player target = (Player) e.getEntity();
        Player damager = PlayerUtility.getDamager(e.getDamager());

        if (damager == null) return;
        if (damager == target) return;

        pvpManager.addPlayer(target);
        pvpManager.addPlayer(damager);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCombust(EntityCombustByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player target = (Player) e.getEntity();

        if (!(e.getCombuster() instanceof Player)) return;
        Player damager = (Player) e.getCombuster();

        if (damager == target) return;

        pvpManager.addPlayer(target);
        pvpManager.addPlayer(damager);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        if (!(e.getPotion().getShooter() instanceof Player)) return;

        Player damager = (Player) e.getPotion().getShooter();

        for (LivingEntity target : e.getAffectedEntities()) {
            if (target == damager) return;

            for (PotionEffect effect : e.getPotion().getEffects()) {
                if (effect.getType().equals(PotionEffectType.POISON) || effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
                    pvpManager.addPlayer(damager);
                    pvpManager.addPlayer((Player) target);
                    return;
                }
            }
        }
    }
}
