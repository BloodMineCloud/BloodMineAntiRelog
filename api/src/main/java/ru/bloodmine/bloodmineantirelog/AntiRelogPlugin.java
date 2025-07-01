package ru.bloodmine.bloodmineantirelog;

import org.bukkit.plugin.Plugin;
import ru.bloodmine.bloodmineantirelog.manager.ICooldownManager;
import ru.bloodmine.bloodmineantirelog.manager.IPvPManager;

public interface AntiRelogPlugin {
    Plugin getPlugin();

    IPvPManager getPvPManager();

    ICooldownManager getCooldownManager();
}
