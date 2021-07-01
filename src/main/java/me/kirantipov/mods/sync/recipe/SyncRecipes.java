package me.kirantipov.mods.sync.recipe;

import me.kirantipov.mods.sync.item.SyncItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;

/**
 * Initializes recipe-related logic of the mod.
 */
public class SyncRecipes {
    private static void reload(MinecraftServer server) {
        MutableRecipeManager mutableRecipeManager = MutableRecipeManager.from(server.getRecipeManager());
        reloadSyncCoreRecipe(server.isHardcore(), mutableRecipeManager);
    }

    private static void reloadSyncCoreRecipe(boolean isHardcore, MutableRecipeManager mutableRecipeManager) {
        mutableRecipeManager.mutateRecipe(RecipeType.CRAFTING, Registry.ITEM.getId(SyncItems.SYNC_CORE), recipe -> {
            Item item = isHardcore ? Items.NETHER_STAR : Items.ENDER_PEARL;
            recipe.getIngredients().set(4, Ingredient.ofItems(item));
        });
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(SyncRecipes::reload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> SyncRecipes.reload(server));
    }
}