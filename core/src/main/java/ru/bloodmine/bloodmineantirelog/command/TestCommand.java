package ru.bloodmine.bloodmineantirelog.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.manager.PvPManager;
import ru.bloodmine.bloodmineantirelog.utility.StringUtility;

@CommandAlias("antirelog|ar")
public class TestCommand extends BaseCommand {

    private final PvPManager pvpManager;

    public TestCommand(PvPManager pvpManager) {
        this.pvpManager = pvpManager;
    }

    @Subcommand("test")
    @CommandCompletion("@players")
    @CommandPermission("antirelog.test")
    public void onTest(CommandSender sender, String target) {
        Player player = Bukkit.getPlayer(target);

        if (player == null) {
            sender.sendMessage(
                    StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.not-found"))
                            .replace("{player}", target));
            return;
        }

        pvpManager.addPlayer(player);

        sender.sendMessage(StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.test"))
                .replace("{player}", target));
    }
}