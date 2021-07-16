package me.kirantipov.mods.sync.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.api.networking.SynchronizationRequestPacket;
import me.kirantipov.mods.sync.client.gl.MSAAFramebuffer;
import me.kirantipov.mods.sync.client.render.MatrixStackStorage;
import me.kirantipov.mods.sync.entity.PersistentCameraEntity;
import me.kirantipov.mods.sync.entity.PersistentCameraEntityGoal;
import me.kirantipov.mods.sync.entity.ShellEntity;
import me.kirantipov.mods.sync.util.BlockPosUtil;
import me.kirantipov.mods.sync.util.client.render.ColorUtil;
import me.kirantipov.mods.sync.util.client.render.RenderSystemUtil;
import me.kirantipov.mods.sync.util.math.Radians;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

import java.util.function.BiPredicate;

@Environment(EnvType.CLIENT)
public class ShellSelectorButtonWidget extends AbstractWidget {
    private static final DyeColor DEFAULT_COLOR = DyeColor.BLACK;
    private static final float DEFAULT_ALPHA = 0.6F;
    private static final float DEFAULT_HOVERED_ALPHA = 0.8F;
    private static final float DEFAULT_PRESSED_ALPHA = 1F;
    private static final double DEFAULT_STEP = Radians.R_PI_128;

    private final double cX;
    private final double cY;
    private final double majorR;
    private final double minorR;
    private final double diffR;
    private final double minorRBorder;
    private final double from;
    private final double to;
    private final double step;
    private final DyeColor color;
    private final float alpha;
    private final float hoveredAlpha;
    private final float pressedAlpha;
    private final BiPredicate<Double, Double> belongsToSectorPredicate;

    public ShellState shell;

    public ShellSelectorButtonWidget(double cX, double cY, double majorR, double minorR, double borderWidth, double from, double to) {
        this(cX, cY, majorR, minorR, borderWidth, from, to, DEFAULT_STEP, DEFAULT_COLOR, DEFAULT_ALPHA, DEFAULT_HOVERED_ALPHA, DEFAULT_PRESSED_ALPHA);
    }

    public ShellSelectorButtonWidget(double cX, double cY, double majorR, double minorR, double borderWidth, double from, double to, DyeColor color) {
        this(cX, cY, majorR, minorR, borderWidth, from, to, DEFAULT_STEP, color, DEFAULT_ALPHA, DEFAULT_HOVERED_ALPHA, DEFAULT_PRESSED_ALPHA);
    }

    public ShellSelectorButtonWidget(double cX, double cY, double majorR, double minorR, double borderWidth, double from, double to, DyeColor color, float alpha, float hoveredAlpha, float pressedAlpha) {
        this(cX, cY, majorR, minorR, borderWidth, from, to, DEFAULT_STEP, color, alpha, hoveredAlpha, pressedAlpha);
    }

    public ShellSelectorButtonWidget(double cX, double cY, double majorR, double minorR, double borderWidth, double from, double to, double step, DyeColor color, float alpha, float hoveredAlpha, float pressedAlpha) {
        boolean isFullCircle = Math.abs(Radians.R_2_PI - to + from) < step;
        if (isFullCircle) {
            double tmp = from;
            from = -to;
            to = -tmp;
        }

        this.cX = cX;
        this.cY = cY;
        this.majorR = majorR;
        this.minorR = minorR;
        this.diffR = majorR - minorR;
        this.minorRBorder = majorR - borderWidth;
        this.from = from;
        this.to = to;
        this.step = step;
        this.color = color;
        this.alpha = alpha;
        this.hoveredAlpha = hoveredAlpha;
        this.pressedAlpha = pressedAlpha;

        double x0 = majorR * Math.cos(from) + cX;
        double y0 = majorR * Math.sin(from) + cY;
        double x1 = majorR * Math.cos(to) + cX;
        double y1 = majorR * Math.sin(to) + cY;

        double a0 = (y0 - cY) / (x0 - cX);
        double b0 = -a0 * cX + cY;
        double a1 = (y1 - cY) / (x1 - cX);
        double b1 = -a1 * cX + cY;

        if (isFullCircle) {
            this.belongsToSectorPredicate = (x, y) -> true;
        } else if (from < Radians.R_PI_2 && to > Radians.R_PI_2) {
            this.belongsToSectorPredicate = (x, y) -> liesOnOrAboveLine(x, y, a0, b0) && liesOnOrAboveLine(x, y, a1, b1);
        } else if (from < Radians.R_3_PI_2 && to > Radians.R_3_PI_2) {
            this.belongsToSectorPredicate = (x, y) -> liesOnOrBelowLine(x, y, a0, b0) && liesOnOrBelowLine(x, y, a1, b1);
        } else if (y1 > y0) {
            this.belongsToSectorPredicate = (x, y) -> liesOnOrAboveLine(x, y, a0, b0) && liesOnOrBelowLine(x, y, a1, b1);
        } else {
            this.belongsToSectorPredicate = (x, y) -> liesOnOrBelowLine(x, y, a0, b0) && liesOnOrAboveLine(x, y, a1, b1);
        }
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.shell != null && liesOnCircle(mouseX, mouseY, this.cX, this.cY, this.majorR) && !liesOnCircle(mouseX, mouseY, this.cX, this.cY, this.minorR) && this.belongsToSectorPredicate.test(mouseX, mouseY);
    }

    @Override
    protected void onMouseClick(double mouseX, double mouseY, int button) {
        if (this.shell == null || this.shell.getProgress() < ShellState.PROGRESS_DONE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            return;
        }

        BlockPos pos = player.getBlockPos();
        Direction facing = BlockPosUtil.getHorizontalFacing(pos, world).orElse(Direction.NORTH);
        PersistentCameraEntityGoal cameraGoal = PersistentCameraEntityGoal.stairwayToHeaven(pos, facing, shell.getPos(), x -> new SynchronizationRequestPacket(this.shell).send());
        PersistentCameraEntity.setup(client, cameraGoal);

        client.openScreen(null);
    }

    @Override
    protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderSector(matrices);

        if (this.shell != null) {
            if (this.shell.getProgress() < ShellState.PROGRESS_PRINTING) {
                this.renderShellAndProgress(matrices);
            } else {
                MSAAFramebuffer.renderAfterUsage(() -> this.renderShellAndProgress(matrices));
            }
        }
    }

    private void renderSector(MatrixStack matrices) {
        float alpha = this.isPressed() ? this.pressedAlpha : this.isHovered() ? this.hoveredAlpha : this.alpha;
        DyeColor borderColor = this.shell == null ? null : this.shell.getColor();
        DyeColor color = borderColor != null && (this.isHovered() || this.isPressed()) ? borderColor : this.color;
        float[] rgb = color.getColorComponents();
        RenderSystemUtil.drawAnnulusSector(matrices, this.cX, this.cY, this.majorR, this.minorR, this.from, this.to, this.step, rgb[0], rgb[1], rgb[2], alpha);

        if (borderColor != null) {
            rgb = borderColor.getColorComponents();
            RenderSystemUtil.drawAnnulusSector(matrices, this.cX, this.cY, this.majorR, this.minorRBorder, this.from, this.to, this.step, rgb[0], rgb[1], rgb[2], 1F);
        }
    }

    private void renderShellAndProgress(MatrixStack matrices) {
        this.renderShell(matrices);
        if (this.shell.getProgress() < ShellState.PROGRESS_DONE) {
            this.renderProgress(matrices);
        }
    }

    private void renderShell(MatrixStack matrices) {
        final double SHELL_WIDTH_HALF = 0.26F;
        final double SHELL_HEIGHT_HALF = 0.35F;
        final float SHELL_SCALE = 0.365F;

        ShellEntity shellEntity = this.shell.asEntity();
        shellEntity.pitchProgress = 0;
        shellEntity.isActive = this.shell.getProgress() >= ShellState.PROGRESS_DONE;

        double r = this.minorR + this.diffR / 2;
        double tAngle = (this.to + this.from) / 2;
        double shellCX = r * Math.cos(tAngle) + this.cX;
        double shellCY = r * Math.sin(tAngle) + this.cY;
        double tX = shellCX - this.diffR * SHELL_WIDTH_HALF;
        double tY = shellCY + this.diffR * SHELL_HEIGHT_HALF;
        float scale = (float)this.diffR * SHELL_SCALE;

        matrices.push();
        matrices.translate(tX, tY, this.majorR);
        matrices.scale(scale, -scale, 1F);
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(15));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(40));

        RenderSystem.setupGui3DDiffuseLighting(new Vec3f((float)this.cX * 2, (float)this.cY * 2, -1), new Vec3f(0, 0, 1));
        MatrixStackStorage.saveModelMatrixStack(matrices);
        VertexConsumerProvider.Immediate immediate = RenderSystemUtil.getEntityVertexConsumerProvider();
        EntityRenderDispatcher renderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        renderDispatcher.render(shellEntity, 0, 0, 0, 0,0, matrices, immediate, RenderSystemUtil.MAX_LIGHT_LEVEL);
        immediate.draw();

        matrices.pop();
    }

    private void renderProgress(MatrixStack matrices) {
        final float FONT_SCALE = 0.15F;
        final float BOX_SCALE = 1.6F;

        TextRenderer textRenderer = RenderSystemUtil.getTextRenderer();
        double r = this.minorR + this.diffR / 2;
        double tAngle = (this.to + this.from) / 2;
        float shellCX = (float)(r * Math.cos(tAngle) + this.cX);
        float shellCY = (float)(r * Math.sin(tAngle) + this.cY);
        float fontHeight = (float)this.diffR * FONT_SCALE;
        float fontScale = fontHeight / textRenderer.fontHeight;

        int progress = (int)Math.floor(this.shell.getProgress() * 100);
        Text progressText = Text.of(String.format("%s%%", progress));
        float boxHeight = fontHeight * BOX_SCALE;
        float progressTextWidth = textRenderer.getWidth(progressText) * fontScale;
        float progressBoxWidth = Math.max(boxHeight * 2F, progressTextWidth);
        float boxTop = shellCY - boxHeight / 2 - textRenderer.fontHeight * fontScale * 0.125F;
        float boxLeft = shellCX - progressBoxWidth / 2F;

        matrices.push();
        matrices.translate(0, 0, this.majorR * 2);
        RenderSystemUtil.drawRectangle(matrices, boxLeft, boxTop, progressBoxWidth, boxHeight, boxHeight * 0.25F, 1F, 0, (float)this.step, 0F, 0F, 0F, 0.8F);
        RenderSystemUtil.drawCenteredText(progressText, matrices, shellCX, shellCY, fontScale, ColorUtil.fromDyeColor(DyeColor.WHITE), true);
        matrices.pop();
    }

    @Override
    protected Text getWidgetDescription() {
        if (this.shell == null) {
            return null;
        }

        BlockPos pos = this.shell.getPos();
        return Text.of(String.format("%s, %s, %s", pos.getX(), pos.getY(), pos.getZ()));
    }

    private static boolean liesOnCircle(double x, double y, double cX, double cY, double r) {
        return MathHelper.square(cX - x) + MathHelper.square(cY - y) <= r * r;
    }

    private static boolean liesOnOrAboveLine(double x, double y, double a, double b) {
        return y >= a * x + b;
    }

    private static boolean liesOnOrBelowLine(double x, double y, double a, double b) {
        return y <= a * x + b;
    }
}
