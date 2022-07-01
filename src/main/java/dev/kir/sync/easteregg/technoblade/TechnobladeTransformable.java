package dev.kir.sync.easteregg.technoblade;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TechnobladeTransformable {
    default boolean isTechnoblade() {
        return this.asTechnoblade() != null;
    }

    Technoblade asTechnoblade();

    boolean transformIntoTechnoblade();
}
