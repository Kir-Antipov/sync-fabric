package dev.kir.sync.easteregg.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public final class MixinEasterEggs implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String name = getPackageName(mixinClassName);
        JsonObject config = this.getConfig();
        if (
            config.has(name) && config.get(name) instanceof JsonObject configEntry &&
            configEntry.has("enable") && configEntry.get("enable") instanceof JsonPrimitive enable && enable.isBoolean()
        ) {
            return enable.getAsBoolean();
        }

        return true;
    }

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    private static String getPackageName(String mixinClassName) {
        int mixinStartI = mixinClassName.lastIndexOf("mixin.");
        if (mixinStartI == -1)
            return mixinClassName;

        int endI = mixinClassName.indexOf('.', mixinStartI + 6);
        return mixinClassName.substring(mixinStartI + 6, endI);
    }

    private JsonObject config;
    private JsonObject getConfig() {
        if (this.config == null) {
            try {
                Path path = Path.of("./config/sync.json");
                if (Files.isReadable(path)) {
                    String json = Files.readString(path);
                    this.config = (JsonObject)JsonParser.parseString(json);
                }
            } catch (Throwable e) {
                this.config = null;
            }

            if (this.config == null) {
                this.config = new JsonObject();
            }

            if (this.config.has("easterEggs") && this.config.get("easterEggs") instanceof JsonObject easterEggs) {
                this.config = easterEggs;
            }
        }
        return this.config;
    }
}
