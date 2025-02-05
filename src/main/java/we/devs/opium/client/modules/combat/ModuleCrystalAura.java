package we.devs.opium.client.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.client.values.impl.ValueBoolean;
import we.devs.opium.client.values.impl.ValueNumber;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RegisterModule(name = "CrystalAura", description = "Places and breaks crystals on bedrock/obsidian near the target", category = Module.Category.COMBAT)
public class ModuleCrystalAura extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final ValueNumber range = new ValueNumber("Range", "Range", "", 5.0f, 1.0f, 10.0f);
    private final ValueBoolean rotate = new ValueBoolean("Rotate", "Rotate", "", false);

    private final Set<BlockPos> blacklistedPositions = new HashSet<>();

    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null) return;

        PlayerEntity target = getTarget();
        if (target == null) {
            return;
        }

        BlockPos placePos = findBestPlacePosition(target);
        while (placePos != null && !placeCrystal(placePos)) {
            blacklistedPositions.add(placePos);
            placePos = findBestPlacePosition(target);
        }

        if (placePos != null) {
            breakCrystalAt(placePos.up());
        } else {
        }
    }

    @Override
    public void onDisable() {
        blacklistedPositions.clear();
    }

    private PlayerEntity getTarget() {
        return mc.world.getPlayers().stream()
                .filter(player -> player != mc.player && !player.isRemoved())
                //.filter(player -> mc.player.distanceTo(player) < range.getValue())
                .min((player1, player2) -> Double.compare(mc.player.distanceTo(player1), mc.player.distanceTo(player2)))
                .orElse(null);
    }

    private BlockPos findBestPlacePosition(PlayerEntity target) {
        BlockPos standingOn = target.getBlockPos().down(); // Block the player is standing on

        List<BlockPos> possiblePositions = Stream.of(
                        standingOn.north(), standingOn.south(), standingOn.east(), standingOn.west(),
                        standingOn.north().down(), standingOn.south().down(),
                        standingOn.east().down(), standingOn.west().down()
                )
                .filter(this::isValidTargetBlock) // Ensure it's a valid block (obsidian/bedrock)
                .filter(pos -> !blacklistedPositions.contains(pos))
                .sorted((p1, p2) -> Double.compare(p1.getSquaredDistance(target.getPos()), p2.getSquaredDistance(target.getPos())))
                .collect(Collectors.toList());

        // Return the best block based on damage calculations
        return possiblePositions.stream()
                .max((p1, p2) -> Float.compare(calculateDamage(p1, target), calculateDamage(p2, target)))
                .orElse(null);
    }


    private boolean isValidTargetBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN;
    }

    private float calculateDamage(BlockPos pos, PlayerEntity target) {
        // Simplified damage calculation
        double dist = Math.sqrt(pos.getSquaredDistance(target.getPos()));
        double exposure = 1.0 - (dist / 12.0);
        return (float) ((exposure * exposure + exposure) * 7.0 * 12.0 + 1.0);
    }

    private boolean placeCrystal(BlockPos pos) {
        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) {
            // Swap to end crystal if not holding one
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getItem() == Items.END_CRYSTAL) {
                    mc.player.getInventory().selectedSlot = i;
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(i));  // Ensure slot update is sent
                    break;
                }
            }
        }

        if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            if (rotate.getValue()) {
                facePosition(pos);
            }
            BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
            try {
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 1));
                return true;
            } catch (Exception e) {
                blacklistedPositions.add(pos);
                return false;
            }
        } else {
            blacklistedPositions.add(pos);
            return false;
        }
    }

    private void breakCrystalAt(BlockPos pos) {
        EndCrystalEntity crystal = mc.world.getEntitiesByClass(EndCrystalEntity.class,
                        new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1),
                        e -> !e.isRemoved())
                .stream()
                .findFirst()
                .orElse(null);

        if (crystal != null) {
            if (rotate.getValue()) {
                faceEntity(crystal);
            }
            mc.interactionManager.attackEntity(mc.player, crystal);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void facePosition(BlockPos pos) {
        Vec3d eyesPos = mc.player.getEyePos();
        Vec3d targetPos = Vec3d.ofCenter(pos);
        double diffX = targetPos.x - eyesPos.x;
        double diffY = targetPos.y - eyesPos.y;
        double diffZ = targetPos.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    private void faceEntity(Entity entity) {
        Vec3d eyesPos = mc.player.getEyePos();
        Vec3d targetPos = entity.getPos();
        double diffX = targetPos.x - eyesPos.x;
        double diffY = targetPos.y + entity.getHeight() / 2.0 - eyesPos.y;
        double diffZ = targetPos.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
}
