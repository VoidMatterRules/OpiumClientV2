package we.devs.opium.client.gui.click.components;

import we.devs.opium.Opium;
import we.devs.opium.api.utilities.MathUtils;
import we.devs.opium.api.utilities.RenderUtils;
import we.devs.opium.client.gui.click.manage.Component;
import we.devs.opium.client.gui.click.manage.Frame;
import we.devs.opium.client.values.impl.ValueNumber;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

public class NumberComponent extends Component {
    private final ValueNumber value;
    private float sliderWidth;
    private boolean dragging;

    public NumberComponent(ValueNumber value, int offset, Frame parent) {
        super(offset, parent);
        this.value = value;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        // Draw the slider background
        RenderUtils.drawRect(context.getMatrices(), this.getX() + 1, this.getY(), (float)(this.getX() + 1) + this.sliderWidth, this.getY() + 14, Opium.CLICK_GUI.getColor());

        // Draw the value text
        RenderUtils.drawString(context.getMatrices(), this.value.getTag() + " " + Formatting.GRAY + this.value.getValue() + (this.value.getType() == 1 ? ".0" : ""), this.getX() + 3, this.getY() + 3, -1);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTicks) {
        super.update(mouseX, mouseY, partialTicks);
        if (this.value.getParent() != null) {
            this.setVisible(this.value.getParent().isOpen());
        }

        double difference = Math.min(98, Math.max(0, mouseX - this.getX()));
        if (this.value.getType() == 1) {
            this.sliderWidth = 98.0f * (float)(this.value.getValue().intValue() - this.value.getMinimum().intValue()) / (float)(this.value.getMaximum().intValue() - this.value.getMinimum().intValue());
            if (this.dragging) {
                if (difference == 0.0) {
                    this.value.setValue(this.value.getMinimum());
                } else {
                    int value = (int) MathUtils.roundToPlaces(difference / 98.0 * (double)(this.value.getMaximum().intValue() - this.value.getMinimum().intValue()) + (double)this.value.getMinimum().intValue(), 0);
                    this.value.setValue(value);
                }
            }
        } else if (this.value.getType() == 2) {
            this.sliderWidth = (float)(98.0 * (this.value.getValue().doubleValue() - this.value.getMinimum().doubleValue()) / (this.value.getMaximum().doubleValue() - this.value.getMinimum().doubleValue()));
            if (this.dragging) {
                if (difference == 0.0) {
                    this.value.setValue(this.value.getMinimum());
                } else {
                    double value = MathUtils.roundToPlaces(difference / 98.0 * (this.value.getMaximum().doubleValue() - this.value.getMinimum().doubleValue()) + this.value.getMinimum().doubleValue(), 2);
                    this.value.setValue(value);
                }
            }
        } else if (this.value.getType() == 3) {
            this.sliderWidth = 98.0f * (this.value.getValue().floatValue() - this.value.getMinimum().floatValue()) / (this.value.getMaximum().floatValue() - this.value.getMinimum().floatValue());
            if (this.dragging) {
                if (difference == 0.0) {
                    this.value.setValue(this.value.getMinimum());
                } else {
                    float value = (float)MathUtils.roundToPlaces(difference / 98.0 * (double)(this.value.getMaximum().floatValue() - this.value.getMinimum().floatValue()) + (double)this.value.getMinimum().floatValue(), 2);
                    this.value.setValue(value);
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.isHovering(mouseX, mouseY) && mouseButton == 0) {
            this.dragging = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.dragging = false;
    }

    public ValueNumber getValue() {
        return this.value;
    }
}