package me.kirantipov.mods.sync.api.shell;

import java.util.*;

class ShellStateComponentFactoryRegistryImpl implements ShellStateComponentFactoryRegistry {
    public static final ShellStateComponentFactoryRegistryImpl INSTANCE = new ShellStateComponentFactoryRegistryImpl();

    private final Set<ShellStateComponentFactory> factories = new HashSet<>(16);

    @Override
    public Collection<ShellStateComponentFactory> getValues() {
        return Collections.unmodifiableSet(this.factories);
    }

    @Override
    public ShellStateComponentFactory register(ShellStateComponentFactory factory) {
        this.factories.add(factory);
        return factory;
    }
}
