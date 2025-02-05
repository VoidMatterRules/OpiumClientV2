package we.devs.opium.client.gui.click.components;

import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.util.math.MatrixStack;
import we.devs.opium.Opium;
import we.devs.opium.api.manager.event.EventListener;
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
    private static final int PADDING_X = 3; // Left/right padding
    private static final int CURSOR_WIDTH = 1; // Cursor width in pixels
    private static final int CURSOR_HEIGHT = 10; // Cursor height in pixels
    private static final int BLINK_INTERVAL = 400; // Cursor blink interval in milliseconds
    private static final Color CURSOR_COLOR = new Color(180, 180, 180);
    private static final Color TEXT_COLOR = Color.LIGHT_GRAY;

    private final ValueString value;
    private boolean listening;
    private String currentString = "";
    private final TimerUtils timer = new TimerUtils();
    private boolean line = false; // Cursor visibility
    private int renderOffset = 0; // Text scroll offset

    public StringComponent(ValueString value, int offset, Frame parent) {
        super(offset, parent);
        Opium.EVENT_MANAGER.register(this);
        this.value = value;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        // Update cursor blink animation
        if (this.timer.hasTimeElapsed(BLINK_INTERVAL)) {
            this.line = !this.line;
            this.timer.reset();
        }

        // Background color (darker when listening)
        Color defaultColor = Opium.CLICK_GUI.getColor();
        Color backgroundColor = this.listening
                ? new Color(
                Math.max(defaultColor.getRed() - 20, 0),
                Math.max(defaultColor.getGreen() - 20, 0),
                Math.max(defaultColor.getBlue() - 20, 0),
                defaultColor.getAlpha())
                : defaultColor;

        // Draw textbox background
        RenderUtils.drawRect(
                context.getMatrices(),
                this.getX() + 1,
                this.getY(),
                this.getX() + this.getWidth() - 1,
                this.getY() + 14,
                backgroundColor);

        // Calculate visible text area
        int textX = this.getX() + PADDING_X;
        int textY = this.getY() + 3;
        int textBoxWidth = this.getWidth() - 2 * PADDING_X;

        // Get full text and its width
        String fullText = this.listening ? this.currentString : this.value.getValue();
        int fullTextWidth = mc.textRenderer.getWidth(fullText);

        // Calculate visible text based on scroll offset
        String visibleText = fullText;
        if (fullTextWidth > textBoxWidth) {
            renderOffset = Math.min(renderOffset, fullTextWidth - textBoxWidth);
            visibleText = getVisibleText(fullText, renderOffset, textBoxWidth);
        } else {
            renderOffset = 0; // Reset offset if no scrolling is needed
        }

        // Draw visible text with scissor for clipping
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        context.enableScissor(textX, textY, textX + textBoxWidth, textY + getHeight());
        RenderUtils.drawString(context.getMatrices(), visibleText, textX, textY, TEXT_COLOR.getRGB());
        context.disableScissor();
        matrices.pop();

        // Draw cursor if listening and visible
        if (this.listening && this.line) {
            int visibleTextWidth = (int) FontRenderers.fontRenderer.getStringWidth(visibleText);
            int cursorX = textX + visibleTextWidth;

            RenderUtils.drawRect(
                    context.getMatrices(),
                    cursorX,
                    this.getY() + 3,
                    cursorX + CURSOR_WIDTH,
                    this.getY() + 3 + CURSOR_HEIGHT,
                    CURSOR_COLOR
            );
        }
    }

    /**
     * Gets the visible portion of the text based on the scroll offset and textbox width.
     */
    private String getVisibleText(String fullText, int renderOffset, int textBoxWidth) {
        StringBuilder visibleText = new StringBuilder();
        int currentWidth = 0;

        for (int i = renderOffset; i < fullText.length(); i++) {
            char c = fullText.charAt(i);
            int charWidth = (int) FontRenderers.fontRenderer.getStringWidth(String.valueOf(c));

            if (currentWidth + charWidth > textBoxWidth) {
                break; // Stop if the text exceeds the textbox width
            }

            visibleText.append(c);
            currentWidth += charWidth;
        }

        return visibleText.toString();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.listening = !this.listening;
            this.currentString = this.value.getValue();
        } else if (this.listening) {
            this.updateString();
            this.listening = false;
        }
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
            switch (event.getKeyCode()) {
                case InputUtil.GLFW_KEY_ENTER:
                    this.updateString();
                    this.listening = false;
                    break;
                case InputUtil.GLFW_KEY_BACKSPACE:
                    if (!this.currentString.isEmpty()) {
                        this.currentString = this.currentString.substring(0, this.currentString.length() - 1);
                    }
                    break;
                case GLFW.GLFW_KEY_LEFT:
                    if (renderOffset > 0) {
                        renderOffset--; // Scroll left
                    }
                    break;
                case GLFW.GLFW_KEY_RIGHT:
                    if (mc.textRenderer.getWidth(this.currentString) > this.getWidth() - 6) {
                        renderOffset++; // Scroll right
                    }
                    break;
                case GLFW.GLFW_KEY_SPACE:
                    this.currentString += " ";
                    break;
                case InputUtil.GLFW_KEY_V:
                    if (isCtrlPressed()) {
                        String clipboardData = getClipboard();
                        if (clipboardData != null) {
                            this.currentString += clipboardData;
                        }
                    }
                    break;
                default:
                    handleTypedCharacter(event);
                    break;
            }
        }
    }

    /**
     * Handles typed characters and updates the current string.
     */
    private void handleTypedCharacter(EventKey event) {
        String keyName = GLFW.glfwGetKeyName(event.getKeyCode(), event.getScanCode());
        if (keyName != null && !keyName.isEmpty()) {
            char typedChar = keyName.charAt(0);
            if (isShiftPressed()) {
                typedChar = Character.toUpperCase(typedChar);
            }
            if (isValidChatCharacter(typedChar)) {
                this.currentString += typedChar;

                // Auto-scroll if text exceeds textbox width
                if (mc.textRenderer.getWidth(this.currentString) > this.getWidth() - 6) {
                    renderOffset++;
                }
            }
        }
    }

    /**
     * Checks if the character is valid for chat input.
     */
    private boolean isValidChatCharacter(char c) {
        return c >= ' ' && c != 127;
    }

    /**
     * Updates the value with the current string.
     */
    private void updateString() {
        this.value.setValue(this.currentString);
    }

    /**
     * Safely retrieves the clipboard contents.
     */
    private String getClipboard() {
        try {
            long window = GLFW.glfwGetCurrentContext();
            CharSequence glfwClipboard = GLFW.glfwGetClipboardString(window);
            if (glfwClipboard != null) {
                return glfwClipboard.toString();
            }

            if (!GraphicsEnvironment.isHeadless()) {
                return (String) Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .getData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            System.err.println("Error accessing clipboard: " + e.getMessage());
        }

        return null;
    }

    /**
     * Checks if CTRL is pressed for copy-paste operations.
     */
    private boolean isCtrlPressed() {
        long handle = mc.getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_LEFT_CONTROL)
                || InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_CONTROL);
    }

    /**
     * Checks if SHIFT is pressed for uppercase characters.
     */
    private boolean isShiftPressed() {
        long handle = mc.getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public ValueString getValue() {
        return this.value;
    }
}