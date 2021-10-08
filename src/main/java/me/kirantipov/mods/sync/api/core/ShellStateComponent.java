package me.kirantipov.mods.sync.api.core;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class ShellStateComponent {
    public abstract String getId();

    public Collection<ItemStack> getItems() {
        return List.of();
    }

    public int getXp() {
        return 0;
    }

    public void clear() { }

    public abstract void clone(ShellStateComponent component);

    public void readNbt(NbtCompound nbt) {
        this.readComponentNbt(nbt.getCompound(this.getId()));
    }

    protected abstract void readComponentNbt(NbtCompound nbt);

    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound componentNbt = this.writeComponentNbt(new NbtCompound());
        nbt.put(this.getId(), componentNbt);
        return nbt;
    }

    protected abstract NbtCompound writeComponentNbt(NbtCompound nbt);

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
        return switch (components.size()) {
            case 0 -> EmptyShellStateComponent.INSTANCE;
            case 1 -> components.iterator().next();
            default -> new CombinedShellStateComponent(components);
        };
    }


    private static class EmptyShellStateComponent extends ShellStateComponent {
        public static final ShellStateComponent INSTANCE = new EmptyShellStateComponent();

        @Override
        public String getId() {
            return "sync:empty";
        }

        @Override
        public void clone(ShellStateComponent component) { }

        @Override
        public void readNbt(NbtCompound nbt) { }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            return nbt;
        }

        @Override
        public <T extends ShellStateComponent> @Nullable T as(Class<T> type) {
            return null;
        }

        @Override
        protected void readComponentNbt(NbtCompound nbt) { }

        @Override
        protected NbtCompound writeComponentNbt(NbtCompound nbt) {
            return nbt;
        }
    }

    private static class CombinedShellStateComponent extends ShellStateComponent {
        private final Collection<ShellStateComponent> components;

        public CombinedShellStateComponent(Collection<ShellStateComponent> components) {
            this.components = List.copyOf(components);
        }

        @Override
        public String getId() {
            return "sync:combined";
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

        @Override
        protected void readComponentNbt(NbtCompound nbt) { }

        @Override
        protected NbtCompound writeComponentNbt(NbtCompound nbt) {
            return nbt;
        }
    }
}
