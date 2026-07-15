package com.origami10004.necalc.compat.jei;

import mezz.jei.api.gui.IAdvancedGuiHandler;

import java.util.List;
import java.awt.Rectangle;

import com.origami10004.necalc.gui.GuiCommon;

public class NecalcGuiHandler<T extends GuiCommon> implements IAdvancedGuiHandler<T> {
	
	private final Class<T> guiClass;

	public NecalcGuiHandler(Class<T> guiClass) {
		this.guiClass = guiClass;
	}

	@Override
	public Class<T> getGuiContainerClass() {
		return guiClass;
	}

	@Override
	public Object getIngredientUnderMouse(T gui, int mouseX, int mouseY) {
		return gui.getHoveredStack(mouseX, mouseY).getStack();
	}

	@Override
	public List<Rectangle> getGuiExtraAreas(T guiContainer) {
		return guiContainer.getExtraGuiArea();
	}	
}
