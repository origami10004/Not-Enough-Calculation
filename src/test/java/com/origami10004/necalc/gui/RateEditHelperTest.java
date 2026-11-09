package com.origami10004.necalc.gui;

import java.lang.reflect.Field;

import com.origami10004.necalc.Necalc;
import net.minecraft.client.gui.GuiTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.input.Keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RateEditHelperTest {
    private Logger previousLogger;

    @Before
    public void setUpLogger() {
        previousLogger = Necalc.logger;
        Necalc.logger = LogManager.getLogger("necalc-test");
    }

    @After
    public void restoreLogger() {
        Necalc.logger = previousLogger;
    }

    @Test
    public void confirmsDecimalPointWithoutCrashing() throws Exception {
        RateEditHelper editor = editorWithText(".");
        assertTrue(editor.keyTyped('\n', Keyboard.KEY_RETURN));
        assertFalse(editor.isOpen());
    }

    @Test
    public void confirmsDecimalsWithMissingWholeOrFractionalPart() throws Exception {
        for (String text : new String[]{".5", "5.", "0.", ".0", "2.5", ""}) {
            RateEditHelper editor = editorWithText(text);
            assertTrue(editor.keyTyped('\n', Keyboard.KEY_NUMPADENTER));
            assertFalse("Editor should close for " + text, editor.isOpen());
        }
    }

    @Test
    public void malformedRateKeepsEditorOpenWithoutCrashing() throws Exception {
        RateEditHelper editor = editorWithText("..");
        assertTrue(editor.keyTyped('\n', Keyboard.KEY_RETURN));
        assertTrue(editor.isOpen());
    }

    private static RateEditHelper editorWithText(String text) throws Exception {
        RateEditHelper editor = new RateEditHelper(null);
        GuiTextField field = new GuiTextField(0, null, 0, 0, 100, 20);
        // Set text without a renderer or a running Minecraft client.
        setField(field, "text", text);
        setField(editor, "rateInputField", field);
        setField(editor, "isOpen", true);
        // The default slot -1 makes confirmation a no-op on persistent target data.
        return editor;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
