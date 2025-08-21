package ru.bloodmine.bloodmineantirelog.manager;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.utility.BossBarUtility;
import ru.bloodmine.bloodmineantirelog.utility.StringUtility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PvPManager implements IPvPManager {
    private HashMap<String, Integer> pvpMap;

    @Getter
    @Setter
    private FileConfiguration config;

    private final CooldownManager cooldownManager;

    private final AntiRelog antiRelog;


    public PvPManager(AntiRelog antiRelog) {
        this.cooldownManager = antiRelog.cooldownManager;
        this.antiRelog = antiRelog;
        this.pvpMap = new HashMap<>();
        this.config = antiRelog.getConfig();
    }

    @Override
    public void addPlayer(Player player) {
        if (player == null)
            return;
        if (player.hasPermission("antirelog.bypass"))
            return;
        if (player.isOp())
            return;
        if (player.isInvulnerable())
            return;
        if (player.isDead())
            return;

        String name = player.getName();
        int time = config.getInt("settings.time");

        if (pvpMap.containsKey(name)) {
            pvpMap.put(name, time);
        } else {
            PlayerMessageManager.send("start", player);
            sendCommands(true, player);
            disable(player);
            pvpMap.put(name, time);
        }
    }

    @Override
    public void removeFromMaps(Player player) {
        pvpMap.remove(player.getName());

        cooldownManager.removePlayer(player);
    }

    @Override
    public boolean isPvP(Player player) {
        String name = player.getName();

        if (pvpMap.containsKey(name)) {
            return true;
        }
        return false;
    }

    @Override
    public void leave(Player player) {
        if (config.getBoolean("settings.leave.kill")) {
            player.damage(player.getHealth());
            player.setHealth(0);
        }

        for (String string : config.getStringList("settings.leave.message")) {
            String replacedString = StringUtility.getMessage(string).replace("{player}", player.getName());
            Bukkit.getServer().broadcastMessage(replacedString);
        }

        removeFromMaps(player);
    }

    @Override
    public void disable(Player player) {
        if (config.getBoolean("settings.disable.fly")) {
            player.setFlying(false);
            player.setAllowFlight(false);
//            if (antiRelog.CMI_HOOK) {
//                CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
//
//                if (user != null) {
//                    user.setFlying(false);
//                    user.setWasFlying(false);
//                    user.setTfly(0L);
//                }
//            }
        }

        if (config.getBoolean("settings.disable.speed")) {
            player.setWalkSpeed(0.2F);
        }

        if (config.getBoolean("settings.disable.gamemode")) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        if (config.getBoolean("settings.disable.invisibility")) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }

        if (config.getBoolean("settings.disable.elytra")
                && player.getInventory().getChestplate() != null
                && player.getInventory().getChestplate().getType() == Material.ELYTRA) {

            ItemStack elytra = player.getInventory().getChestplate().clone();
            Map<Integer, ItemStack> savedMap = player.getInventory().addItem(elytra);
            if (savedMap.isEmpty()) {
                player.getInventory().setChestplate(null);
            }
        }

        if (config.getBoolean("settings.disable.godmode")) {
            if (antiRelog.CMI_HOOK) {
//                CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
//
//                if (user != null) {
//                    CMI.getInstance().getNMS().changeGodMode(player, false);
//                    user.setTgod(0L);
//                }
            }
        }
    }

    public BukkitTask startTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(antiRelog, 0L, 20L);
    }

    private void update() {
        if (pvpMap == null)
            return;

        Iterator<Map.Entry<String, Integer>> iterator = pvpMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String name = entry.getKey();
            int time = entry.getValue() - 1;

            if (time <= 0) {
                iterator.remove();
                Player player = Bukkit.getPlayer(name);
                if (player != null) {
                    PlayerMessageManager.send("end", player);
                    sendCommands(false, player);
                }
            } else {
                pvpMap.replace(name, time);
                BossBarUtility.setTemporarily(name, time);
            }
        }
    }

    @Override
    public void death(Player player) {
        String name = player.getName();

        if (pvpMap.containsKey(name)) {
            pvpMap.remove(name);
        }

        cooldownManager.removePlayer(player);
    }

    private void sendCommands(boolean start, Player player) {
        List<String> commands;
        if (start) {
            commands = config.getStringList("settings.command-start");
        } else {
            commands = config.getStringList("settings.command-end");
        }

        for (String string : commands) {
            CommandSender sender = null;

            if (string.startsWith("[console]")) {
                sender = Bukkit.getConsoleSender();
            } else if (string.startsWith("[player]")) {
                sender = player.getPlayer();
            }

            String replacedString = string.replace("{player}", player.getName())
                    .replace("[player]", "")
                    .replace("[console]", "");
            Bukkit.getServer().dispatchCommand(sender, replacedString);
        }
    }
}
