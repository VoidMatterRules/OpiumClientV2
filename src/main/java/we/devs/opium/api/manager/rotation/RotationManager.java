package we.devs.opium.api.manager.rotation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import we.devs.opium.api.utilities.RotationUtils;
import we.devs.opium.client.events.EventMotion;
import we.devs.opium.client.events.EventPacketSend;
import we.devs.opium.client.modules.client.ModuleRotations;

import java.util.ArrayList;
import java.util.List;

// Author jaxui
public class RotationManager {

    public RotationManager() {
    }

    private MinecraftClient mc = MinecraftClient.getInstance();
    private ModuleRotations RotationModule = ModuleRotations.getInstance();

    private float yaw, pitch, currentPriority;
    private List<Rotation> rotationRequests = new ArrayList<>();

    private void onMotion(EventMotion event) {
        if (yaw != mc.player.getYaw()) {
            event.setRotationYaw(yaw);
        }
        if (pitch != mc.player.getPitch()) {
            event.setRotationPitch(pitch);
        }
    }

    private void OnPacketOutbound(EventPacketSend event) {
        if (mc.player == null || mc.world == null) {return;}


        if(event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesLook()) {
            yaw = packet.getYaw(0f);
            pitch = packet.getPitch(0f);
        }

        for (Rotation rotation : rotationRequests) {
            if (rotation.getPriority() > currentPriority) {
                currentPriority = rotation.getPriority();
                if (rotation.isBlock()) {
                    handleBlockRotation(rotation);
                } else  handleEntityRotation(rotation);
                rotationRequests.remove(rotation);
            }
        }

    }

    private void handleBlockRotation(Rotation rotation) {
        if (RotationModule.getBlockRotations().equals(ModuleRotations.blockRotations.Packet)) {
            PlayerMoveC2SPacket p = new PlayerMoveC2SPacket.LookAndOnGround(rotation.getYaw(), rotation.getPitch(), mc.player.isOnGround());
            mc.getNetworkHandler().sendPacket(p);
            yaw = rotation.getYaw();
            pitch = rotation.getPitch();
        }
        if (RotationModule.getBlockRotations().equals(ModuleRotations.entityRotations.NCP)) {
            float[] a = new float[]{rotation.getYaw(), rotation.getPitch()};
            float[] angles = RotationUtils.getSmoothRotations(a,ModuleRotations.INSTANCE.getBlockSmooth());
            PlayerMoveC2SPacket p = new PlayerMoveC2SPacket.LookAndOnGround(angles[0], angles[1], mc.player.isOnGround());
            mc.getNetworkHandler().sendPacket(p);
            yaw= angles[0];
            pitch= angles[1];
        }
        if (RotationModule.getBlockRotations().equals(ModuleRotations.entityRotations.Grim)) {
            PlayerMoveC2SPacket first = new PlayerMoveC2SPacket.Full(mc.player.getX(),mc.player.getY(),mc.player.getZ(),rotation.getYaw(), rotation.getPitch(), mc.player.isOnGround());
            PlayerMoveC2SPacket second = new PlayerMoveC2SPacket.Full(mc.player.getX(),mc.player.getY(),mc.player.getZ(),rotation.getYaw(), rotation.getPitch(), mc.player.isOnGround());
            mc.getNetworkHandler().sendPacket(first);
            yaw = rotation.getYaw();
            pitch = rotation.getPitch();
            mc.getNetworkHandler().sendPacket(second);
            yaw = mc.player.getYaw();
            pitch = mc.player.getPitch();
        }
    }

    private void handleEntityRotation(Rotation rotation) {
        if (RotationModule.getEntityRotations().equals(ModuleRotations.blockRotations.Packet)) {
            PlayerMoveC2SPacket p = new PlayerMoveC2SPacket.LookAndOnGround(rotation.getYaw(), rotation.getPitch(), mc.player.isOnGround());
            mc.getNetworkHandler().sendPacket(p);
            yaw = rotation.getYaw();
            pitch = rotation.getPitch();
        }
        if (RotationModule.getEntityRotations().equals(ModuleRotations.entityRotations.NCP)) {
            float[] a = new float[]{rotation.getYaw(), rotation.getPitch()};
            float[] angles = RotationUtils.getSmoothRotations(a,ModuleRotations.INSTANCE.getEntitySmooth());
            PlayerMoveC2SPacket p = new PlayerMoveC2SPacket.LookAndOnGround(angles[0], angles[1], mc.player.isOnGround());
            mc.getNetworkHandler().sendPacket(p);
            yaw= angles[0];
            pitch= angles[1];
        }
        if (RotationModule.getEntityRotations().equals(ModuleRotations.entityRotations.Grim)) {
            PlayerMoveC2SPacket first = new PlayerMoveC2SPacket.Full(mc.player.getX(),mc.player.getY(),mc.player.getZ(),rotation.getYaw(), rotation.getPitch(), mc.player.isOnGround());
            PlayerMoveC2SPacket second = new PlayerMoveC2SPacket.Full(mc.player.getX(),mc.player.getY(),mc.player.getZ(),rotation.getYaw(), rotation.getPitch(), mc.player.isOnGround());
            mc.getNetworkHandler().sendPacket(first);
            yaw = rotation.getYaw();
            pitch = rotation.getPitch();
            mc.getNetworkHandler().sendPacket(second);
            yaw = mc.player.getYaw();
            pitch = mc.player.getPitch();
        }
    }

    public void RequestRotation(int priority, Vec3d pos, boolean block) {
        float[] angles = RotationUtils.getRotationsTo(pos);

        Rotation rotation = new Rotation(
                priority,
                angles[0],
                angles[1],
                false,
                false,
                block
        );

        rotationRequests.add(rotation);
    }

    public void RequestRotation(int priority, Vec3d pos,Boolean snap,Boolean grim,Boolean block) {
        float[] angles = RotationUtils.getRotationsTo(pos);

        Rotation rotation = new Rotation(
                priority,
                angles[0],
                angles[1],
                snap,
                grim,
                block
        );

        rotationRequests.add(rotation);
    }

}
