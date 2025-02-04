package we.devs.opium.api.utilities;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SmoothRotationUtil {
    private static float targetYaw = 0;
    private static float targetPitch = 0;
    private static float originalYaw = 0;
    private static float originalPitch = 0;
    private static boolean rotating = false;
    private static boolean rotatingBack = false;
    private static boolean rotationComplete = false; // New flag
    private static final float ROTATION_SPEED = 5.0f; // Adjust for smoother/faster rotation

    public static void setTargetRotation(float yaw, float pitch) {
        targetYaw = yaw;
        targetPitch = pitch;
        rotating = true;
        rotatingBack = false;
        rotationComplete = false; // Reset the flag
    }

    public static void rotateToBlockPos(BlockPos blockPos) {
        if (MinecraftClient.getInstance().player == null) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        Vec3d playerPos = player.getEyePos(); // Player's eye position
        Vec3d blockCenter = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5); // Center of the block

        // Calculate the difference vector between the player and the block
        Vec3d diff = blockCenter.subtract(playerPos);

        // Calculate yaw and pitch
        double horizontalDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, horizontalDistance));

        // Store the original rotation
        originalYaw = player.getYaw();
        originalPitch = player.getPitch();

        setTargetRotation(yaw, pitch);
    }

    public static void rotateBackToOriginal() {
        if (!rotating && !rotatingBack) {
            rotatingBack = true;
            targetYaw = originalYaw;
            targetPitch = originalPitch;
            rotationComplete = false; // Reset the flag
        }
    }

    public static void updateRotation() {
        if ((!rotating && !rotatingBack) || MinecraftClient.getInstance().player == null) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        // Interpolate yaw and pitch
        float newYaw = lerpAngle(currentYaw, targetYaw, ROTATION_SPEED * 0.1f);
        float newPitch = lerp(currentPitch, targetPitch, ROTATION_SPEED * 0.1f);

        // Set the new rotation
        player.setYaw(newYaw);
        player.setPitch(newPitch);

        // Send the rotation to the server
        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.LookAndOnGround(newYaw, newPitch, player.isOnGround());
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);

        // Check if we've reached the target rotation
        if (Math.abs(newYaw - targetYaw) < 1.0f && Math.abs(newPitch - targetPitch) < 1.0f) {
            if (rotating) {
                rotating = false;
                rotationComplete = true; // Set the flag
                onRotationComplete();
            } else if (rotatingBack) {
                rotatingBack = false;
                onRotationBackComplete();
            }
        }
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private static float lerpAngle(float start, float end, float t) {
        float difference = MathHelper.wrapDegrees(end - start);
        return start + difference * t;
    }

    private static void onRotationComplete() {
        // This method is called when the rotation to the target is complete
        System.out.println("Rotation to target complete!");
    }

    private static void onRotationBackComplete() {
        // This method is called when the rotation back to the original position is complete
        System.out.println("Rotation back to original complete!");
    }

    public static boolean isRotating() {
        return rotating || rotatingBack;
    }

    public static boolean isRotationComplete() {
        return rotationComplete;
    }

    public static void resetRotationComplete() {
        rotationComplete = false;
    }

}