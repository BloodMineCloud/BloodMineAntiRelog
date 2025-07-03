package ru.bloodmine.bloodmineantirelog.listener;

import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.data.CooldownData;
import ru.bloodmine.bloodmineantirelog.manager.CooldownManager;
import ru.bloodmine.bloodmineantirelog.manager.PlayerMessageManager;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;

import java.time.Duration;
import java.time.LocalTime;

import static org.bukkit.Material.*;

public class CooldownListener implements Listener {

    private static final String HEALTH_POTION_NAME = "HEALTH_POTION";

    private final CooldownManager cooldownManager;
    private final PvPManager pvpManager;

    public CooldownListener(CooldownManager cooldownManager, PvPManager pvpManager) {
        this.cooldownManager = cooldownManager;
        this.pvpManager = pvpManager;
    }

    @EventHandler
    public void onCrossbowUse(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getBow() == null) {
                return;
            }
            if (event.getBow().getType() == Material.CROSSBOW) {
                Player player = (Player) event.getEntity();
                if (handleCooldown(player, Material.CROSSBOW)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        PlayerTeleportEvent.TeleportCause cause = e.getCause();
        Player player = e.getPlayer();

        if (!pvpManager.isPvP(player))
            return;

        if (cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
                || cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Material material = (cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)
                    ? Material.CHORUS_FRUIT
                    : Material.ENDER_PEARL;

            if (handleCooldown(player, material)) {
                e.setCancelled(true);
            }
        } else if (AntiRelog.getInstance().getConfig().getBoolean("settings.cancel.teleport")) {
            PlayerMessageManager.send("block", player);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTridentThrow(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Trident && event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            if (handleCooldown(player, Material.TRIDENT)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent e) {
        if (e.getEntity() instanceof Player && !e.isCancelled()) {
            if (e.getEntity().getEquipment() != null &&
                    (e.getEntity().getEquipment().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                            || e.getEntity().getEquipment().getItemInOffHand()
                                    .getType() == Material.TOTEM_OF_UNDYING)) {
                Player player = (Player) e.getEntity();

                if (handleCooldown(player, Material.TOTEM_OF_UNDYING)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();

        if (handleCooldown(player, e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireworkLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Firework && e.getEntity().getShooter() instanceof Player player) {
            if (handleCooldown(player, FIREWORK_ROCKET)) {
                e.setCancelled(true);
            }
        }
    }

    private boolean handleCooldown(Player player, ItemStack item) {
        return handleCooldown(player, getCooldownId(item));
    }

    private boolean handleCooldown(Player player, Material material) {
        return handleCooldown(player, material.name());
    }

    private boolean handleCooldown(Player player, String cooldownId) {
        if (!pvpManager.isPvP(player))
            return false;

        CooldownData data = cooldownManager.getCooldownData(player, cooldownId);
        int configTime = getCooldownTime(cooldownId);
        if (cooldownId == null || configTime <= 0)
            return false;

        if (data != null) {
            LocalTime now = LocalTime.now();
            LocalTime cooldown = data.getTime();
            Duration timePassed = Duration.between(cooldown, now);
            long secondsPassed = timePassed.getSeconds();
            long remainingTime = configTime - secondsPassed;

            if (secondsPassed >= configTime) {
                cooldownManager.removePlayer(player);
            } else {
                PlayerMessageManager.send("cooldown", player, message -> message.replace("{time}", String.valueOf(remainingTime)));
                return true;
            }
        } else {
            cooldownManager.addPlayer(player, cooldownId);
            return false;
        }

        return false;
    }

    @Nullable
    private String getCooldownId(ItemStack item) {
        switch (item.getType()) {
            case FIREWORK_ROCKET, GOLDEN_APPLE, ENCHANTED_GOLDEN_APPLE, ENDER_PEARL, CHORUS_FRUIT, CROSSBOW, TRIDENT,
                 TOTEM_OF_UNDYING:
                return item.getType().name();
        }

        if ((item.getType() == Material.POTION) && item.getItemMeta() instanceof PotionMeta potionMeta) {
            PotionData potionData = potionMeta.getBasePotionData();
            if (potionData.getType() == PotionType.INSTANT_HEAL) {
                return HEALTH_POTION_NAME;
            }
        }

        return null;
    }

    private int getCooldownTime(String cooldownId) {
        if (cooldownId == null) return 0;
        Material material = Material.getMaterial(cooldownId);
        if (material != null) {
            switch (material) {
                case FIREWORK_ROCKET:
                    return AntiRelog.getInstance().getConfig().getInt("settings.cooldown.firework");
                case GOLDEN_APPLE:
                    return AntiRelog.getInstance().getConfig().getInt("settings.cooldown.golden-apple");
                case ENCHANTED_GOLDEN_APPLE:
                    return AntiRelog.getInstance().getConfig().getInt("settings.cooldown.enchanted-golden-apple");
                case ENDER_PEARL:
                    return AntiRelog.getInstance().getConfig().getInt("settings.cooldown.ender-pearl");
                case CHORUS_FRUIT:
                    return AntiRelog.getInstance().getConfig().getInt("settings.cooldown.chorus");
                case CROSSBOW:
                    return AntiRelog.getInstance().getConfig().getInt("settings.cooldown.crossbow");
                case TRIDENT:
                    return AntiRelog.getInstance().getConfig().getInt("settings.cooldown.trident");
                case TOTEM_OF_UNDYING:
                    return AntiRelog.getInstance().getConfig().getInt("settings.cooldown.totem");
            }
        }

        if (cooldownId.equals(HEALTH_POTION_NAME)) {
            return AntiRelog.getInstance().getConfig().getInt("settings.cooldown.health-potion");
        }

        return 0;
    }
}
