package bai.god.shen.ui;

import bai.god.shen.ShenLaw;
import bai.god.shen.ShenNetwork;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ShenImportScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget inputField;

    public ShenImportScreen(Screen parent) {
        super(Text.literal("导入法则"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int fieldW = Math.min(this.width - 40, 500);
        int fieldX = centerX - fieldW / 2;
        int fieldY = this.height / 2 - 30;

        inputField = new TextFieldWidget(this.textRenderer, fieldX, fieldY, fieldW, 20, Text.literal(""));
        inputField.setMaxLength(Integer.MAX_VALUE);
        inputField.setTextPredicate(s -> true);
        this.addDrawableChild(inputField);
        this.setFocused(inputField);

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a导入"),
            btn -> {
                String text = inputField.getText().trim();
                if (text.isEmpty()) {
                    if (this.client != null && this.client.player != null)
                        this.client.player.sendMessage(Text.literal("§c[言出法随] 输入内容为空"), false);
                    return;
                }
                try {
                    ShenLaw imported = ShenNetwork.GSON.fromJson(text, ShenLaw.class);
                    if (imported == null || imported.name == null || imported.name.isEmpty()) {
                        if (this.client != null && this.client.player != null)
                            this.client.player.sendMessage(Text.literal("§c[言出法随] 无效的法则数据"), false);
                        return;
                    }
                    ShenNetwork.sendSaveLaw(imported);
                    if (this.client != null && this.client.player != null)
                        this.client.player.sendMessage(Text.literal("§a[言出法随] §e导入 §b" + imported.name + " §e成功"), false);
                    if (this.client != null) this.client.setScreen(parent);
                } catch (Exception e) {
                    if (this.client != null && this.client.player != null)
                        this.client.player.sendMessage(Text.literal("§c[言出法随] 导入失败: " + e.getMessage()), false);
                }
            }
        ).dimensions(centerX - 84, fieldY + 30, 80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§7取消"),
            btn -> {
                if (this.client != null) this.client.setScreen(parent);
            }
        ).dimensions(centerX + 4, fieldY + 30, 80, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§d导入法则 - 粘贴JSON数据"), centerX, this.height / 2 - 50, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client != null && hasControlDown() && keyCode == 86) {
            String clipboard = this.client.keyboard.getClipboard();
            if (clipboard != null && inputField != null) {
                String text = inputField.getText();
                int cursor = Math.min(inputField.getCursor(), text.length());
                String newText = text.substring(0, cursor) + clipboard + text.substring(cursor);
                if (newText.length() <= Integer.MAX_VALUE) {
                    inputField.setText(newText);
                    inputField.setCursor(cursor + clipboard.length());
                }
            }
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;

        if (this.client != null && hasControlDown()) {
            if (keyCode == 65) {
                if (inputField != null) {
                    inputField.setSelectionStart(0);
                    inputField.setSelectionEnd(inputField.getText().length());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.getFocused() == inputField) {
            String text = inputField.getText();
            if (text.length() >= Integer.MAX_VALUE) return false;
            int cursor = Math.min(inputField.getCursor(), text.length());
            String newText = text.substring(0, cursor) + chr + text.substring(cursor);
            inputField.setText(newText);
            inputField.setCursor(cursor + 1);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}