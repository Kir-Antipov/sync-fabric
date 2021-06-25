package me.kirantipov.mods.sync.api.energy;

/**
 * An interface that allows applications
 * to implement a provider for an energy container.
 */
public interface EnergyContainerProvider {
    /**
     * @return Energy container.
     */
    EnergyContainer getEnergyContainer();
}