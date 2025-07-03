package ru.bloodmine.bloodmineantirelog.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.bloodmine.bloodmineantirelog.data.CooldownData;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CooldownManager implements ICooldownManager {
    private HashMap<String, List<CooldownData>> cooldownMap;

    public CooldownManager() {
        this.cooldownMap = new HashMap<>();
    }

    @Override
    public void addPlayer(Player player, Material material) {
        addPlayer(player, material.name());
    }

    public void addPlayer(Player player, String cooldownId) {
        if (cooldownId == null) return;

        CooldownData data = new CooldownData(LocalTime.now(), cooldownId);

        if (cooldownMap.containsKey(player.getName())) {
            List<CooldownData> arrayList = cooldownMap.get(player.getName());

            arrayList.add(data);

            cooldownMap.put(player.getName(), arrayList);
        } else {
            List<CooldownData> arrayList = new ArrayList<>();

            arrayList.add(data);

            cooldownMap.put(player.getName(), arrayList);
        }
    }

    @Nullable
    @Override
    public CooldownData getCooldownData(Player player, Material material) {
        return getCooldownData(player, material.name());
    }

    @Nullable
    public CooldownData getCooldownData(Player player, String cooldownId) {
        if (cooldownMap.containsKey(player.getName()) && cooldownId != null) {
            for (CooldownData cooldownData : cooldownMap.get(player.getName())) {
                if (cooldownData.getItem().equals(cooldownId)) {
                    return cooldownData;
                }
            }
        }
        return null;
    }

    @Override
    public void removePlayer(Player player) {
        cooldownMap.remove(player.getName());
    }

    @Override
    public void clearMap() {
        cooldownMap.clear();
    }
}
