package me.kirantipov.mods.sync.api.event;

import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Events about the workout of {@linkplain Entity entities}.
 *
 * <p>These events can be categorized into two groups:
 * <ol>
 * <li>Simple listeners: {@link #START_RUNNING} and {@link #STOP_RUNNING}</li>
 * <li>Modifiers: {@link #MODIFY_OUTPUT_ENERGY_QUANTITY}</li>
 * </ol></p>
 *
 * <p>Fitness events are useful for registering custom entities as valid treadmill users.
 * Custom treadmill users generally only need a custom {@link #MODIFY_OUTPUT_ENERGY_QUANTITY} callback,
 * but the other events might be useful as well.</p>
 */
public final class EntityFitnessEvents {
    /**
     * An event that is called when an entity starts treadmill running.
     */
    public static final Event<StartRunning> START_RUNNING = EventFactory.createArrayBacked(StartRunning.class, callbacks -> (entity, energyStorage) -> {
        for (StartRunning callback : callbacks) {
            callback.onStartRunning(entity, energyStorage);
        }
    });

    /**
     * An event that is called when an entity stops treadmill running.
     */
    public static final Event<StopRunning> STOP_RUNNING = EventFactory.createArrayBacked(StopRunning.class, callbacks -> (entity, energyStorage) -> {
        for (StopRunning callback : callbacks) {
            callback.onStopRunning(entity, energyStorage);
        }
    });

    /**
     * An event that can be used to provide amount of energy being produced by an entity if missing.
     */
    public static final Event<ModifyOutputEnergyQuantity> MODIFY_OUTPUT_ENERGY_QUANTITY = EventFactory.createArrayBacked(ModifyOutputEnergyQuantity.class, callbacks -> (entity, energyStorage, outputEnergyQuantity) -> {
        for (ModifyOutputEnergyQuantity callback : callbacks) {
            outputEnergyQuantity = callback.modifyOutputEnergyQuantity(entity, energyStorage, outputEnergyQuantity);
        }
        return outputEnergyQuantity;
    });


    @FunctionalInterface
    public interface ModifyOutputEnergyQuantity {
        /**
         * Modifies or provides amount of energy being produced by an entity.
         *
         * @param entity The running entity.
         * @param energyStorage The energy storage that stores energy being produced by the entity.
         * @param outputEnergyQuantity Amount of energy that will be produced by the entity every tick, or null if not determined by the mod logic.
         * @return Amount of energy that will be produced by the entity every tick, or null if the given entity cannot use treadmills.
         */
        @Nullable
        Double modifyOutputEnergyQuantity(Entity entity, EnergyIo energyStorage, @Nullable Double outputEnergyQuantity);
    }

    @FunctionalInterface
    public interface StartRunning {
        /**
         * Called when an entity starts treadmill running.
         *
         * @param entity The running entity.
         * @param energyStorage The energy storage that stores energy being produced by the entity.
         */
        void onStartRunning(Entity entity, EnergyIo energyStorage);
    }

    @FunctionalInterface
    public interface StopRunning {
        /**
         * Called when an entity stops treadmill running.
         *
         * @param entity The running entity.
         * @param energyStorage The energy storage that stores energy being produced by the entity.
         */
        void onStopRunning(Entity entity, EnergyIo energyStorage);
    }


    private EntityFitnessEvents() { }
}