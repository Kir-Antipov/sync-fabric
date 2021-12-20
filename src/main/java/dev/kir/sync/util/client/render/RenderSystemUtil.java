package dev.kir.sync.util.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.kir.sync.util.math.Radians;
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
    public static final int MAX_LIGHT_LEVEL = (15 << 20) | (15 << 4);
    private static final BufferBuilderStorage BUFFER_BUILDER_STORAGE = new BufferBuilderStorage();

    public static void drawTriangleStrip(Consumer<VertexConsumer> consumer) {
        drawTriangleStrip(consumer, VertexFormats.POSITION_COLOR);
    }

    public static void drawTriangleStrip(Consumer<VertexConsumer> consumer, VertexFormat format) {
        draw(consumer, VertexFormat.DrawMode.TRIANGLE_STRIP, format);
    }

    public static void drawAnnulusSector(MatrixStack matrices, double cX, double cY, double majorR, double minorR, double from, double to, double step, float r, float g, float b, float a) {
        drawTriangleStrip(consumer -> drawAnnulusSector(matrices, consumer, cX, cY, majorR, minorR, from, to, step, r, g, b, a));
    }

    public static void drawAnnulusSector(MatrixStack matrices, VertexConsumer consumer, double cX, double cY, double majorR, double minorR, double from, double to, double step, float r, float g, float b, float a) {
        to += step / 32;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        for (double i = from; i < to; i += step) {
            double sin = Math.sin(i);
            double cos = Math.cos(i);

            double x0 = majorR * cos + cX;
            double y0 = majorR * sin + cY;

            double x1 = minorR * cos + cX;
            double y1 = minorR * sin + cY;

            consumer.vertex(matrix, (float)x0, (float)y0, 0).color(r, g, b, a).next();
            consumer.vertex(matrix, (float)x1, (float)y1, 0).color(r, g, b, a).next();
        }
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
        Matrix4f matrix = matrices.peek().getPositionMatrix();

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

    public static TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static VertexConsumerProvider.Immediate getEntityVertexConsumerProvider() {
        return BUFFER_BUILDER_STORAGE.getEntityVertexConsumers();
    }

    public static void drawCenteredText(Text text, MatrixStack matrices, float cX, float cY, int color) {
        drawCenteredText(text, matrices, cX, cY, 1F, color);
    }

    public static void drawCenteredText(Text text, MatrixStack matrices, float cX, float cY, float scale, int color) {
        drawCenteredText(text, matrices, cX, cY, scale, color, false);
    }

    public static void drawCenteredText(Text text, MatrixStack matrices, float cX, float cY, float scale, int color, boolean shadow) {
        drawCenteredText(text, matrices, getTextRenderer(), cX, cY, scale, color, shadow);
    }

    public static void drawCenteredText(Text text, MatrixStack matrices, TextRenderer textRenderer, float cX, float cY, float scale, int color, boolean shadow) {
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        drawCenteredText(text, matrices, immediate, textRenderer, cX, cY, scale, color, shadow);
        immediate.draw();
    }

    public static void drawCenteredText(Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, TextRenderer textRenderer, float cX, float cY, float scale, int color, boolean shadow) {
        final int backgroundColor = 0;

        float height = textRenderer.fontHeight * scale;
        float width = textRenderer.getWidth(text) * scale;
        cX -= width / 2F;
        cY -= height / 2F;

        matrices.push();
        matrices.translate(cX, cY, 0);
        matrices.scale(scale, scale, 1F);
        matrices.translate(-cX, -cY, 0);
        textRenderer.draw(text, cX, cY, color, shadow, matrices.peek().getPositionMatrix(), vertexConsumers, false, backgroundColor, MAX_LIGHT_LEVEL);
        matrices.pop();
    }
}
