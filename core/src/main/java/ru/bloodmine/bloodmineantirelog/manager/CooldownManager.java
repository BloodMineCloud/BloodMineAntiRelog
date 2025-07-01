package ru.bloodmine.bloodmineantirelog.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
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
        if (material == null) return;

        CooldownData data = new CooldownData(LocalTime.now(), material.name());

        if (cooldownMap.containsKey(player.getName())) {
            List<CooldownData> arrayList = cooldownMap.get(player.getName());

            arrayList.add(data);

            cooldownMap.put(player.getName(), arrayList);
        } else {
            List<CooldownData> arrayList = new ArrayList();

            arrayList.add(data);

            cooldownMap.put(player.getName(), arrayList);
        }
    }

    @Override
    public CooldownData getCooldownData(Player player, Material material) {
        if (cooldownMap.containsKey(player.getName())) {
            for (CooldownData cooldownData : cooldownMap.get(player.getName())) {
                if (cooldownData.getItem().equals(material.name())) {
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
