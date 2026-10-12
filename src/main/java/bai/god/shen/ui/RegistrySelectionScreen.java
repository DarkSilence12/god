package bai.god.shen.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 通用选择全屏页面
 */
public class RegistrySelectionScreen extends Screen {
    private final Screen parent;
    private final ShenParamHelper.ParamDef def;
    private final String currentValue;
    private final Consumer<String> callback;
    private final String[][] allOptions;

    private TextFieldWidget searchInput;
    private String searchText = "";
    private int scrollOffset = 0;

    private final List<String[]> filteredOptions = new ArrayList<>();

    public RegistrySelectionScreen(Screen parent, ShenParamHelper.ParamDef def,
                                   String currentValue, Consumer<String> callback) {
        super(Text.literal("选择 " + def.label));
        this.parent = parent;
        this.def = def;
        this.currentValue = currentValue;
        this.callback = callback;
        String[][] builtOptions;
        if (def.registryType != null) {
            builtOptions = ShenParamHelper.buildRegistryOptions(def.registryType);
            if (def.registryType == ShenParamHelper.RegistryType.DIMENSION) {
                String[][] dynamicOptions = tryLoadDimensionOptions();
                if (dynamicOptions != null && dynamicOptions.length > 0) {
                    builtOptions = dynamicOptions;
                }
            } else if (def.registryType == ShenParamHelper.RegistryType.DAMAGE_TYPE) {
                String[][] dynamicOptions = tryLoadDamageTypeOptions();
                if (dynamicOptions != null && dynamicOptions.length > 0) {
                    builtOptions = dynamicOptions;
                }
            }
        } else if (def.options != null) {
            builtOptions = def.options;
        } else {
            builtOptions = new String[0][0];
        }
        this.allOptions = builtOptions;
        updateFilteredOptions();
    }

    private String[][] tryLoadDimensionOptions() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return null;
        try {
            DynamicRegistryManager registryManager = client.world.getRegistryManager();
            return ShenParamHelper.buildDimensionOptionsFromRegistry(registryManager);
        } catch (Exception e) {
            return null;
        }
    }

    private String[][] tryLoadDamageTypeOptions() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return null;
        try {
            DynamicRegistryManager registryManager = client.world.getRegistryManager();
            return ShenParamHelper.buildDamageTypeOptionsFromRegistry(registryManager);
        } catch (Exception e) {
            return null;
        }
    }

    public RegistrySelectionScreen(Screen parent, String title, String[][] options,
                                   String currentValue, Consumer<String> callback) {
        super(Text.literal("选择 " + title));
        this.parent = parent;
        this.def = null;
        this.currentValue = currentValue;
        this.callback = callback;
        this.allOptions = options;
        updateFilteredOptions();
    }

    private void updateFilteredOptions() {
        filteredOptions.clear();
        String search = searchText.toLowerCase().trim();
        for (String[] opt : allOptions) {
            if (search.isEmpty() ||
                opt[0].toLowerCase().contains(search) ||
                opt[1].toLowerCase().contains(search)) {
                filteredOptions.add(opt);
            }
        }
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c← 返回"),
            button -> {
                if (this.client != null) this.client.setScreen(parent);
            }
        ).dimensions(10, 8, 60, 20).build());

        String titleLabel = (def != null) ? def.label : "选项";
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§6§l选择 " + titleLabel),
            b -> {}
        ).dimensions(80, 8, 200, 20).build());

        int totalCount = allOptions.length;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§7共 " + totalCount + " 项"),
            b -> {}
        ).dimensions(this.width - 120, 8, 110, 20).build());

        if (searchInput == null) {
            searchInput = new TextFieldWidget(this.textRenderer,
                10, 36, this.width - 20, 20, Text.literal("搜索中文名或ID..."));
            searchInput.setMaxLength(64);
            searchInput.setText(searchText);
            searchInput.setChangedListener(text -> {
                searchText = text;
                scrollOffset = 0;
                updateFilteredOptions();
            });
        }
        this.addDrawableChild(searchInput);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        int startY = 65;
        int itemHeight = 24;
        int availableHeight = this.height - startY - 50;
        int maxVisible = Math.max(1, availableHeight / itemHeight);

        int end = Math.min(scrollOffset + maxVisible, filteredOptions.size());
        int actualListHeight = (end - scrollOffset) * itemHeight;
        context.fill(8, startY - 2, this.width - 8, startY + actualListHeight + 2, 0x33000000);

        for (int i = scrollOffset; i < end; i++) {
            String[] opt = filteredOptions.get(i);
            boolean selected = opt[1].equals(currentValue);
            int y = startY + (i - scrollOffset) * itemHeight;

            boolean hovered = mouseX >= 10 && mouseX <= this.width - 10 && mouseY >= y && mouseY <= y + itemHeight - 1;

            int bgColor = selected ? 0x4400AA00 : (hovered ? 0x44555555 : 0x00000000);
            if (bgColor != 0) {
                context.fill(10, y, this.width - 10, y + itemHeight - 1, bgColor);
            }

            String numStr = String.format("%-" + String.valueOf(filteredOptions.size()).length() + "d", i + 1);
            String prefix = selected ? "§a▶ " : (hovered ? "§e→ " : "  ");
            String display = prefix + "§7" + numStr + " §f" + opt[0];
            String idHint = " §8(" + opt[1] + ")";

            context.drawText(this.textRenderer,
                Text.literal(display),
                15, y + 5, 0xFFFFFF, true);

            int idWidth = this.textRenderer.getWidth(idHint);
            context.drawText(this.textRenderer,
                Text.literal(idHint),
                this.width - 15 - idWidth, y + 5, 0x888888, false);
        }

        if (scrollOffset > 0) {
            String upText = "▲ 还有 " + scrollOffset + " 项 (滚轮向上)";
            int tw = this.textRenderer.getWidth(upText);
            context.drawText(this.textRenderer, upText,
                (this.width - tw) / 2, startY - 12, 0x888888, false);
        }
        if (end < filteredOptions.size()) {
            String downText = "▼ 还有 " + (filteredOptions.size() - end) + " 项 (滚轮向下)";
            int tw = this.textRenderer.getWidth(downText);
            context.drawText(this.textRenderer, downText,
                (this.width - tw) / 2, startY + actualListHeight + 6, 0x888888, false);
        }

        if (filteredOptions.isEmpty() && !searchText.isEmpty()) {
            Text msg = Text.literal("§c未找到匹配 \"" + searchText + "\"");
            context.drawText(this.textRenderer, msg,
                this.width / 2 - this.textRenderer.getWidth(msg) / 2,
                startY + 10, 0xFFFFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int startY = 65;
        int itemHeight = 24;
        int availableHeight = this.height - startY - 50;
        int maxVisible = Math.max(1, availableHeight / itemHeight);

        int end = Math.min(scrollOffset + maxVisible, filteredOptions.size());
        for (int i = scrollOffset; i < end; i++) {
            int y = startY + (i - scrollOffset) * itemHeight;
            int yEnd = y + itemHeight - 1;

            if (mouseX >= 10 && mouseX <= this.width - 10 && mouseY >= y && mouseY <= yEnd) {
                String selectedValue = filteredOptions.get(i)[1];
                callback.accept(selectedValue);
                if (this.client != null) this.client.setScreen(parent);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client != null && hasControlDown()) {
            var focused = this.getFocused();
            if (focused instanceof TextFieldWidget tf) {
                if (keyCode == 86) {
                    String clipboard = this.client.keyboard.getClipboard();
                    if (clipboard != null) {
                        pasteIntoTextField(tf, clipboard);
                    }
                    return true;
                } else if (keyCode == 67) {
                    String selected = tf.getSelectedText();
                    if (selected != null && !selected.isEmpty()) {
                        this.client.keyboard.setClipboard(selected);
                    }
                    return true;
                } else if (keyCode == 88) {
                    String selected = tf.getSelectedText();
                    if (selected != null && !selected.isEmpty()) {
                        this.client.keyboard.setClipboard(selected);
                        pasteIntoTextField(tf, "");
                    }
                    return true;
                } else if (keyCode == 65) {
                    tf.setSelectionStart(0);
                    tf.setSelectionEnd(tf.getText().length());
                    return true;
                }
            }
        }

        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return false;
    }

    private void pasteIntoTextField(TextFieldWidget tf, String content) {
        tf.write(content);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int startY = 65;
        int itemHeight = 24;
        int availableHeight = this.height - startY - 50;
        int maxVisible = Math.max(1, availableHeight / itemHeight);

        int oldOffset = scrollOffset;
        scrollOffset += (int) amount;
        scrollOffset = Math.max(0, Math.min(filteredOptions.size() - maxVisible, scrollOffset));

        return oldOffset != scrollOffset;
    }
}