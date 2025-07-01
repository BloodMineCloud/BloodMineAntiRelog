package me.katze.powerantirelog;

import co.aikar.commands.PaperCommandManager;
import me.katze.powerantirelog.command.HelpCommand;
import me.katze.powerantirelog.command.ReloadCommand;
import me.katze.powerantirelog.command.TestCommand;
import me.katze.powerantirelog.listener.*;
import me.katze.powerantirelog.manager.CooldownManager;
import me.katze.powerantirelog.manager.PvPManager;
import me.katze.powerantirelog.utility.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

public final class AntiRelog extends JavaPlugin {

    public boolean CMI_HOOK = false;
    public boolean WORLDGUARD_HOOK = false;

    public final String VERSION = "1.6";
    public final String CREATOR = "https://github.com/BloodMineCloud/BloodMineAntiRelog";
    public final String TELEGRAM_URL = "https://t.me/thedivazo";

    private static AntiRelog instance;
    public PvPManager pvpmanager;
    public CooldownManager cooldownManager;

    @Nullable
    public BukkitTask activeUpdateTask;

    @Override
    public void onEnable() {
        instance = this;

        System.out.println("Version: " + CREATOR);
        System.out.println("By: " + VERSION);
        System.out.println("Support: " + TELEGRAM_URL);

        loadDepend();
        loadConfig();

        loadListeners();
        loadCommands();

        pvpmanager = new PvPManager();
        activeUpdateTask = pvpmanager.startTask();
        cooldownManager = new CooldownManager();
    }

    @Override
    public void onDisable() {
        if (activeUpdateTask != null) {
            activeUpdateTask.cancel();
        }
    }

    private void loadCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(AntiRelog.getInstance());

        commandManager.registerCommand(new HelpCommand());
        commandManager.registerCommand(new ReloadCommand());
        commandManager.registerCommand(new TestCommand());
    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new WorldGuardListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new LeaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
        Bukkit.getPluginManager().registerEvents(new CooldownListener(), this);
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void loadDepend() {
        if (getServer().getPluginManager().getPlugin("CMI") != null) {
            CMI_HOOK = true;
        }
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            WORLDGUARD_HOOK = true;
        }
    }

    public static AntiRelog getInstance() {
        return instance;
    }
}
