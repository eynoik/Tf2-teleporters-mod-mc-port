package com.eynoik.tf2teleporter.client;

import com.eynoik.tf2teleporter.TF2TeleporterMod;
import com.eynoik.tf2teleporter.menu.TeleporterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class TeleporterScreen extends AbstractContainerScreen<TeleporterMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TF2TeleporterMod.MOD_ID, "textures/gui/gui_teleporter.png");
    private static final int TEXT_COLOR = 0x404040;

    public TeleporterScreen(TeleporterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addLegacyButton("<<", leftPos + 34, topPos + 43, TeleporterMenu.BUTTON_MINUS_TEN);
        addLegacyButton("<", leftPos + 56, topPos + 43, TeleporterMenu.BUTTON_MINUS_ONE);
        addLegacyButton(">", leftPos + 100, topPos + 43, TeleporterMenu.BUTTON_PLUS_ONE);
        addLegacyButton(">>", leftPos + 122, topPos + 43, TeleporterMenu.BUTTON_PLUS_TEN);
    }

    private void addLegacyButton(String text, int x, int y, int buttonId) {
        addRenderableWidget(Button.builder(Component.literal(text), button -> sendButton(buttonId)).bounds(x, y, 20, 20).build());
    }

    private void sendButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        drawCentered(graphics, Component.literal("TF2 Teleporter"), 8);
        drawCentered(graphics, Component.translatable("gui.tf2teleporter.frequency"), 23);
        drawCentered(graphics, Component.literal(Integer.toString(menu.getSelectedFrequency())), 49);
        if (!menu.isFrequencyAvailable()) drawCentered(graphics, Component.translatable("gui.tf2teleporter.inUse"), 68);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int y) {
        graphics.drawString(font, text, (imageWidth - font.width(text)) / 2, y, TEXT_COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
