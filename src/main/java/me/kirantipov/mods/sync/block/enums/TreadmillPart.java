package me.kirantipov.mods.sync.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum TreadmillPart implements StringIdentifiable {
    FRONT("front"),
    BACK("back");

    private final String name;

    TreadmillPart(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}