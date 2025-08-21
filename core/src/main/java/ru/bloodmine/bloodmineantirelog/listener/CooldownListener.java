package ru.bloodmine.bloodmineantirelog.listener;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
import ru.bloodmine.bloodmineantirelog.utility.ItemNameRegistry;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;

import static org.bukkit.Material.*;

public class CooldownListener implements Listener {

    private final Set<Material> launchMaterials = Set.of(ENDER_PEARL, TRIDENT);

    private final Set<PlayerTeleportEvent.TeleportCause> teleportCauses = Set.of(
            PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT,
            PlayerTeleportEvent.TeleportCause.ENDER_PEARL
    );

    private final Set<Material> consumerCooldown = Set.of(GOLDEN_APPLE, ENCHANTED_GOLDEN_APPLE, CHORUS_FRUIT, POTION);

    private final ItemNameRegistry itemNameRegistry = ItemNameRegistry.builder()
            .registry("golden-apple", Material.GOLDEN_APPLE)
            .registry("enchanted-golden-apple", Material.ENCHANTED_GOLDEN_APPLE)
            .registry("ender-pearl", Material.ENDER_PEARL)
            .registry("chorus", Material.CHORUS_FRUIT)
            .registry("firework", Material.FIREWORK_ROCKET)
            .registry("totem", Material.TOTEM_OF_UNDYING)
            .registry("crossbow", Material.CROSSBOW)
            .registry("health-potion", POTION, (itemStack) -> {
                if ((itemStack.getType() == Material.POTION) && itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
                    PotionData potionData = potionMeta.getBasePotionData();
                    return potionData.getType() == PotionType.INSTANT_HEAL;
                }
                return false;
            })
            .build();

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
            if (itemNameRegistry.hasItem(event.getBow())) {
                Player player = (Player) event.getEntity();
                if (handleCooldown(player, event.getBow())) {
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

        if (!teleportCauses.contains(cause)) {
            if (AntiRelog.getInstance().getConfig().getBoolean("settings.cancel.teleport")) {
                PlayerMessageManager.send("block", player);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent e) {
        if (e.getEntity() instanceof Player && !e.isCancelled()) {
            if (e.getEntity().getEquipment() != null &&
                    (e.getEntity().getEquipment().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                            || e.getEntity().getEquipment().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING)) {
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
        if (consumerCooldown.contains(e.getItem().getType()) && handleCooldown(player, e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectile(PlayerLaunchProjectileEvent event) {
        boolean bool = launchMaterials.contains(event.getItemStack().getType()) && handleCooldown(event.getPlayer(), event.getItemStack());
        if (bool) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getItem() == null) return;
        if (!consumerCooldown.contains(e.getMaterial())
                && !launchMaterials.contains(e.getMaterial())
                && handleCooldown(player, e.getItem())) {
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

    public List<String> getMaterialCooldownList() {
        return AntiRelog.getInstance().getConfig().getStringList("settings.material-cooldown");
    }

    private boolean handleCooldown(Player player, ItemStack item) {
        return handleCooldown(player, itemNameRegistry.getItemName(item));
    }

    private boolean handleCooldown(Player player, Material material) {
        return handleCooldown(player, itemNameRegistry.getItemName(material));
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
                setCooldown(player, cooldownId);
                return false;
            } else {
                PlayerMessageManager.send("cooldown", player, message -> message.replace("{time}", String.valueOf(remainingTime)));
                return true;
            }
        } else {
            setCooldown(player, cooldownId);
            return false;
        }
    }

    private void setCooldown(Player player, String cooldownId) {
        handleMaterialCooldown(player, cooldownId);
        cooldownManager.addPlayer(player, cooldownId);
    }

    private void handleMaterialCooldown(Player player, String configName) {
        if (!getMaterialCooldownList().contains(configName)) return;
        Material material = itemNameRegistry.getCooldownMaterial(configName);
        int configTime = getCooldownTime(configName);
        if (configTime > 0 && material != null) {
            Bukkit.getScheduler().runTaskLater(AntiRelog.getInstance(), ()-> player.setCooldown(material, configTime*20-1), 1);
            //player.setCooldown(material, configTime*20);
        }
    }

    @Nullable
    private String getCooldownId(ItemStack item) {
        return itemNameRegistry.getItemName(item);
    }

    private int getCooldownTime(String cooldownId) {
        if (cooldownId == null) return 0;
        return AntiRelog.getInstance().getConfig().getInt("settings.cooldown." + cooldownId, 0);
    }
}
