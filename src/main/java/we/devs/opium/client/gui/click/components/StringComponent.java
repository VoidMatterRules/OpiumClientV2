package we.devs.opium.client.gui.click.components;

import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.util.math.MatrixStack;
import we.devs.opium.Opium;
import we.devs.opium.api.manager.event.EventListener;
import we.devs.opium.api.manager.miscellaneous.FontManager;
import we.devs.opium.api.utilities.RenderUtils;
import we.devs.opium.api.utilities.TimerUtils;
import we.devs.opium.api.utilities.font.FontRenderers;
import we.devs.opium.client.events.EventKey;
import we.devs.opium.client.gui.click.manage.Component;
import we.devs.opium.client.gui.click.manage.Frame;
import we.devs.opium.client.values.impl.ValueString;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class StringComponent extends Component implements EventListener {
    private final ValueString value;
    private boolean listening;
    private String currentString = "";
    private final TimerUtils timer = new TimerUtils();
    private boolean selecting = false;
    private boolean line = false;
    private int renderOffset = 0;

    public StringComponent(ValueString value, int offset, Frame parent) {
        super(offset, parent);
        Opium.EVENT_MANAGER.register(this);
        this.value = value;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        if (this.timer.hasTimeElapsed(400L)) {
            this.line = !this.line; // Cursor-animation (blinking)
            this.timer.reset();
        }

        // Backgroundcolor
        Color defaultColor = Opium.CLICK_GUI.getColor();
        Color backgroundColor = this.listening
                ? new Color(
                Math.max(defaultColor.getRed() - 20, 0),
                Math.max(defaultColor.getGreen() - 20, 0),
                Math.max(defaultColor.getBlue() - 20, 0),
                defaultColor.getAlpha())
                : defaultColor;

        // Textbox-Background drawn (with outline)
        RenderUtils.drawRect(
                context.getMatrices(),
                this.getX() + 1,
                this.getY(),
                this.getX() + this.getWidth() - 1,
                this.getY() + 14,
                backgroundColor);

        // Position and calculation of visible area
        int paddingX = 3; // left/right separation
        int textX = this.getX() + paddingX;
        int textY = this.getY() + 3;
        int textBoxWidth = this.getWidth() - 2 * paddingX+20;

        // get Whole text
        String fullText = this.listening ? this.currentString : this.value.getValue();
        int fullTextWidth = mc.textRenderer.getWidth(fullText); // Width the whole text

        // Visible text calculation while accounting for renderOffset
        String visibleText = fullText;
        if (fullTextWidth > textBoxWidth) {
            // change of renderOffset, if need be
            int maxOffset = fullTextWidth - textBoxWidth;
            if (renderOffset > maxOffset) {
                renderOffset = maxOffset;
            }

            // cut String based on Offset and Textbox-width
            visibleText = getVisibleText(fullText, renderOffset, textBoxWidth);
        } else {
            // no scrolling needed
            renderOffset = 0;
        }

        // text drawn
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        context.enableScissor(textX,textY,textX+textBoxWidth,textY+getHeight());
        RenderUtils.drawString(context.getMatrices(), visibleText, textX, textY, Color.LIGHT_GRAY.getRGB());
        context.disableScissor();
        matrices.pop();
        // cursor drawn
        if (this.listening && this.line) {
            int visibleTextWidth = (int) FontRenderers.fontRenderer.getStringWidth((visibleText));
            int cursorX = (textX + visibleTextWidth); // Cursor direkt hinter dem letzten Zeichen

            // draw Cursor
            RenderUtils.drawRect(
                    context.getMatrices(),
                    cursorX,
                    this.getY() + 3,
                    cursorX + 1, // 1 Pixel breit
                    this.getY() + 13, // Höhe des Cursors
                    new Color(180, 180, 180)
            );
        }
    }

    private String getVisibleText(String fullText, int renderOffset, int textBoxWidth) {

        StringBuilder visibleText = new StringBuilder();
        int currentWidth = 0;

        for (int i = renderOffset; i < fullText.length(); i++) {
            char c = fullText.charAt(i);
            int charWidth = (int) FontRenderers.fontRenderer.getStringWidth(String.valueOf(c));

            if (currentWidth + charWidth > textBoxWidth) {
                break; // Textbox-border reached, abort
            }

            visibleText.append(c);
            currentWidth += charWidth;
        }
        return visibleText.toString();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        selecting = false;
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            if (this.isHovering(mouseX, mouseY)) {
                this.parent.closeOtherTextboxListening();

                this.listening = !this.listening;
                this.currentString = this.value.getValue();
            } else if (this.listening) {
                this.updateString();
                this.listening = false;
            }
        }
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (this.listening && !this.isHovering(mouseX, mouseY)) {
            this.updateString();
            this.listening = false;
        }
    }

    @Override
    public void onKey(EventKey event) {
        if (listening && event.getScanCode() == GLFW.GLFW_PRESS) {
            if (event.getKeyCode() == InputUtil.GLFW_KEY_ENTER) {
                this.updateString();
                this.listening = false;
            } else if (event.getKeyCode() == InputUtil.GLFW_KEY_BACKSPACE) {
                if (!this.currentString.isEmpty()) {
                    this.currentString = this.currentString.substring(0, this.currentString.length() - 1);
                }
            } else if (event.getKeyCode() == GLFW.GLFW_KEY_LEFT) {
                if (renderOffset > 0) {
                    renderOffset--; // scroll to the left
                }
            } else if (event.getKeyCode() == GLFW.GLFW_KEY_RIGHT) {
                if (mc.textRenderer.getWidth(this.currentString) > this.getWidth() - 6) {
                    renderOffset++; // scroll to the right
                }
            } else if (event.getKeyCode() == GLFW.GLFW_KEY_SPACE) {
                this.currentString += " ";
            } else if (event.getKeyCode() == InputUtil.GLFW_KEY_V && isCtrlPressed()) {
                // pasting text from the clipboard
                String clipboardData = getClipboard();
                if (clipboardData != null) {
                    this.currentString += clipboardData;
                }
            } else {
                String keyName = GLFW.glfwGetKeyName(event.getKeyCode(), event.getScanCode());
                if (keyName != null && !keyName.isEmpty()) {
                    char typedChar = keyName.charAt(0);
                    boolean isShiftPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                            InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
                    if (isShiftPressed) {
                        typedChar = Character.toUpperCase(typedChar);
                    }
                    if (isValidChatCharacter(typedChar)) {
                        this.currentString += typedChar;

                        // automatic scrolling to the right if the text gets longer
                        if (mc.textRenderer.getWidth(this.currentString) > this.getWidth() - 6) {
                            renderOffset++;
                        }
                    }
                }
            }
        }
    }

    private boolean isValidChatCharacter(char c) {
        return c >= ' ' && c != 127;
    }

    private void updateString() {
        this.value.setValue(this.currentString);
    }

    private String removeLastCharacter(String input) {
        if (!input.isEmpty()) {
            return input.substring(0, input.length() - 1);
        }
        return input;
    }

    /**
     * Safe Method to read the clipboard contents
     */
    private String getClipboard() {
        // try to read GLFW-Clipboard
        long window = GLFW.glfwGetCurrentContext();
        CharSequence glfwClipboard = GLFW.glfwGetClipboardString(window);

        if (glfwClipboard != null) {
            return glfwClipboard.toString();
        }

        // Fallback on AWT (if the GUI environment is available)
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                return (String) Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .getData(DataFlavor.stringFlavor);
            }
        } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
            System.err.println("Error when accessing the clipboard:" + e.getMessage());
        }

        return null;
    }

    /**
     * Checks if CTRL for Copy-Paste is pressed.
     */
    private boolean isCtrlPressed() {
        long handle = mc.getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_CONTROL)
                || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_CONTROL);
    }

    public ValueString getValue() {
        return this.value;
    }
}