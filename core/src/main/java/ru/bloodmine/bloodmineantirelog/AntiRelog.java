package ru.bloodmine.bloodmineantirelog;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import ru.bloodmine.bloodmineantirelog.command.HelpCommand;
import ru.bloodmine.bloodmineantirelog.command.ReloadCommand;
import ru.bloodmine.bloodmineantirelog.command.TestCommand;
import ru.bloodmine.bloodmineantirelog.listener.*;
import ru.bloodmine.bloodmineantirelog.manager.CooldownManager;
import ru.bloodmine.bloodmineantirelog.manager.ICooldownManager;
import ru.bloodmine.bloodmineantirelog.manager.IPvPManager;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;

public final class AntiRelog extends JavaPlugin implements AntiRelogPlugin {

    public boolean CMI_HOOK = false;
    public boolean WORLDGUARD_HOOK = false;

    @Getter
    private static AntiRelog instance;
    public PvPManager pvpmanager;
    public CooldownManager cooldownManager;

    @Nullable
    public BukkitTask activeUpdateTask;

    private void info(Object o) {
        getLogger().info(o.toString());
    }

    @Override
    public void onEnable() {
        instance = this;

        info("Plugin has been enabled!");

        loadDepend();
        loadConfig();

        loadListeners();
        loadCommands();

        info("Plugin has been loaded!");

        pvpmanager = new PvPManager(this);
        activeUpdateTask = pvpmanager.startTask();

        info("PvPManager has been started!");

        cooldownManager = new CooldownManager();

        info("Load all managers!");

        Bukkit.getServicesManager().register(AntiRelogPlugin.class, this, this, ServicePriority.Normal);

        info("Register service AntiRelogPlugin!");
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
        commandManager.registerCommand(new ReloadCommand(pvpmanager));
        commandManager.registerCommand(new TestCommand(pvpmanager));
    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new WorldGuardListener(pvpmanager), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(pvpmanager), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(pvpmanager), this);
        Bukkit.getPluginManager().registerEvents(new LeaveListener(pvpmanager), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(pvpmanager), this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(pvpmanager), this);
        Bukkit.getPluginManager().registerEvents(new CooldownListener(cooldownManager, pvpmanager), this);
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

    @Override
    public Plugin getPlugin() {
        return this;
    }

    @Override
    public IPvPManager getPvPManager() {
        return pvpmanager;
    }

    @Override
    public ICooldownManager getCooldownManager() {
        return cooldownManager;
    }
}
