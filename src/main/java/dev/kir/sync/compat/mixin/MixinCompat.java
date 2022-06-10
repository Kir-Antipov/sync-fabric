package dev.kir.sync.compat.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MixinCompat implements IMixinConfigPlugin {
    private static final Map<String, String> PACKAGE_TO_ID = Map.of(
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String targetModId = getTargetModId(mixinClassName);
        return FabricLoader.getInstance().isModLoaded(targetModId);
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

    private static String getTargetModId(String mixinClassName) {
        String packageName = getPackageName(mixinClassName);
        return PACKAGE_TO_ID.getOrDefault(packageName, packageName);
    }

    private static String getPackageName(String mixinClassName) {
        int mixinStartI = mixinClassName.lastIndexOf("mixin.");
        if (mixinStartI == -1)
            return mixinClassName;

        int endI = mixinClassName.indexOf('.', mixinStartI + 6);
        return mixinClassName.substring(mixinStartI + 6, endI);
    }
}
