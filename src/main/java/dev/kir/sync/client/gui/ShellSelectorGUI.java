package dev.kir.sync.client.gui;

import dev.kir.sync.client.gl.MSAAFramebuffer;
import dev.kir.sync.client.gui.controller.HudController;
import dev.kir.sync.client.gui.widget.ArrowButtonWidget;
import dev.kir.sync.client.gui.widget.CrossButtonWidget;
import dev.kir.sync.client.gui.widget.PageDisplayWidget;
import dev.kir.sync.client.gui.widget.ShellSelectorButtonWidget;
import dev.kir.sync.util.client.render.ColorUtil;
import dev.kir.sync.api.shell.Shell;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.util.IdentifierUtil;
import dev.kir.sync.util.math.Radians;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

@SuppressWarnings("FieldCanBeLocal")
@Environment(EnvType.CLIENT)
public class ShellSelectorGUI extends Screen {
    private static final int MAX_SLOTS = 8;
    private static final double MENU_RADIUS = 0.3F;
    private static final int BACKGROUND_COLOR = ColorUtil.fromDyeColor(DyeColor.BLACK, 0.3F);
    private static final Text TITLE = new TranslatableText("gui.sync.default.cross_button.title");
    private static final Collection<Text> ARROW_TITLES = List.of(new TranslatableText("gui.sync.shell_selector.up.title"), new TranslatableText("gui.sync.shell_selector.right.title"), new TranslatableText("gui.sync.shell_selector.down.title"), new TranslatableText("gui.sync.shell_selector.left.title"));

    private final Runnable onCloseCallback;
    private final Runnable onRemovedCallback;
    private boolean wasClosed;
    private List<ShellSelectorButtonWidget> shellButtons;
    private List<ArrowButtonWidget> arrowButtons;
    private CrossButtonWidget crossButton;
    private PageDisplayWidget<Identifier, ShellState> pageDisplay;

    public ShellSelectorGUI(Runnable onCloseCallback, Runnable onRemovedCallback) {
        super(TITLE);
        this.onCloseCallback = onCloseCallback;
        this.onRemovedCallback = onRemovedCallback;
    }

    @Override
    public void init() {
        ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);
        Stream<ShellState> data = ((Shell)player).getAvailableShellStates();
        Identifier selectedWorld = player.world.getRegistryKey().getValue();

        this.wasClosed = false;
        this.arrowButtons = createArrowButtons(this.width, this.height, ARROW_TITLES, List.of(this::previousSection, this::nextPage, this::nextSection, this::previousPage));
        this.crossButton = createCrossButton(this.width, this.height, this::onClose);
        this.pageDisplay = createPageDisplay(this.width, this.height, data, selectedWorld, MAX_SLOTS, this::onPageChange);
        Stream.concat(this.arrowButtons.stream(), Stream.of(this.crossButton, this.pageDisplay)).forEach(this::addDrawableChild);

        HudController.hide();
    }

    private static List<ShellSelectorButtonWidget> createShellButtons(int screenWidth, int screenHeight, int count) {
        final double HOLLOW_R = MENU_RADIUS * 0.6;
        final double BORDER_WIDTH = 0.0033;
        final double SECTOR_SPACING = 0.01;

        double cX = screenWidth / 2.0;
        double cY = screenHeight / 2.0;
        double majorR = screenHeight * MENU_RADIUS;
        double minorR = screenHeight * HOLLOW_R;
        double spacing = count > 1 ? SECTOR_SPACING : 0;
        double sector = Radians.R_2_PI / count - spacing;
        double borderWidth = screenHeight * BORDER_WIDTH;
        double pos = -sector / (2 << (count % 2));
        List<ShellSelectorButtonWidget> shellButtons = new ArrayList<>();

        for (int i = 0; i < count; ++i) {
            ShellSelectorButtonWidget button = new ShellSelectorButtonWidget(cX, cY, majorR, minorR, borderWidth, pos, pos + sector);
            pos += sector + spacing;
            shellButtons.add(button);
        }

        return shellButtons;
    }

    private static PageDisplayWidget<Identifier, ShellState> createPageDisplay(int screenWidth, int screenHeight, Stream<ShellState> data, Identifier defaultPage, int entriesPerPage, BiConsumer<PageDisplayWidget<Identifier, ShellState>, PageDisplayWidget<Identifier, ShellState>.Page> onChange) {
        final float FONT_HEIGHT = 1 / 30F;

        float cX = screenWidth / 2F;
        float cY = screenHeight / 2F;
        float scale = screenHeight * FONT_HEIGHT / MinecraftClient.getInstance().textRenderer.fontHeight;

        return new PageDisplayWidget<Identifier, ShellState>(cX, cY, scale, data, ShellState::getWorld, IdentifierUtil::prettifyAsText, defaultPage, entriesPerPage, onChange);
    }

    private static List<ArrowButtonWidget> createArrowButtons(int screenWidth, int screenHeight, Iterable<Text> arrowTitles, Iterable<Runnable> arrowActions) {
        final float ARROW_HEIGHT = 2 / 75F;
        final float ARROW_WIDTH = 57 / 32F;
        final float ARROW_THICKNESS = 1 / 240F;
        final float ARROW_SPACING = 1 / 14F;

        float cX = screenWidth / 2F;
        float cY = screenHeight / 2F;
        float r = screenHeight * (float)MENU_RADIUS * (1F + ARROW_SPACING);
        float arrowHeight = screenHeight * ARROW_HEIGHT;
        float arrowWidth = arrowHeight * ARROW_WIDTH;
        float thickness = screenHeight * ARROW_THICKNESS;
        Iterator<Runnable> actions = arrowActions.iterator();
        Iterator<Text> descriptions = arrowTitles.iterator();
        List<ArrowButtonWidget> arrowButtons = new ArrayList<>();

        for (ArrowButtonWidget.ArrowType arrowType : ArrowButtonWidget.ArrowType.values()) {
            float x;
            float y;
            if (arrowType.isVertical()) {
                x = screenWidth / 2F - arrowWidth / 2F;
                y = cY + r * (arrowType.isDown() ? 1 : -1) + (arrowType.isDown() ? 0 : -arrowHeight);
            } else {
                x = cX + r * (arrowType.isRight() ? 1 : -1) + (arrowType.isRight() ? 0 : -arrowHeight);
                y = screenHeight / 2F - arrowWidth / 2F;
            }
            arrowButtons.add(new ArrowButtonWidget(x, y, arrowWidth, arrowHeight, arrowType, thickness, descriptions.next(), actions.next()));
        }

        return arrowButtons;
    }

    private static CrossButtonWidget createCrossButton(int screenWidth, int screenHeight, Runnable onClose) {
        final float CROSS_MARGIN = 1 / 15F;
        final float CROSS_WIDTH = 2 / 75F;
        final float CROSS_THICKNESS = 1 / 240F;

        float width = screenHeight * CROSS_WIDTH;
        float y = screenHeight * CROSS_MARGIN;
        float x = screenWidth - y - width;
        float thickness = screenHeight * CROSS_THICKNESS;

        // The heck is this inspection?
        // noinspection SuspiciousNameCombination
        return new CrossButtonWidget(x, y, width, width, thickness, onClose);
    }

    @Override
    public void renderBackground(MatrixStack matrices, int vOffset) {
        if (Objects.requireNonNull(this.client).world != null) {
            this.fillGradient(matrices, 0, 0, this.width, this.height, BACKGROUND_COLOR, BACKGROUND_COLOR);
        } else {
            super.renderBackground(matrices, vOffset);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> super.render(matrices, mouseX, mouseY, delta));
        this.renderTooltips(matrices, mouseX, mouseY);
    }

    protected void renderTooltips(MatrixStack matrices, int mouseX, int mouseY) {
        for (Element child : this.children()) {
            if (child instanceof Selectable selectable && selectable.getType() != Selectable.SelectionType.NONE) {
                Text tooltipText = selectable instanceof TooltipProvider tooltipProvider ? tooltipProvider.getTooltip() : null;
                if (tooltipText != null) {
                    this.renderTooltip(matrices, tooltipText, mouseX, mouseY);
                }
                return;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        for (Element child : this.children()) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void onPageChange(PageDisplayWidget<Identifier, ShellState> pageDisplay, PageDisplayWidget<Identifier, ShellState>.Page page) {
        for (ArrowButtonWidget arrow : this.arrowButtons) {
            arrow.visible = arrow.type.isVertical() ? pageDisplay.hasMoreSections() : pageDisplay.hasMorePages();
        }

        if (this.shellButtons != null) {
            this.shellButtons.forEach(this::remove);
        }

        List<ShellState> content = page.content;
        this.shellButtons = createShellButtons(this.width, this.height, Math.max(content.size(), 1));
        this.shellButtons.forEach(this::addDrawableChild);

        for (int i = 0; i < content.size(); ++i) {
            this.shellButtons.get(i).shell = content.get(i);
        }
    }

    private void nextSection() {
        this.pageDisplay.nextSection();
    }

    private void previousSection() {
        this.pageDisplay.previousSection();
    }

    private void nextPage() {
        this.pageDisplay.nextPage();
    }

    private void previousPage() {
        this.pageDisplay.previousPage();
    }

    @Override
    public void onClose() {
        HudController.restore();
        if (this.onCloseCallback != null) {
            this.onCloseCallback.run();
        }
        this.wasClosed = true;
        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
        if (!this.wasClosed && this.onRemovedCallback != null) {
            this.onRemovedCallback.run();
        }
    }
}
