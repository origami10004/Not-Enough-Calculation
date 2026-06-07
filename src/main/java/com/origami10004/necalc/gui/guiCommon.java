package com.origami10004.necalc.gui;

import net.minecraft.client.gui.GuiScreen;

abstract class guiCommon extends GuiScreen {
    protected static void drawSolidPanel(int x, int y, int w, int h, int fill) {
        drawRect(x,     y,     x + w, y + h, 0xFF555555);
        drawRect(x,     y,     x + w, y + 1, 0xFFFFFFFF);
        drawRect(x,     y,     x + 1, y + h, 0xFFFFFFFF);
        drawRect(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }
}
