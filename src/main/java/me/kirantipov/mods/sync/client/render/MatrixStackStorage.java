package me.kirantipov.mods.sync.client.render;

import net.minecraft.client.util.math.MatrixStack;

public final class MatrixStackStorage {
    private static MatrixStack modelMatrixStack;

    public static void saveModelMatrixStack(MatrixStack matrixStack) {
        modelMatrixStack = matrixStack;
    }

    public static MatrixStack getModelMatrixStack() {
        return modelMatrixStack;
    }
}