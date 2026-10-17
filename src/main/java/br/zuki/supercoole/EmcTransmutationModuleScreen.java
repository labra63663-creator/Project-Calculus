package br.zuki.supercoole;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.Locale;

public class EmcTransmutationModuleScreen extends AbstractContainerScreen<EmcTransmutationModuleMenu> {
    public EmcTransmutationModuleScreen(EmcTransmutationModuleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = EmcTransmutationModuleMenu.IMAGE_WIDTH;
        this.imageHeight = EmcTransmutationModuleMenu.IMAGE_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int topX = x + EmcTransmutationModuleMenu.TOP_PANEL_OFFSET_X;
        int topY = y;
        int invX = x;
        int invY = y + EmcTransmutationModuleMenu.TOP_PANEL_HEIGHT + 8;

        // Top panel background
        g.fill(topX, topY, topX + EmcTransmutationModuleMenu.TOP_PANEL_WIDTH, topY + EmcTransmutationModuleMenu.TOP_PANEL_HEIGHT, 0xFF1E1E1E);
        g.fill(topX, topY, topX + EmcTransmutationModuleMenu.TOP_PANEL_WIDTH, topY + 1, 0xFF5A5A5A);
        g.fill(topX, topY + EmcTransmutationModuleMenu.TOP_PANEL_HEIGHT - 1, topX + EmcTransmutationModuleMenu.TOP_PANEL_WIDTH, topY + EmcTransmutationModuleMenu.TOP_PANEL_HEIGHT, 0xFF5A5A5A);
        g.fill(topX, topY, topX + 1, topY + EmcTransmutationModuleMenu.TOP_PANEL_HEIGHT, 0xFF5A5A5A);
        g.fill(topX + EmcTransmutationModuleMenu.TOP_PANEL_WIDTH - 1, topY, topX + EmcTransmutationModuleMenu.TOP_PANEL_WIDTH, topY + EmcTransmutationModuleMenu.TOP_PANEL_HEIGHT, 0xFF5A5A5A);

        // Inventory panel background
        g.fill(invX, invY, invX + EmcTransmutationModuleMenu.INV_PANEL_WIDTH, invY + EmcTransmutationModuleMenu.INV_PANEL_HEIGHT, 0xFF1E1E1E);
        g.fill(invX, invY, invX + EmcTransmutationModuleMenu.INV_PANEL_WIDTH, invY + 1, 0xFF5A5A5A);
        g.fill(invX, invY + EmcTransmutationModuleMenu.INV_PANEL_HEIGHT - 1, invX + EmcTransmutationModuleMenu.INV_PANEL_WIDTH, invY + EmcTransmutationModuleMenu.INV_PANEL_HEIGHT, 0xFF5A5A5A);
        g.fill(invX, invY, invX + 1, invY + EmcTransmutationModuleMenu.INV_PANEL_HEIGHT, 0xFF5A5A5A);
        g.fill(invX + EmcTransmutationModuleMenu.INV_PANEL_WIDTH - 1, invY, invX + EmcTransmutationModuleMenu.INV_PANEL_WIDTH, invY + EmcTransmutationModuleMenu.INV_PANEL_HEIGHT, 0xFF5A5A5A);

        // Player inventory slot backgrounds
        int invStartX = invX + 8;
        int invStartY = invY + 4;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int px = invStartX + col * 18;
                int py = invStartY + row * 18;
                g.fill(px - 1, py - 1, px + 17, py + 17, 0xFF3A3A3A);
                g.fill(px, py, px + 16, py + 16, 0xFF101010);
            }
        }
        for (int col = 0; col < 9; col++) {
            int px = invStartX + col * 18;
            int py = invStartY + 3 * 18 + 4;
            g.fill(px - 1, py - 1, px + 17, py + 17, 0xFF3A3A3A);
            g.fill(px, py, px + 16, py + 16, 0xFF101010);
        }

        // Slot background (centered in top panel)
        int sx = x + EmcTransmutationModuleMenu.SLOT_X;
        int sy = y + EmcTransmutationModuleMenu.SLOT_Y;
        g.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF3A3A3A);
        g.fill(sx, sy, sx + 16, sy + 16, 0xFF101010);

        long emc = this.menu.getEmc();
        g.drawString(this.font, "EMC: " + formatEmc(emc), topX + 8, topY + 70, 0xE0E0E0, false);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, EmcTransmutationModuleMenu.TOP_PANEL_OFFSET_X + 8, 6, 0xE0E0E0, false);
        // EMC text drawn in renderBg area
    }


    @Override
    public void render(net.minecraft.client.gui.GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        long emc = this.menu.getEmc();
        int topX = this.leftPos + EmcTransmutationModuleMenu.TOP_PANEL_OFFSET_X;
        int topY = this.topPos;
        String label = "EMC: " + formatEmc(emc);
        int textX = topX + 8;
        int textY = topY + 70;
        int textW = this.font.width(label);
        if (mouseX >= textX && mouseX <= textX + textW && mouseY >= textY && mouseY <= textY + this.font.lineHeight) {
            g.renderTooltip(this.font, Component.literal(formatAbsolute(emc)), mouseX, mouseY);
        }
    }

    private static String formatEmc(long value) {
        if (value < 5000) {
            return String.format(Locale.GERMANY, "%,d", value);
        }
        long[] divisors = {1_000_000_000_000L, 1_000_000_000L, 1_000_000L, 1_000L};
        String[] suffixes = {"T", "B", "M", "K"};
        for (int i = 0; i < divisors.length; i++) {
            if (value >= divisors[i]) {
                double scaled = (double) value / divisors[i];
                if (scaled >= 10 || scaled == Math.floor(scaled)) {
                    return String.format(Locale.US, "%.0f%s", scaled, suffixes[i]);
                }
                return String.format(Locale.US, "%.1f%s", scaled, suffixes[i]);
            }
        }
        return String.valueOf(value);
    }


    private static String formatAbsolute(long value) {
        return String.format(java.util.Locale.GERMANY, "%,d", value);
    }
}
