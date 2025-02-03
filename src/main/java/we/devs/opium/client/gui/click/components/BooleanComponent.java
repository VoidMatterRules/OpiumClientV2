package we.devs.opium.client.gui.click.components;

import we.devs.opium.Opium;
import we.devs.opium.api.utilities.RenderUtils;
import we.devs.opium.client.gui.click.manage.Component;
import we.devs.opium.client.gui.click.manage.Frame;
import we.devs.opium.client.modules.client.ModuleColor;
import we.devs.opium.client.values.impl.ValueBoolean;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.opengl.GL11;

public class BooleanComponent extends Component {
    private final ValueBoolean value;

    public BooleanComponent(ValueBoolean value, int offset, Frame parent) {
        super(offset, parent);
        this.value = value;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        if (this.value.getValue()) {
            RenderUtils.drawRect(context.getMatrices(), this.getX() + 1, this.getY(), this.getX() + this.getWidth() - 1, this.getY() + 14, Opium.CLICK_GUI.getColor());
        }
        RenderUtils.drawString(context.getMatrices(), this.value.getTag(), this.getX() + 3, this.getY() + 3, -1);
    }

    private void prepareLine() {
        GL11.glBegin(1);
        GL11.glColor3f((float) ModuleColor.getColor().getRed() / 255.0f, (float)ModuleColor.getColor().getGreen() / 255.0f, (float)ModuleColor.getColor().getBlue() / 255.0f);
        GL11.glVertex2d(this.getX() + this.getWidth() - 8, this.getY() + 10);
        GL11.glColor3f((float)ModuleColor.getColor().getRed() / 255.0f, (float)ModuleColor.getColor().getGreen() / 255.0f, (float)ModuleColor.getColor().getBlue() / 255.0f);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTicks) {
        super.update(mouseX, mouseY, partialTicks);
        if (this.value.getParent() != null) {
            this.setVisible(this.value.getParent().isOpen());
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.isHovering(mouseX, mouseY) && mouseButton == 0) {
            this.value.setValue(!this.value.getValue());
        }
    }

    public ValueBoolean getValue() {
        return this.value;
    }
}
