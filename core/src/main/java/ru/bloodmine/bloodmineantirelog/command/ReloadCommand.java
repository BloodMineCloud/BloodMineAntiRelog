package ru.bloodmine.bloodmineantirelog.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;
import ru.bloodmine.bloodmineantirelog.utility.StringUtility;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@CommandAlias("antirelog|ar|antirelog")
public class ReloadCommand extends BaseCommand {

    private final PvPManager pvpManager;

    public ReloadCommand(PvPManager pvpManager) {
        this.pvpManager = pvpManager;
    }

    @Subcommand("reload")
    @CommandPermission("antirelog.reload")
    public void onReload(CommandSender sender) {
        AntiRelog.getInstance().saveDefaultConfig();
        try {
            AntiRelog.getInstance().reloadConfig();
            AntiRelog.getInstance().getConfig().setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(AntiRelog.getInstance().getResource("config.yml"), StandardCharsets.UTF_8)));

            pvpManager.setConfig(AntiRelog.getInstance().getConfig());

            sender.sendMessage(StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.reload")));
        }
        catch (Exception e) {
            AntiRelog.getInstance().getLogger().log(Level.SEVERE, "failed to reload config", e);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cЕБАТЬ! Конфиг умер нахуй блять сосать. Чекай консось уебок!"));
        }
    }
}