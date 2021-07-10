package me.kirantipov.mods.sync.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class AbstractButtonWidget extends AbstractWidget {
    protected final float x;
    protected final float y;
    protected final float width;
    protected final float height;
    private final Consumer<Integer> onClick;

    protected AbstractButtonWidget(float x, float y, float width, float height, Runnable onClick) {
        this(x, y, width, height, onClick == null ? null : i -> onClick.run());
    }

    protected AbstractButtonWidget(float x, float y, float width, float height, Consumer<Integer> onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onClick = onClick;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
    }

    @Override
    protected void onMouseClick(double mouseX, double mouseY, int button) {
        if (this.onClick != null) {
            this.onClick.accept(button);
        }
    }
}