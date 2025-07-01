package ru.bloodmine.bloodmineantirelog.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.utility.StringUtility;

@CommandAlias("antirelog|ar|antirelog")
public class HelpCommand extends BaseCommand {

    @Default
    @Subcommand("help")
    @CommandPermission("antirelog.help")
    public void onHelp(CommandSender sender) {
        for (String string : AntiRelog.getInstance().getConfig().getStringList("messages.help")) {
            String replacedString = StringUtility.getMessage(string);
            sender.sendMessage(replacedString);
        }
    }
}