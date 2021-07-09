package me.kirantipov.mods.sync.util.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.kirantipov.mods.sync.util.math.Radians;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class RenderSystemUtil {
    public static void drawTriangleStrip(Consumer<VertexConsumer> consumer) {
        drawTriangleStrip(consumer, VertexFormats.POSITION_COLOR);
    }

    public static void drawTriangleStrip(Consumer<VertexConsumer> consumer, VertexFormat format) {
        draw(consumer, VertexFormat.DrawMode.TRIANGLE_STRIP, format);
    }

    public static void draw(Consumer<VertexConsumer> consumer, VertexFormat.DrawMode drawMode, VertexFormat format) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(drawMode, format);
        consumer.accept(bufferBuilder);
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }
}
