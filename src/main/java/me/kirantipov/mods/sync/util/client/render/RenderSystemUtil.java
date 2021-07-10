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

    public static void drawRectangle(MatrixStack matrices, float x, float y, float width, float height, float borderRadius, float scale, float rotation, float step, float r, float g, float b, float a) {
        drawTriangleStrip(consumer -> drawRectangle(matrices, consumer, x, y, width, height, borderRadius, scale, rotation, step, r, g, b, a));
    }

    public static void drawRectangle(MatrixStack matrices, VertexConsumer consumer, float x, float y, float width, float height, float borderRadius, float scale, float rotation, float step, float r, float g, float b, float a) {
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(rotation));
        matrices.scale(scale, scale, 1);
        matrices.translate(-x, -y, 0);
        Matrix4f matrix = matrices.peek().getModel();

        drawQuadrant(matrix, consumer, x + width - borderRadius, y + height - borderRadius, borderRadius, 0, step, r, g, b, a);
        drawQuadrant(matrix, consumer, x + borderRadius, y + height - borderRadius, borderRadius, 1, step, r, g, b, a);
        drawQuadrant(matrix, consumer, x + borderRadius, y + borderRadius, borderRadius, 2, step, r, g, b, a);
        drawQuadrant(matrix, consumer, x + width - borderRadius, y + borderRadius, borderRadius, 3, step, r, g, b, a);

        consumer.vertex(matrix, x + width, y + height - borderRadius, 0).color(r, g, b, a).next();
        consumer.vertex(matrix, x + borderRadius, y + borderRadius, 0).color(r, g, b, a).next();
        consumer.vertex(matrix, x + borderRadius, y + height - borderRadius, 0).color(r, g, b, a).next();

        matrices.pop();
    }

    private static void drawQuadrant(Matrix4f matrix, VertexConsumer consumer, float cX, float cY, float radius, int index, float step, float r, float g, float b, float a) {
        float start = Radians.R_PI_2 * index;
        float end = Radians.R_PI_2 * (index + 1);
        for (float i = start; i < end; i += step) {
            float x = radius * (float)Math.cos(i) + cX;
            float y = radius * (float)Math.sin(i) + cY;
            consumer.vertex(matrix, x, y, 0).color(r, g, b, a).next();
            consumer.vertex(matrix, cX, cY, 0).color(r, g, b, a).next();
        }
        float x = radius * (float)Math.cos(end) + cX;
        float y = radius * (float)Math.sin(end) + cY;
        consumer.vertex(matrix, x, y, 0).color(r, g, b, a).next();
        consumer.vertex(matrix, cX, cY, 0).color(r, g, b, a).next();
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
