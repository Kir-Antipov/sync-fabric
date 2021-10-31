package dev.kir.sync.client.gui.widget;

import dev.kir.sync.util.client.render.ColorUtil;
import dev.kir.sync.util.client.render.RenderSystemUtil;
import dev.kir.sync.util.math.Radians;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PageDisplayWidget<TKey, UData> extends AbstractWidget {
    private static final int DEFAULT_TEXT_COLOR = ColorUtil.fromDyeColor(DyeColor.WHITE);
    private static final int DEFAULT_BACKGROUND_COLOR = ColorUtil.fromDyeColor(DyeColor.BLACK, 0.6F);
    private static final float DEFAULT_SCALE = 1F;
    private static final float DEFAULT_STEP = Radians.R_PI_32;

    private final float cX;
    private final float cY;
    private final float scale;
    private final float step;
    private final int textColor;
    private final float[] backgroundColor;
    private final Map<TKey, List<UData>> data;
    private final List<TKey> keys;
    private final Function<TKey, Text> keyTransformer;
    private final int entriesPerPage;
    private final BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange;

    private int selectedKeyIndex;
    private int selectedPageIndex;
    private Text selectedKeyAsText;
    private int selectedSectionPageCount;

    public PageDisplayWidget(float cX, float cY, Stream<UData> data, Function<UData, TKey> keySelector, Function<TKey, Text> keyTransformer, TKey defaultKey, int entriesPerPage, BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this(cX, cY, DEFAULT_SCALE, DEFAULT_STEP, DEFAULT_TEXT_COLOR, DEFAULT_BACKGROUND_COLOR, data, keySelector, keyTransformer, defaultKey, entriesPerPage, onSelectionChange);
    }

    public PageDisplayWidget(float cX, float cY, float scale, Stream<UData> data, Function<UData, TKey> keySelector, Function<TKey, Text> keyTransformer, TKey defaultKey, int entriesPerPage, BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this(cX, cY, scale, DEFAULT_STEP, DEFAULT_TEXT_COLOR, DEFAULT_BACKGROUND_COLOR, data, keySelector, keyTransformer, defaultKey, entriesPerPage, onSelectionChange);
    }

    public PageDisplayWidget(float cX, float cY, int textColor, int backgroundColor, Stream<UData> data, Function<UData, TKey> keySelector, Function<TKey, Text> keyTransformer, TKey defaultKey, int entriesPerPage, BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this(cX, cY, DEFAULT_SCALE, DEFAULT_STEP, textColor, backgroundColor, data, keySelector, keyTransformer, defaultKey, entriesPerPage, onSelectionChange);
    }

    public PageDisplayWidget(float cX, float cY, float scale, int textColor, int backgroundColor, Stream<UData> data, Function<UData, TKey> keySelector, Function<TKey, Text> keyTransformer, TKey defaultKey, int entriesPerPage, BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this(cX, cY, scale, DEFAULT_STEP, textColor, backgroundColor, data, keySelector, keyTransformer, defaultKey, entriesPerPage, onSelectionChange);
    }

    public PageDisplayWidget(float cX, float cY, float scale, float step, int textColor, int backgroundColor, Stream<UData> data, Function<UData, TKey> keySelector, Function<TKey, Text> keyTransformer, TKey defaultKey, int entriesPerPage, BiConsumer<PageDisplayWidget<TKey, UData>, Page> onSelectionChange) {
        this.cX = cX;
        this.cY = cY;
        this.scale = scale;
        this.step = step;
        this.textColor = textColor;
        this.backgroundColor = ColorUtil.toRGBA(backgroundColor);
        this.data = data.collect(Collectors.groupingBy(keySelector, HashMap::new, Collectors.toList()));
        this.keys = new ArrayList<>(this.data.keySet());
        this.keyTransformer = keyTransformer;
        this.entriesPerPage = entriesPerPage;
        this.onSelectionChange = onSelectionChange;

        defaultKey = defaultKey == null ? this.keys.stream().findFirst().orElseThrow() : defaultKey;
        if (!this.data.containsKey(defaultKey)) {
            this.data.put(defaultKey, List.of());
            this.keys.add(defaultKey);
        }

        this.select(defaultKey, 0);
    }

    public void select(int section, int page) {
        section = MathHelper.clamp((section + this.keys.size()) % this.keys.size(), 0, this.keys.size() - 1);
        TKey key = this.keys.get(section);
        int pages = Math.max(1, (int)Math.ceil(this.data.get(key).size() / (double)this.entriesPerPage));
        page = MathHelper.clamp((page + pages) % pages, 0, pages - 1);

        List<UData> content = this.data.get(key).stream().skip((long)this.entriesPerPage * page).limit(this.entriesPerPage).collect(Collectors.toList());
        this.selectedKeyIndex = section;
        this.selectedPageIndex = page;
        this.selectedKeyAsText = this.keyTransformer.apply(key);
        this.selectedSectionPageCount = pages;

        if (this.onSelectionChange != null) {
            this.onSelectionChange.accept(this, new Page(key, section, page, pages, content));
        }
    }

    public void select(TKey sectionKey, int page) {
        this.select(this.keys.indexOf(sectionKey), page);
    }

    public boolean hasMoreSections() {
        return this.keys.size() > 1;
    }

    public boolean hasMorePages() {
        return this.selectedSectionPageCount > 1;
    }

    public void nextSection() {
        this.select(this.selectedKeyIndex + 1, 0);
    }

    public void previousSection() {
        this.select(this.selectedKeyIndex - 1, 0);
    }

    public void nextPage() {
        this.select(this.selectedKeyIndex, this.selectedPageIndex + 1);
    }

    public void previousPage() {
        this.select(this.selectedKeyIndex, this.selectedPageIndex - 1);
    }

    public void moveSelection(int dSection, int dPage) {
        this.select(this.selectedKeyIndex + dSection, this.selectedPageIndex + dPage);
    }

    @Override
    protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.hasMorePages()) {
            this.renderTitleAndPagination(matrices);
        } else {
            this.renderTitle(matrices);
        }
    }

    private void renderTitle(MatrixStack matrices) {
        RenderSystemUtil.drawCenteredText(this.selectedKeyAsText, matrices, this.cX, this.cY, this.scale, this.textColor, true);
    }

    private void renderTitleAndPagination(MatrixStack matrices) {
        String paginationText = String.format("%s / %s", this.selectedPageIndex + 1, this.selectedSectionPageCount);
        TextRenderer textRenderer = RenderSystemUtil.getTextRenderer();
        float paginationTextScale = 0.5F;
        float titleHeight = textRenderer.fontHeight * this.scale;
        float spacing = titleHeight * 5F / 6F;
        float paginationTextWidth = textRenderer.getWidth(paginationText) * this.scale * paginationTextScale;
        float paginationBoxWidth = Math.max(titleHeight * 2F, paginationTextWidth);
        float height = 2 * titleHeight + spacing;

        float top = this.cY - height / 2F;
        float boxTop = top + titleHeight + spacing - titleHeight * paginationTextScale * 0.125F;
        float boxLeft = this.cX - paginationBoxWidth / 2F;
        RenderSystemUtil.drawRectangle(matrices, boxLeft, boxTop, paginationBoxWidth / this.scale, titleHeight / this.scale, titleHeight * 0.25F / this.scale, this.scale, 0, this.step, backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);

        RenderSystemUtil.drawCenteredText(this.selectedKeyAsText, matrices, this.cX, top + titleHeight, this.scale, this.textColor, true);
        RenderSystemUtil.drawCenteredText(Text.of(paginationText), matrices, this.cX, top + 1.5F * titleHeight + spacing, this.scale * paginationTextScale, this.textColor, false);
    }

    public class Page {
        public final TKey key;
        public final int sectionIndex;
        public final int pageIndex;
        public final int pageCount;
        public final List<UData> content;

        private Page(TKey key, int sectionIndex, int pageIndex, int pageCount, List<UData> content) {
            this.key = key;
            this.sectionIndex = sectionIndex;
            this.pageIndex = pageIndex;
            this.pageCount = pageCount;
            this.content = content;
        }
    }
}