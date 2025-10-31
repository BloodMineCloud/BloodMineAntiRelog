package ru.bloodmine.bloodmineantirelog.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.utility.StringUtility;

import java.util.function.Function;

public class PlayerMessageManager {

    @Nullable
    private static String getValue(String key) {
        return AntiRelog.getInstance().getConfig().getString("messages." + key, null);
    }

    private static void sendMessage(String key, Player player, Function<String, String> replacer) {
        String message = getValue(key);
        if (message != null) {
            player.sendMessage(StringUtility.getMessage(replacer.apply(message)));
        }
    }

    private static void sendSubtitle(String key, Player player, Function<String, String> replacer) {
        String message = getValue(key + "-subtitle");
        if (message != null) {
            player.sendTitle("", StringUtility.getMessage(replacer.apply(message)), 6, 40, 6);
        }
    }

    private static void sendActionBar(String key, Player player, Function<String, String> replacer) {
        String message = getValue(key + "-actionbar");
        if (message != null) {
            player.sendActionBar(StringUtility.getMessage(replacer.apply(message)));
        }
    }

    public static void send(String key, Player player, Function<String, String> replacer) {
        sendMessage(key, player, replacer);
        sendSubtitle(key, player, replacer);
        sendActionBar(key, player, replacer);
    }

    public static void send(String key, Player player, Function<String, String> replacer, long remainingTickTime) {
        if (remainingTickTime > 30) {
            sendMessage(key, player, replacer);
        }
        sendSubtitle(key, player, replacer);
        sendActionBar(key, player, replacer);
    }

    public static void send(String key, Player player) {
        send(key, player, Function.identity());
    }
}
