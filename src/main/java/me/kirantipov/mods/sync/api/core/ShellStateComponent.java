package me.kirantipov.mods.sync.api.core;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class ShellStateComponent {
    public Collection<ItemStack> getItems() {
        return List.of();
    }

    public int getXp() {
        return 0;
    }

    public void clear() { }

    public void clone(ShellStateComponent component) { }

    public void readNbt(NbtCompound nbt) { }

    public NbtCompound writeNbt(NbtCompound nbt) {
        return nbt;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends ShellStateComponent> T as(Class<T> type) {
        return type.isInstance(this) ? (T)this : null;
    }


    public static ShellStateComponent combine() {
        return EmptyShellStateComponent.INSTANCE;
    }

    public static ShellStateComponent combine(ShellStateComponent component) {
        return component;
    }

    public static ShellStateComponent combine(ShellStateComponent... components) {
        return combine(Arrays.asList(components));
    }

    public static ShellStateComponent combine(Collection<ShellStateComponent> components) {
        int size = components.size();
        if (size == 0) {
            return EmptyShellStateComponent.INSTANCE;
        } else if (size == 1) {
            return components.iterator().next();
        } else {
            return new CombinedShellStateComponent(components);
        }
    }


    private static class EmptyShellStateComponent extends ShellStateComponent {
        public static final ShellStateComponent INSTANCE = new EmptyShellStateComponent();

        @Override
        public <T extends ShellStateComponent> @Nullable T as(Class<T> type) {
            return null;
        }
    }

    private static class CombinedShellStateComponent extends ShellStateComponent {
        private final Collection<ShellStateComponent> components;

        public CombinedShellStateComponent(Collection<ShellStateComponent> components) {
            this.components = List.copyOf(components);
        }

        @Override
        public Collection<ItemStack> getItems() {
            List<ItemStack> items = new ArrayList<>();
            for (ShellStateComponent component : components) {
                items.addAll(component.getItems());
            }
            return items;
        }

        @Override
        public int getXp() {
            int xp = 0;
            for (ShellStateComponent component : components) {
                xp += component.getXp();
            }
            return xp;
        }

        @Override
        public void clear() {
            for (ShellStateComponent component : components) {
                component.clear();
            }
        }

        @Override
        public void clone(ShellStateComponent component) {
            for (ShellStateComponent innerComponent : components) {
                innerComponent.clone(component);
            }
        }

        @Override
        @Nullable
        public <T extends ShellStateComponent> T as(Class<T> type) {
            for (ShellStateComponent component : components) {
                T result = component.as(type);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        @Override
        public void readNbt(NbtCompound nbt) {
            for (ShellStateComponent component : components) {
                component.readNbt(nbt);
            }
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            for (ShellStateComponent component : components) {
                nbt = component.writeNbt(nbt);
            }
            return nbt;
        }
    }
}
