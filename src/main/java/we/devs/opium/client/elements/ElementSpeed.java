package we.devs.opium.client.elements;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import we.devs.opium.api.manager.element.Element;
import we.devs.opium.api.manager.element.RegisterElement;
import we.devs.opium.api.utilities.RenderUtils;
import we.devs.opium.client.events.EventRender2D;
import we.devs.opium.client.modules.client.ModuleColor;
import we.devs.opium.client.modules.client.ModuleFont;

import java.text.DecimalFormat;

@RegisterElement(name = "Speed", tag = "Speed", description = "Shows your speed")
public class ElementSpeed extends Element {
    @Override
    public void onRender2D(EventRender2D event) {
        if(RenderUtils.getFontRenderer() == null) return;
        super.onRender2D(event);
        if(ModuleFont.INSTANCE.customFonts.getValue()) {
            this.frame.setWidth(RenderUtils.getFontRenderer().getStringWidth(getText()));
            this.frame.setHeight(RenderUtils.getFontRenderer().getStringHeight(getText()));
        } else {
            this.frame.setWidth(mc.textRenderer.getWidth(this.getText()));
            this.frame.setHeight(mc.textRenderer.fontHeight);
        }
        RenderUtils.drawString(new MatrixStack(),this.getText(), (int) this.frame.getX(), (int) this.frame.getY(), ModuleColor.getColor().getRGB());
    }

    String getText() {
        DecimalFormat df = new DecimalFormat("#.#");
        double d = mc.player.getX() - mc.player.prevX;
        double deltaZ = mc.player.getZ() - mc.player.prevZ;
        float tickRate = mc.getRenderTime();
        String speedText = df.format((double) (MathHelper.sqrt((float) (d * d + deltaZ * deltaZ)) / tickRate) * 3.6);
        return "Speed: " + speedText;
    }
}
