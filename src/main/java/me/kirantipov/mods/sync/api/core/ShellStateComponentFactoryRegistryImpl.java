package me.kirantipov.mods.sync.api.core;

import java.util.*;

class ShellStateComponentFactoryRegistryImpl implements ShellStateComponentFactoryRegistry {
    public static final ShellStateComponentFactoryRegistryImpl INSTANCE = new ShellStateComponentFactoryRegistryImpl();

    private final Set<ShellStateComponentFactory> factories = new HashSet<>(16);

    @Override
    public Set<ShellStateComponentFactory> getValues() {
        return Collections.unmodifiableSet(this.factories);
    }

    @Override
    public ShellStateComponentFactory register(ShellStateComponentFactory factory) {
        this.factories.add(factory);
        return factory;
    }
}
