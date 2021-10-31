package dev.kir.sync.client.model;

import dev.kir.sync.api.shell.ShellState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class ShellConstructorModel extends AbstractShellContainerModel {
    public final ModelPart floor;
    public final ModelPart floorRimF;
    public final ModelPart floorRimL;
    public final ModelPart floorRimR;
    public final ModelPart floorRimB;

    public final ModelPart ceiling;
    public final ModelPart ceilingRimF;
    public final ModelPart ceilingRimL;
    public final ModelPart ceilingRimR;
    public final ModelPart ceilingRimB;

    public final ModelPart wallLL;
    public final ModelPart wallLU;
    public final ModelPart wallRL;
    public final ModelPart wallRU;
    public final ModelPart wallBL;
    public final ModelPart wallBU;

    public final ModelPart pillarBLU;
    public final ModelPart pillarBLL;
    public final ModelPart pillarBRU;
    public final ModelPart pillarBRL;
    public final ModelPart pillarFLU;
    public final ModelPart pillarFLL;
    public final ModelPart pillarFRU;
    public final ModelPart pillarFRL;

    public final ModelPart sprayerR;
    public final ModelPart sprayerG;
    public final ModelPart sprayerB;
    public final ModelPart sprayerRMast;
    public final ModelPart sprayerGMast;
    public final ModelPart sprayerBMast;

    public final ModelPart printerL;
    public final ModelPart printerR;

    public float buildProgress;
    public boolean showInnerParts;

    public ShellConstructorModel() {
        ModelPart rightPart = this.createRotationTemplate(0, 0.7853982F, 0);
        ModelPart leftPart = this.createRotationTemplate(0, -0.7853982F, 0);

        this.floor = this.addCuboid(BOTTOM, 64, 62, -16, 23, -16, 32, 1, 32);
        this.floorRimF = this.addCuboid(BOTTOM, 70, 34, -13, 22, -14, 26, 1, 3);
        this.floorRimL = this.addCuboid(BOTTOM, 0, 38, 13, 22, -14, 3, 1, 27);
        this.floorRimR = this.addCuboid(BOTTOM, 0, 38, -16, 22, -14, 3, 1, 27);
        this.floorRimB = this.addCuboid(BOTTOM, 0, 34, -16, 22, 13, 32, 1, 3);

        this.ceiling = this.addCuboid(TOP, 0, 0, -16, -8, -16, 32, 2, 32);
        this.ceilingRimF = this.addCuboid(TOP, 70, 34, -13, -6, -14, 26, 1, 3);
        this.ceilingRimL = this.addCuboid(TOP, 0, 38, 13, -6, -14, 3, 1, 27);
        this.ceilingRimR = this.addCuboid(TOP, 0, 38, -16, -6, -14, 3, 1, 27);
        this.ceilingRimB = this.addCuboid(TOP, 0, 34, -16, -6, 13, 32, 1, 3);

        this.wallLL = this.addCuboid(BOTTOM, 200, 122, 15.05F, -8, -14, 1, 30, 27);
        this.wallLU = this.addCuboid(TOP, 200, 66, 15.05F, -5, -14, 1, 29, 27);

        this.wallRL = this.addCuboid(BOTTOM, 200, 122, -16.05F, -8, -14, 1, 30, 27);
        this.wallRU = this.addCuboid(TOP, 200, 66, -16.05F, -5, -14, 1, 29, 27);

        this.wallBL = this.addCuboid(BOTTOM, 101, 188, -13, -8, 14, 26, 30, 1);
        this.wallBU = this.addCuboid(TOP, 101, 156, -13, -5, 14, 26, 29, 1);

        this.pillarFLL = this.addCuboid(BOTTOM, 60, 68, -16, -8, -15, 1, 31, 1);
        this.pillarFLU = this.addCuboid(TOP, 60, 38, -16, -6, -15, 1, 30, 1);

        this.pillarFRL = this.addCuboid(BOTTOM, 60, 68, 15, -8, -15, 1, 31, 1);
        this.pillarFRU = this.addCuboid(TOP, 60, 38, 15, -6, -15, 1, 30, 1);

        this.pillarBLL = this.addCuboid(BOTTOM, 0, 155, -16, -8, 13, 3, 30, 3);
        this.pillarBLU = this.addCuboid(TOP, 0, 126, -16, -5, 13, 3, 29, 3);

        this.pillarBRL = this.addCuboid(BOTTOM, 0, 155, 13, -8, 13, 3, 30, 3);
        this.pillarBRU = this.addCuboid(TOP, 0, 126, 13, -5, 13, 3, 29, 3);

        this.sprayerR = this.createCuboid(132, 0, -12, -9, -9.55F, 2, 1, 2, rightPart);
        this.sprayerG = this.createCuboid(132, 0, 10, -9, -9.55F, 2, 1, 2, leftPart);
        this.sprayerB = this.createCuboid(132, 0, -1, -9, 10.55F, 2, 1, 2);

        this.sprayerRMast = this.createCuboid(128, 0, -11.5F, -8.5F, -9.5F, 1, 32, 1, rightPart);
        this.sprayerGMast = this.createCuboid(128, 0, 10.5F, -8.5F, -9.5F, 1, 32, 1, leftPart);
        this.sprayerBMast = this.createCuboid(128, 0, -0.5F, -8.5F, 11.5F, 1, 32, 1);

        this.printerL = this.createCuboid(54, 66, 13, 20, 12, 1, 2, 2, leftPart);
        this.printerR = this.createCuboid(54, 66, -14, 20, 12, 1, 2, 2, rightPart);
    }

    @Override
    public void render(DoubleBlockProperties.Type type, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        super.render(type, matrices, vertices, light, overlay, red, green, blue, alpha);
        if (type != BOTTOM || !this.showInnerParts) {
            return;
        }

        final float MIN_SPRAYER_Y = 22;
        final float SPRAYER_HEIGHT = -60;
        final float MIN_PRINTER_Y = 20;
        final float PRINTER_HEIGHT = -58;
        final float MAST_HEIGHT = 32;
        final float MAST_Y = MIN_SPRAYER_Y - MAST_HEIGHT;
        final float SPRAYER_ACTIVATION_STAGE = 0.9F;

        float printingProgress = this.buildProgress / ShellState.PROGRESS_PRINTING;
        float paintingProgress = (this.buildProgress - ShellState.PROGRESS_PRINTING) / ShellState.PROGRESS_PAINTING;

        float printerY = MIN_PRINTER_Y + PRINTER_HEIGHT * (printingProgress <= 1F ? printingProgress : (1F - paintingProgress));
        float sprayerY = MIN_SPRAYER_Y + SPRAYER_HEIGHT * (printingProgress < SPRAYER_ACTIVATION_STAGE ? 0 : printingProgress <= 1F ? ((printingProgress - SPRAYER_ACTIVATION_STAGE) / (1F - SPRAYER_ACTIVATION_STAGE)) : (1F - paintingProgress));

        this.renderPrinter(printerY, matrices, vertices, light, overlay, red, green, blue, alpha);
        this.renderSprayer(sprayerY, true, matrices, vertices, light, overlay, red, green, blue, alpha);

        if (sprayerY < MAST_Y) {
            this.renderSprayer(MAST_Y, false, matrices, vertices, light, overlay, red, green, blue, alpha);
        }
    }

    private void renderSprayer(float y, boolean isFullyVisible, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.sprayerRMast.pivotY = y;
        this.sprayerGMast.pivotY = y;
        this.sprayerBMast.pivotY = y;

        this.sprayerRMast.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.sprayerGMast.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.sprayerBMast.render(matrices, vertices, light, overlay, red, green, blue, alpha);

        if (!isFullyVisible) {
            return;
        }

        y -= 0.5F;
        this.sprayerR.pivotY = y;
        this.sprayerG.pivotY = y;
        this.sprayerB.pivotY = y;

        this.sprayerR.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.sprayerG.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.sprayerB.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    private void renderPrinter(float y, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.printerL.pivotY = y;
        this.printerR.pivotY = y;

        this.printerL.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        this.printerR.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
