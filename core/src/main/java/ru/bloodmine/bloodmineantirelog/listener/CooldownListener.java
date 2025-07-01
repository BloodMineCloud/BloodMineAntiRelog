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
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.data.CooldownData;
import ru.bloodmine.bloodmineantirelog.manager.CooldownManager;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;
import ru.bloodmine.bloodmineantirelog.utility.StringUtility;

import java.time.Duration;
import java.time.LocalTime;

public class CooldownListener implements Listener {

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
                if (material.name().equalsIgnoreCase("ENDER_PEARL")) {
                    e.setCancelled(true);
                } else if (material.name().equalsIgnoreCase("chorus_fruit")) {
                    e.setCancelled(true);
                }

                return;
            }
        } else if (AntiRelog.getInstance().getConfig().getBoolean("settings.cancel.teleport")) {
            e.getPlayer().sendMessage(
                    StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.block")));
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

        if (handleCooldown(player, e.getItem().getType())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireworkLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Firework && e.getEntity().getShooter() instanceof Player) {
            Player player = (Player) e.getEntity().getShooter();

            if (handleCooldown(player, Material.FIREWORK_ROCKET)) {
                e.setCancelled(true);
            }
        }
    }

    private boolean handleCooldown(Player player, Material material) {
        if (!pvpManager.isPvP(player))
            return false;

        CooldownData data = cooldownManager.getCooldownData(player, material);
        int configTime = getCooldownTime(material);
        if (configTime <= 0)
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
                String message = AntiRelog.getInstance().getConfig().getString("messages.cooldown")
                        .replace("{time}", String.valueOf(remainingTime));
                player.sendMessage(StringUtility.getMessage(message));

                String subtitle = AntiRelog.getInstance().getConfig().getString("messages.cooldown-subtitle")
                        .replace("{time}", String.valueOf(remainingTime));
                player.sendTitle("", StringUtility.getMessage(subtitle), 6, 40, 6);
                return true;
            }
        } else {
            cooldownManager.addPlayer(player, material);
            return false;
        }

        return false;
    }

    private int getCooldownTime(Material material) {
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
            default:
                return 0;
        }
    }
}
