package me.kirantipov.mods.sync.client.gui.widget;

import me.kirantipov.mods.sync.util.client.render.ColorUtil;
import me.kirantipov.mods.sync.util.client.render.RenderSystemUtil;
import me.kirantipov.mods.sync.util.math.QuarticFunction;
import me.kirantipov.mods.sync.util.math.Radians;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CrossButtonWidget extends AbstractButtonWidget {
    private static final Text DEFAULT_DESCRIPTION = new TranslatableText("gui.sync.default.cross_button.title");
    private static final int DEFAULT_COLOR = ColorUtil.fromDyeColor(DyeColor.WHITE);
    private static final float DEFAULT_STEP = Radians.R_PI_32;

    private final float step;
    private final float[] color;
    private final Text description;
    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;
    private final float borderRadius;
    private final float stickWidth;
    private final float stickHeight;
    private final float angle;

    public CrossButtonWidget(float x, float y, float width, float height, float thickness, Runnable onClick) {
        this(x, y, width, height, thickness, DEFAULT_STEP, DEFAULT_COLOR, DEFAULT_DESCRIPTION, onClick);
    }

    public CrossButtonWidget(float x, float y, float width, float height, float thickness, int color, Runnable onClick) {
        this(x, y, width, height, thickness, DEFAULT_STEP, color, DEFAULT_DESCRIPTION, onClick);
    }

    public CrossButtonWidget(float x, float y, float width, float height, float thickness, Text description, Runnable onClick) {
        this(x, y, width, height, thickness, DEFAULT_STEP, DEFAULT_COLOR, description, onClick);
    }

    public CrossButtonWidget(float x, float y, float width, float height, float thickness, int color, Text description, Runnable onClick) {
        this(x, y, width, height, thickness, DEFAULT_STEP, color, description, onClick);
    }

    public CrossButtonWidget(float x, float y, float width, float height, float thickness, float step, int color, Text description, Runnable onClick) {
        super(x, y, width, height, onClick);
        this.step = step;
        this.color = ColorUtil.toRGBA(color);
        this.description = description;

        float shiftY = (float)new QuarticFunction(4, -4 * height, height * height + width * width - 4 * thickness * thickness, 2 * height * thickness * thickness, thickness * thickness * (thickness * thickness - width * width)).getRoot(1);
        float shiftX = (float)Math.sqrt(thickness * thickness - shiftY * shiftY);
        this.angle = (float)Math.acos(shiftY / thickness);
        this.x0 = x;
        this.y0 = y + height - shiftY;
        this.x1 = x + shiftX;
        this.y1 = y;
        this.borderRadius = thickness * 0.5F;
        this.stickHeight = thickness;
        this.stickWidth = MathHelper.sqrt(MathHelper.square(width - shiftX) + MathHelper.square(height - shiftY));
    }

    @Override
    protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystemUtil.drawRectangle(matrices, this.x0, this.y0, this.stickWidth, this.stickHeight, this.borderRadius, 1, -this.angle, this.step, this.color[0], this.color[1], this.color[2], this.color[3]);
        RenderSystemUtil.drawRectangle(matrices, this.x1, this.y1, this.stickWidth, this.stickHeight, this.borderRadius, 1, this.angle, this.step, this.color[0], this.color[1], this.color[2], this.color[3]);
    }

    @Override
    protected Text getWidgetDescription() {
        return this.description;
    }
}