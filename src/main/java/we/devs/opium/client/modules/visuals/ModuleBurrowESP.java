package we.devs.opium.client.modules.visuals;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.client.events.EventRender2D;
import we.devs.opium.client.values.impl.ValueColor;
import we.devs.opium.client.values.impl.ValueNumber;

import java.awt.*;

import static me.x150.renderer.util.RendererUtils.worldSpaceToScreenSpace;

@RegisterModule(name = "BurrowESP", description = "Display if a player is burrowed", tag = "BurrowESP", category = Module.Category.VISUALS)
public class ModuleBurrowESP extends Module {

    private final ValueNumber scale = new ValueNumber("Scale","Scale","Scale", 0.68f, 0.1f, 2f);
    private final ValueNumber minScale = new ValueNumber("MinScale","MinScale","MinScale", 0.2f, 0.1f, 1f);
    private final ValueNumber yOff = new ValueNumber("YOffset", "YOffset", "Text y offset", 0.1f, -0.5, 0.5);
    private final ValueColor color = new ValueColor("TextColor", "TextColor", "TextColor", Color.WHITE);

    @Override
    public void onRender2D(EventRender2D context) {
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        assert mc.world != null;
        for (PlayerEntity ent : mc.world.getPlayers()) {
            if (ent == mc.player && mc.options.getPerspective().isFirstPerson()) continue;
            if(mc.world.getBlockState(ent.getBlockPos()).isReplaceable()) continue;
            double x = ent.prevX + (ent.getX() - ent.prevX) * tickDelta;
            double y = ent.prevY + (ent.getY() - ent.prevY) * tickDelta;
            double z = ent.prevZ + (ent.getZ() - ent.prevZ) * tickDelta;
            Vec3d vector = new Vec3d(x, y + yOff.getValue().doubleValue(), z);
            Vec3d preVec = vector;
            vector = worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (vector.z > 0 && vector.z < 1) {
                Vector4d position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);

                String text = "Burrowed";

                double posX = position.x;
                double posY = position.y;
                double endPosX = position.z;

                float diff = (float) (endPosX - posX) / 2;
                float textWidth = mc.textRenderer.getWidth(text);

                float tagX = (float) ((posX + diff - textWidth / 2) * 1);
                context.getContext().getMatrices().push();
                context.getContext().getMatrices().translate(tagX - 2 + (textWidth + 4) / 2f, (float) (posY - 13f) + 6.5f, 0);
                float size = (float) Math.max(1 - MathHelper.sqrt((float) mc.cameraEntity.squaredDistanceTo(preVec)) * 0.01, 0);
                context.getContext().getMatrices().scale(Math.max(scale.getValue().floatValue() * size, minScale.getValue().floatValue()), Math.max(scale.getValue().floatValue() * size, minScale.getValue().floatValue()), 1f);
                context.getContext().getMatrices().translate(0, MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(preVec)), 0);
                context.getContext().getMatrices().translate(-(tagX - 2 + (textWidth + 4) / 2f), -(float) ((posY - 13f) + 6.5f), 0);

                context.getContext().getMatrices().push();
                context.getContext().getMatrices().translate(tagX, ((float) posY - 11), 0);
                context.getContext().drawText(mc.textRenderer, text, 0, 0, color.getValue().getRGB(), true);
                context.getContext().getMatrices().pop();
                context.getContext().getMatrices().pop();
            }
        }
    }
}
