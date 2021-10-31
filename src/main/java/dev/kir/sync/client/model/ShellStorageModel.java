package dev.kir.sync.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;

@Environment(EnvType.CLIENT)
public class ShellStorageModel extends AbstractShellContainerModel {
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

    public final ModelPart feetRetainerF;
    public final ModelPart feetRetainerL;
    public final ModelPart feetRetainerR;
    public final ModelPart feetRetainerB;
    public final ModelPart feetRetainerSupportR;
    public final ModelPart feetRetainerSupportL;

    public final ModelPart shoulderRetainerL;
    public final ModelPart shoulderRetainerLT;
    public final ModelPart shoulderRetainerR;
    public final ModelPart shoulderRetainerRT;

    public final ModelPart headRetainerT;
    public final ModelPart headRetainerL;
    public final ModelPart headRetainerR;
    public final ModelPart headRetainerB;
    public final ModelPart headConnector;

    public final ModelPart wallLU;
    public final ModelPart wallLL;
    public final ModelPart wallRU;
    public final ModelPart wallRL;
    public final ModelPart wallBU;
    public final ModelPart wallBL;

    public final ModelPart pillarFRU;
    public final ModelPart pillarFRL;
    public final ModelPart pillarFLU;
    public final ModelPart pillarFLL;
    public final ModelPart pillarBRU;
    public final ModelPart pillarBRL;
    public final ModelPart pillarBLU;
    public final ModelPart pillarBLL;
    public final ModelPart pillarBU1;
    public final ModelPart pillarBU2;
    public final ModelPart pillarBU3;
    public final ModelPart pillarBU4;
    public final ModelPart pillarBL1;
    public final ModelPart pillarBL2;
    public final ModelPart pillarBL3;
    public final ModelPart pillarBL4;

    public final ModelPart ledT;
    public final ModelPart ledL;
    public final ModelPart ledR;
    public final ModelPart ledB;
    public final ModelPart ledLight;
    public DyeColor ledColor;

    public float connectorProgress;

    public ShellStorageModel() {
        ModelPart ledTemplate = this.createRotationTemplate(-0.4833219F, 0, 0);

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

        this.feetRetainerF = this.addCuboid(BOTTOM, 70, 100, -9, 16, -4.75F, 18, 3, 1);
        this.feetRetainerB = this.addCuboid(BOTTOM, 70, 100, -9, 16, 4.25F, 18, 3, 1);
        this.feetRetainerR = this.addCuboid(BOTTOM, 70, 104, -9, 16, -3.75F, 1, 3, 8);
        this.feetRetainerL = this.addCuboid(BOTTOM, 70, 104, 8, 16, -3.75F, 1, 3, 8);
        this.feetRetainerSupportR = this.addCuboid(BOTTOM, 70, 115, -9F, 16.75F, -0.75F, 1, 9, 2);
        this.feetRetainerSupportL = this.addCuboid(BOTTOM, 70, 115, 8F, 16.75F, -0.75F, 1, 9, 2);

        this.shoulderRetainerL = this.addCuboid(TOP, 70, 107, 6.5F, 16, -5, 1, 1, 19);
        this.shoulderRetainerLT = this.addCuboid(TOP, 78, 115, 6.5F, 13, -5, 1, 4, 1);
        this.shoulderRetainerR = this.addCuboid(TOP, 70, 107, -7.5F, 16, -5, 1, 1, 19);
        this.shoulderRetainerRT = this.addCuboid(TOP, 78, 115, -7.5F, 13, -5, 1, 4, 1);

        this.headRetainerL = this.addCuboid(TOP, 70, 127, 0.5F, 3, 9, 1, 1, 6);
        this.headRetainerT = this.addCuboid(TOP, 70, 134, -1.5F, 2, 9, 3, 1, 6);
        this.headRetainerB = this.addCuboid(TOP, 70, 134, -1.5F, 4, 9, 3, 1, 6);
        this.headRetainerR = this.addCuboid(TOP, 70, 127, -1.5F, 3, 9, 1, 1, 6);
        this.headConnector = this.addCuboid(TOP, 70, 141, -0.5F, 3, 10, 1, 1, 5);

        this.wallLL = this.addCuboid(BOTTOM, 200, 122, 15.05F, -8, -14, 1, 30, 27);
        this.wallLU = this.addCuboid(TOP, 200, 66, 15.05F, -5, -14, 1, 29, 27);

        this.wallRL = this.addCuboid(BOTTOM, 200, 122, -16.05F, -8, -14, 1, 30, 27);
        this.wallRU = this.addCuboid(TOP, 200, 66, -16.05F, -5, -14, 1, 29, 27);

        this.wallBL = this.addCuboid(BOTTOM, 96, 161, -13, -8, 14, 26, 30, 2);
        this.wallBU = this.addCuboid(TOP, 96, 128, -13, -5, 14, 26, 29, 2);

        this.pillarFRL = this.addCuboid(BOTTOM, 60, 68, -16, -8, -15, 1, 31, 1);
        this.pillarFRU = this.addCuboid(TOP, 60, 38, -16, -6, -15, 1, 30, 1);

        this.pillarFLL = this.addCuboid(BOTTOM, 60, 68, 15, -8, -15, 1, 31, 1);
        this.pillarFLU = this.addCuboid(TOP, 60, 38, 15, -6, -15, 1, 30, 1);

        this.pillarBRL = this.addCuboid(BOTTOM, 0, 155, -16, -8, 13, 3, 30, 3);
        this.pillarBRU = this.addCuboid(TOP, 0, 126, -16, -5, 13, 3, 29, 3);

        this.pillarBLL = this.addCuboid(BOTTOM, 0, 155, 13, -8, 13, 3, 30, 3);
        this.pillarBLU = this.addCuboid(TOP, 0, 126, 13, -5, 13, 3, 29, 3);

        this.pillarBL1 = this.addCuboid(BOTTOM, 88, 157, -10.5F, -8, 13, 3, 30, 1);
        this.pillarBU1 = this.addCuboid(TOP, 88, 128, -10.5F, -5, 13, 3, 29, 1);

        this.pillarBL2 = this.addCuboid(BOTTOM, 88, 157, -4.5F, -8, 13, 3, 30, 1);
        this.pillarBU2 = this.addCuboid(TOP, 88, 128, -4.5F, -5, 13, 3, 29, 1);

        this.pillarBL3 = this.addCuboid(BOTTOM, 88, 157, 1.5F, -8, 13, 3, 30, 1);
        this.pillarBU3 = this.addCuboid(TOP, 88, 128, 1.5F, -5, 13, 3, 29, 1);

        this.pillarBL4 = this.addCuboid(BOTTOM, 88, 157, 7.5F, -8, 13, 3, 30, 1);
        this.pillarBU4 = this.addCuboid(TOP, 88, 128, 7.5F, -5, 13, 3, 29, 1);

        this.ledB = this.addCuboid(BOTTOM, 0, 0, 8, 21.5F, 9.335F, 5, 1, 1, ledTemplate);
        this.ledT = this.addCuboid(BOTTOM, 0, 0, 8, 16.2F, 12.12F, 5, 1, 1, ledTemplate);
        this.ledR = this.addCuboid(BOTTOM, 0, 0, 8, 17, 11.7F, 1, 6, 1, ledTemplate);
        this.ledL = this.addCuboid(BOTTOM, 0, 0, 12, 17, 11.7F, 1, 6, 1, ledTemplate);
        this.ledLight = this.createCuboid(0, 9, 8, 17.15F, 11.8F, 5, 8, 1, ledTemplate);
        this.ledColor = DyeColor.RED;
    }

    @Override
    public void render(DoubleBlockProperties.Type type, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.headConnector.pivotZ = 10 - 5 * this.connectorProgress;
        super.render(type, matrices, vertices, light, overlay, red, green, blue, alpha);
        if (type == BOTTOM) {
            float[] rgb = this.ledColor.getColorComponents();
            this.ledLight.render(matrices, vertices, light, overlay, rgb[0], rgb[1], rgb[2], 1F);
        }
    }
}
