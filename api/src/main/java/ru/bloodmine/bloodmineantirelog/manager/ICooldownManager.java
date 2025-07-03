package ru.bloodmine.bloodmineantirelog.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.bloodmine.bloodmineantirelog.data.ICooldownData;

public interface ICooldownManager {
    void addPlayer(Player player, Material material);

    void addPlayer(Player player, String cooldownId);

    ICooldownData getCooldownData(Player player, String cooldownId);

    @Nullable
    ICooldownData getCooldownData(Player player, Material material);

    void removePlayer(Player player);

    void clearMap();
}
