package dev.kir.sync.api.shell;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents attachable shell data.
 */
public abstract class ShellStateComponent {
    /**
     * @return Identifier of the component.
     */
    public abstract String getId();

    /**
     * @return Items that are stored in the component.
     */
    public Collection<ItemStack> getItems() {
        return List.of();
    }

    /**
     * @return Experience points that are stored in the component.
     */
    public int getXp() {
        return 0;
    }

    /**
     * Clones state of the given component.
     * @param component The component.
     */
    public abstract void clone(ShellStateComponent component);

    /**
     * Restores state of the component from the nbt data.
     * @param nbt The nbt data.
     */
    @ApiStatus.NonExtendable
    public void readNbt(NbtCompound nbt) {
        this.readComponentNbt(nbt.getCompound(this.getId()));
    }

    /**
     * Restores state of the component from the nbt data.
     * @param nbt The nbt data.
     */
    protected abstract void readComponentNbt(NbtCompound nbt);

    /**
     * Stores the state of the component to the nbt.
     * @param nbt The nbt data.
     * @return The nbt data.
     */
    @ApiStatus.NonExtendable
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound componentNbt = this.writeComponentNbt(new NbtCompound());
        nbt.put(this.getId(), componentNbt);
        return nbt;
    }

    /**
     * Stores the state of the component to the nbt.
     * @param nbt The nbt data.
     * @return The nbt data.
     */
    protected abstract NbtCompound writeComponentNbt(NbtCompound nbt);

    /**
     * Attempts to cast the component to the given type.
     * @param type The target type.
     * @param <T> The target type.
     * @return Casted version of the component, if the operation succeeded; otherwise, null.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> type) {
        return type.isInstance(this) ? (T)this : null;
    }


    /**
     * Creates a new instance of {@link ShellStateComponent} that has no player data.
     *
     * @return The {@linkplain ShellStateComponent}.
     */
    public static ShellStateComponent empty() {
        Collection<ShellStateComponentFactoryRegistry.ShellStateComponentFactory> factories = ShellStateComponentFactoryRegistry.getInstance().getValues();
        List<ShellStateComponent> components = new ArrayList<>(factories.size());
        for (ShellStateComponentFactoryRegistry.ShellStateComponentFactory factory : factories) {
            components.add(factory.empty());
        }
        return ShellStateComponent.combine(components);
    }

    /**
     * Creates a new instance of {@link ShellStateComponent} that is synced with the player's state.
     * @param player The player.
     * @return The {@linkplain ShellStateComponent}.
     */
    public static ShellStateComponent of(ServerPlayerEntity player) {
        Collection<ShellStateComponentFactoryRegistry.ShellStateComponentFactory> factories = ShellStateComponentFactoryRegistry.getInstance().getValues();
        List<ShellStateComponent> components = new ArrayList<>(factories.size());
        for (ShellStateComponentFactoryRegistry.ShellStateComponentFactory factory : factories) {
            components.add(factory.of(player));
        }
        return ShellStateComponent.combine(components);
    }


    /**
     * Combines several {@link ShellStateComponent} into a single one.
     * @return The combined {@linkplain ShellStateComponent}.
     */
    public static ShellStateComponent combine() {
        return EmptyShellStateComponent.INSTANCE;
    }

    /**
     * Combines several {@link ShellStateComponent} into a single one.
     * @param component The components to be combined.
     * @return The combined {@linkplain ShellStateComponent}.
     */
    public static ShellStateComponent combine(ShellStateComponent component) {
        return component;
    }

    /**
     * Combines several {@link ShellStateComponent} into a single one.
     * @param components The components to be combined.
     * @return The combined {@linkplain ShellStateComponent}.
     */
    public static ShellStateComponent combine(ShellStateComponent... components) {
        return combine(Arrays.asList(components));
    }

    /**
     * Combines several {@link ShellStateComponent} into a single one.
     * @param components The components to be combined.
     * @return The combined {@linkplain ShellStateComponent}.
     */
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
        public <T> T as(Class<T> type) {
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
            for (ShellStateComponent component : this.components) {
                items.addAll(component.getItems());
            }
            return items;
        }

        @Override
        public int getXp() {
            int xp = 0;
            for (ShellStateComponent component : this.components) {
                xp += component.getXp();
            }
            return xp;
        }

        @Override
        public void clone(ShellStateComponent component) {
            for (ShellStateComponent innerComponent : this.components) {
                innerComponent.clone(component);
            }
        }

        @Override
        public <T> T as(Class<T> type) {
            for (ShellStateComponent component : this.components) {
                T result = component.as(type);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        @Override
        public void readNbt(NbtCompound nbt) {
            for (ShellStateComponent component : this.components) {
                component.readNbt(nbt);
            }
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            for (ShellStateComponent component : this.components) {
                component.writeNbt(nbt);
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
