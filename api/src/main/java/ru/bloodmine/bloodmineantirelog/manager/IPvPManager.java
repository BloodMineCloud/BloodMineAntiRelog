package ru.bloodmine.bloodmineantirelog.manager;

import org.bukkit.entity.Player;

public interface IPvPManager {
    void addPlayer(Player player);

    void removeFromMaps(Player player);

    boolean isPvP(Player player);

    void leave(Player player);

    void disable(Player player);

    void death(Player player);
}
