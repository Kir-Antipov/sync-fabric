package dev.kir.sync.entity;

public interface KillableEntity {
    default void onKillableEntityDeath() { }

    default boolean updateKillableEntityPostDeath() {
        return false;
    }
}
