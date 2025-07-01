package ru.bloodmine.bloodmineantirelog.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.bloodmine.bloodmineantirelog.data.ICooldownData;

public interface ICooldownManager {
    void addPlayer(Player player, Material material);

    ICooldownData getCooldownData(Player player, Material material);

    void removePlayer(Player player);

    void clearMap();
}
