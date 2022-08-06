package dev.kir.sync.entity;

public interface LookingEntity {
    default boolean changeLookingEntityLookDirection(double cursorDeltaX, double cursorDeltaY) {
        return false;
    }
}
