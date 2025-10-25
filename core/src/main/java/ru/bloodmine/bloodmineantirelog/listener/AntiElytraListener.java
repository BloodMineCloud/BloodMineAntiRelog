package ru.bloodmine.bloodmineantirelog.listener;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.bloodmine.bloodmineantirelog.AntiRelog;
import ru.bloodmine.bloodmineantirelog.manager.IPvPManager;

public class AntiElytraListener implements Listener {

    private final IPvPManager pvpManager;

    public AntiElytraListener(IPvPManager pvpManager) {
        this.pvpManager = pvpManager;
    }

    private static boolean isElytraAndCancel(ItemStack item) {
        return AntiRelog.getInstance().getConfig().getBoolean("settings.cancel.elytra") && item != null && item.getType() == Material.ELYTRA;
    }

    /** 1) Запрет надевания ПКМ (правый клик по элитре) */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRightClickEquip(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!pvpManager.isPvP(p))
            return;
        ItemStack item = event.getItem();
        ItemStack hand = p.getEquipment() != null && event.getHand() != null ? p.getEquipment().getItem(event.getHand()) : null;
        if (isElytraAndCancel(item) || isElytraAndCancel(hand)) {
            event.setCancelled(true);
        }
    }

    /** 2) Запрет через клики по инвентарю (вставка в слот брони, шифт-клик, swap по числу и т.п.) */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity who = event.getWhoClicked();
        if (!(who instanceof Player p)) return;
        if (!pvpManager.isPvP(p))
            return;

        // (a) Любая попытка положить элитру непосредственно в слот брони (груди)
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            // Уточняем именно нагрудный слот: в 1.16.5 он совпадает с EquipmentSlot.CHEST
            // Простой способ: смотрим что кладут/меняют
            ItemStack cursor = event.getCursor();
            ItemStack hotbar = null;
            if (event.getClick() == ClickType.NUMBER_KEY) {
                int hotbarButton = event.getHotbarButton();
                if (hotbarButton >= 0) {
                    hotbar = who.getInventory().getItem(hotbarButton);
                }
            }

            boolean placingElytra =
                    isElytraAndCancel(cursor) // кладут курсором
                            || (event.getAction() == InventoryAction.SWAP_WITH_CURSOR && isElytraAndCancel(cursor))
                            || (event.getClick() == ClickType.NUMBER_KEY && isElytraAndCancel(hotbar));

            if (placingElytra) {
                // В ARMOR есть 4 слота. Проверим, что это именно нагрудник.
                // На разных вью слот индексы разные, но у Player Inventory нагрудник — единственный слот,
                // который принимает ELYTRA; достаточно просто запретить ELYTRA в любом ARMOR-слоте:
                event.setCancelled(true);
                return;
            }
        }

        // (b) Shift-клик из инвентаря игрока, который авто-наденет элитру в слот груди
        if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)
                && isElytraAndCancel(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        // (c) Явная попытка положить элитру курсором в любой слот, который может трактоваться как броня (edge-case)
        if (isElytraAndCancel(event.getCursor()) && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }

    /** 3) Запрет перетаскивания (drag) элитры на слот брони */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity who = event.getWhoClicked();
        if (!(who instanceof Player p)) return;
        if (!pvpManager.isPvP(p))
            return;
        if (!isElytraAndCancel(event.getOldCursor())) return;
        // Если среди затронутых слотов есть ARMOR, отменяем
        // Точного API для типа слота в Drag нет, но если тянут по окну,
        // попадание в armor слоты приведёт к надеванию — просто отменяем, если задеты "верхние" слоты вью.
        // Безопаснее — отменить вообще drag элитры по окну:
        event.setCancelled(true);
    }

    /** 4) Запрет надевания через раздатчик */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDispenserEquip(BlockDispenseArmorEvent event) {
        LivingEntity who = event.getTargetEntity();
        if (!(who instanceof Player p)) return;
        if (!pvpManager.isPvP(p))
            return;
        if (!isElytraAndCancel(event.getItem())) return;
        event.setCancelled(true);
    }
}
