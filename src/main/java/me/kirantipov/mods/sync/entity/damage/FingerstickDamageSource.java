package me.kirantipov.mods.sync.entity.damage;

import net.minecraft.entity.damage.DamageSource;

public class FingerstickDamageSource extends DamageSource {
    private static final DamageSource FINGERSTICK = new FingerstickDamageSource("sync.fingerstick");

    private FingerstickDamageSource(String name) {
        super(name);
        this.setUnblockable();
        this.setBypassesArmor();
    }

    public static DamageSource getInstance() {
        return FINGERSTICK;
    }
}