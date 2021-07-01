package me.kirantipov.mods.sync.recipe;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A wrapper for the recipe manager that allows us to modify recipes.
 */
public class MutableRecipeManager {
    private final RecipeManager recipeManager;

    private MutableRecipeManager(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    /**
     * @param recipeManager The recipe manager.
     * @return Wrapped recipe manager.
     */
    public static MutableRecipeManager from(RecipeManager recipeManager) {
        return new MutableRecipeManager(recipeManager);
    }

    /**
     * Transforms a recipe with the specified identifier via given mutator.
     *
     * @param recipeType Recipe's type.
     * @param id Recipe's identifier.
     * @param mutator Transformation to be applied to the recipe.
     * @param <T> Type of the recipe.
     */
    @SuppressWarnings("unchecked")
    public <T extends Recipe<?>> void mutateRecipe(RecipeType<T> recipeType, Identifier id, Consumer<T> mutator) {
        Map<Identifier, Recipe<?>> mutableRecipes = this.recipeManager.recipes.get(recipeType);
        if (mutableRecipes != null) {
            Recipe<?> recipe = mutableRecipes.get(id);
            if (recipe != null) {
                mutator.accept((T)recipe);
            }
        }
    }
}