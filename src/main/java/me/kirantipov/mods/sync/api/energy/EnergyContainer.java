package me.kirantipov.mods.sync.api.energy;

/**
 * An interface that allows applications
 * to implement an energy container.
 */
public interface EnergyContainer {
    /**
     * @return Energy amount that is currently stored in this container.
     */
    float getAmount();

    /**
     * @return The maximum amount of energy this container can store.
     */
    float getCapacity();

    /**
     * Extracts energy from the container.
     *
     * @param pj Requested amount of energy.
     * @return The amount of energy extracted from the container.
     */
    float extract(float pj);
}