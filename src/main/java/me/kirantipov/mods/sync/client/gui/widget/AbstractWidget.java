package me.kirantipov.mods.sync.client.gui.widget;

import me.kirantipov.mods.sync.client.gui.TooltipProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Date;

@Environment(EnvType.CLIENT)
public abstract class AbstractWidget implements Drawable, Selectable, TooltipProvider, Element {
    public boolean visible = true;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private double lastMouseX = -1;
    private double lastMouseY = -1;
    private long lastMovementTime = 0;

    public boolean isHovered() {
        return this.isHovered;
    }

    public boolean isPressed() {
        return this.isPressed;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.mouseMoved(mouseX, mouseY);
            this.renderContent(matrices, mouseX, mouseY, delta);
        }
    }

    protected abstract void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta);

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.isHovered = this.visible && this.isMouseOver(mouseX, mouseY);
        this.isPressed &= this.isHovered;
        if (!this.isHovered || this.lastMouseX != mouseX || this.lastMouseY != mouseY) {
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            this.lastMovementTime = new Date().getTime();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.visible && this.isHovered) {
            this.isPressed = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.visible && this.isPressed) {
            this.onMouseClick(mouseX, mouseY, button);
            this.isPressed = false;
            return true;
        }
        return false;
    }

    protected void onMouseClick(double mouseX, double mouseY, int button) { }

    @Override
    public SelectionType getType() {
        return this.isPressed() ? SelectionType.FOCUSED : this.isHovered() ? SelectionType.HOVERED : SelectionType.NONE;
    }

    @Override
    public Text getTooltip() {
        if (this.isHovered && (new Date().getTime() - this.lastMovementTime) >= this.getTooltipDelay()) {
            return this.getWidgetDescription();
        }

        return null;
    }

    protected long getTooltipDelay() {
        return 500;
    }

    protected Text getWidgetDescription() {
        return null;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        Text description = this.getWidgetDescription();
        if (description != null) {
            builder.put(NarrationPart.TITLE, description);
        }
    }
}
