package ru.bloodmine.bloodmineantirelog.utility;

import lombok.Getter;
import lombok.Singular;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class ItemNameRegistry {

    private static final class ItemStackPredicateDecorator implements Predicate<ItemStack> {
        @Getter
        private final String name;
        private final Predicate<ItemStack> predicate;
        @Getter
        private final @Nullable Material material;

        public ItemStackPredicateDecorator(String name, Predicate<ItemStack> predicate) {
            this.name = name;
            this.predicate = predicate;
            this.material = null;
        }

        public ItemStackPredicateDecorator(String name, @NotNull Material material) {
            this.name = name;
            this.material = material;
            this.predicate = (item) -> material.equals(item.getType());
        }

        public ItemStackPredicateDecorator(String name, @Nullable Material material, Predicate<ItemStack> predicate) {
            this.name = name;
            this.material = material;
            this.predicate = predicate;
        }

        @Override
        public boolean test(ItemStack t) {
            return predicate.test(t);
        }

    }

    private final Map<String, ItemStackPredicateDecorator> registry;

    private ItemNameRegistry(@Singular Map<String, ItemStackPredicateDecorator> registry) {
        this.registry = registry;
    }

    public boolean hasItem(String configName) {
        return registry.containsKey(configName);
    }

    public boolean hasItem(Material material) {
        return registry.values().stream().anyMatch(predicate -> predicate.test(new ItemStack(material)));
    }

    public boolean hasItem(ItemStack itemStack) {
        return registry.values().stream().anyMatch(predicate -> predicate.test(itemStack));
    }

    public String getItemName(Material material) {
        return getItemName(new ItemStack(material));
    }

    public String getItemName(ItemStack itemStack) {
        return registry.values().stream()
                .filter(predicate -> predicate.test(itemStack))
                .map(ItemStackPredicateDecorator::getName)
                .findAny()
                .orElse(null);
    }

    public Material getCooldownMaterial(String configName) {
        return registry.get(configName).getMaterial();
    }

    public static ItemNameRegistryBuilder builder() {
        return new ItemNameRegistryBuilder();
    }

    public static class ItemNameRegistryBuilder {
        private List<String> registry$key;
        private List<ItemStackPredicateDecorator> registry$value;

        ItemNameRegistryBuilder() {
        }

        public ItemNameRegistryBuilder registry(String name, Material material) {
            if (this.registry$key == null) {
                this.registry$key = new ArrayList<>();
                this.registry$value = new ArrayList<>();
            }
            this.registry$key.add(name);
            this.registry$value.add(new ItemStackPredicateDecorator(name, material));
            return this;
        }

        public ItemNameRegistryBuilder registry(String name, Predicate<ItemStack> predicate) {
            if (this.registry$key == null) {
                this.registry$key = new ArrayList<>();
                this.registry$value = new ArrayList<>();
            }
            this.registry$key.add(name);
            this.registry$value.add(new ItemStackPredicateDecorator(name, predicate));
            return this;
        }

        public ItemNameRegistryBuilder registry(String name, Material material, Predicate<ItemStack> predicate) {
            if (this.registry$key == null) {
                this.registry$key = new ArrayList<>();
                this.registry$value = new ArrayList<>();
            }
            this.registry$key.add(name);
            this.registry$value.add(new ItemStackPredicateDecorator(name, material, predicate));
            return this;
        }

        public ItemNameRegistryBuilder clearRegistry() {
            if (this.registry$key != null) {
                this.registry$key.clear();
                this.registry$value.clear();
            }
            return this;
        }

        public ItemNameRegistry build() {
            Map<String, ItemStackPredicateDecorator> registry;
            switch (this.registry$key == null ? 0 : this.registry$key.size()) {
                case 0:
                    registry = Collections.emptyMap();
                    break;
                case 1:
                    registry = Collections.singletonMap(this.registry$key.get(0), this.registry$value.get(0));
                    break;
                default:
                    registry = new LinkedHashMap<>(this.registry$key.size() < 1073741824 ? 1 + this.registry$key.size() + (this.registry$key.size() - 3) / 3 : Integer.MAX_VALUE);
                    for (int $i = 0; $i < this.registry$key.size(); $i++)
                        registry.put(this.registry$key.get($i), this.registry$value.get($i));
                    registry = Collections.unmodifiableMap(registry);
            }

            return new ItemNameRegistry(registry);
        }

        public String toString() {
            return "ItemNameRegistry.ItemNameRegistryBuilder(registry$key=" + this.registry$key + ", registry$value=" + this.registry$value + ")";
        }
    }
}
