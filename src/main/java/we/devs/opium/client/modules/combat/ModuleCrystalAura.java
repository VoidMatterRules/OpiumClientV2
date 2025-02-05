package we.devs.opium.client.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.client.values.impl.ValueBoolean;
import we.devs.opium.client.values.impl.ValueNumber;
import we.devs.opium.api.utilities.PlayerUtils
import we.devs.opium.api.utilities.InventoryUtils

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RegisterModule(name = "CrystalAura", description = "Places and breaks crystals on bedrock/obsidian near the target", category = Module.Category.COMBAT)
public class ModuleCrystalAura extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final ValueNumber targetRange = new ValueNumber("TargetRange", "Target Range", "The Max Range a Target can be from the Player to be considered a Target", 12.5f, 1.0f, 20.0f);
    private final ValueNumber placeRange = new ValueNumber("PlaceRange", "Place Range", "The Max Range at which a crystal can be placed", 4.5, 1.0, 6.0); //Its important to keep the target Range and Place Range seperate, when calculating the best Position it needs to check if the Position is in the Range that is set by the Place Range, if not it needs to continue
    private final ValueNumber placeSpeed = new ValueNumber("PlaceSpeed", "Place Speed", "Determines how many Crystals will be attempted to place per second", 20f, 1f, 20f); //This might be scrapped, but could be used to determine the amount of tries on strict servers, like cps limits (cps are usually for break speed but you get the idea)
    private final ValueNumber placeDelay = new ValueNumber("Place Delay", "Place Delay", "Determines the Delay between each Place Action in MS", 0, 0, 1000);
    private final ValueNumber breakRange = new ValueNumber("BreakRange", "Break Range", "The Max Range at which a crystal can be broken", 4.5, 1.0, 6.0);
    private final ValueNumber breakSpeed = new ValueNumber("BreakSpeed", "Break Speed", "Determines the amount of attacks / clicks per second", 20f, 1f, 20f); //Same as PlaceSpeed, now im unsure yet what this will actually do, as the BreakDelay will automatically limit the amount of cps, should be thought over how this might get implemented / is it needed (mainly added the idea of this setting due to future)
    private final ValueNumber breakDelay = new ValueNumber("BreakDelay", "Break Delay", "Determines the Delay between each Break Action in MS", 0, 0, 1000);
    private final ValueBoolean rotate = new ValueBoolean("Rotate", "Rotate", "Turns on the Rotations", false);
    private final ValueEnum autoSwitch = new ValueEnum("AutoSwitch", "Auto Switch", "Automatically Switches either silently or to Mainhand", AutoSwitch.MainHand);
    private final ValueBoolean antiWeakness = new ValueBoolean("AntiWeakness", "Anti Weakness", "Automatically Switches to a sword to counteract weakness", AntiWeaknessMode.Normal);
    private final ValueBoolean blockPlacements = new ValueBoolean("BlockPlacements", "Block Placements", "Determines if the Client will Place Obsidian at Possible Damage Positions that do not have obsidian.", false); //Intention here is that if the client found a good position but is missing a block to place it on, it would place an obsidian block, this should be calculated in such a way that it doesnt include any positions that dont have a placeable block near them and also dont place on already placeable blocks like obi and bedrock
    private final ValueBoolean totemPopPrio = new ValueBoolean("TotemPopPrio", "Totem Pop Prio", "Determines if Totem Pops are Valued over the highest Damage that can be done to a target", true); //this would mean that if you had 2 cases: in 1 case the target would pop, but another Target in case 2 would take more damage, the totempop would be prioritised, meaning the first case would be made the target


    private final Set<BlockPos> blacklistedPositions = new HashSet<>();

    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null) return;

        PlayerEntity target = getTarget();
        if (target == null) {
            return;
        }

        BlockPos placePos = findBestPlacePosition(target); // there should also be a similar method just for breaking, so the breaking can kick in independent of the placing (usefull if a crystal is already placed and the aura should only break it)
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
    } //this code is unclear what it does to me, please make it more readable or make a comment to it, ty

    private BlockPos findBestPlacePosition(PlayerEntity target) {
        BlockPos targetPos = target.getBlockPos();
        List<BlockPos> positions = Stream.of(
                targetPos.north(),
                targetPos.south(),
                targetPos.east(),
                targetPos.west(),
                targetPos.north().down(),
                targetPos.south().down(),
                targetPos.east().down(),
                targetPos.west().down(),
                targetPos.down()
        ).collect(Collectors.toList());

        return positions.stream()
                .filter(pos -> isValidTargetBlock(pos) && target.getPos().distanceTo(Vec3d.ofCenter(pos)) >= 1)
                .filter(pos -> !blacklistedPositions.contains(pos))
                .max((pos1, pos2) -> Float.compare(calculateDamage(pos1, target), calculateDamage(pos2, target)))
                .orElse(null);
    } //make this a foreach loop, so that it quickly goes through the blocks and checks them, the one with the highest calculated damage and inside of the specified ranges should then get picked, this should also get done in a variabalie way, as currently it only checks pre defined positions

    private boolean isValidTargetBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN;
    }

    private float calculateDamage(BlockPos pos, PlayerEntity target) {
        // Simplified damage calculation
        double dist = Math.sqrt(pos.getSquaredDistance(target.getPos()));
        double exposure = 1.0 - (dist / 12.0);
        return (float) ((exposure * exposure + exposure) * 7.0 * 12.0 + 1.0);
    } // i'd love to get a short introduction to this, to later continue it

    private boolean placeCrystal(BlockPos pos) {
        int CrystalSlot = InventoryUtils.findItem(Items.END_CRYSTAL, 0, 9);
        if (CrystalSlot == -1) {
            if (autoSwitch.getValue().equals(AutoSwitch.MainHand)) {
                InventoryUtils.switchSlot(CrystalSlot, false);
            } else if (autoSwitch.getValue().equals(AutoSwitch.SilentSwitch)) {
                InventoryUtils.switchSlot(CrystalSlot, true);
            }
        } else {
            ChatUtils.sendMessage("No Crystals were found.", "CrystalAura");
        }
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
    }

    private void breakCrystalAt(BlockPos pos) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity && entity.getBlockPos().equals(pos)) {
                if (rotate.getValue()) {
                    faceEntity(entity);
                }
                if (antiWeakness.getValue().equals(AntiWeaknessMode.Normal) || antiWeakness.getValue().equals(AntiWeaknessMode.SilentSwitch)) {
                    boolean weakness = PlayerUtils.getWeakness
                    if (weakness) {
                        int slot = InventoryUtils.getSlotByClass(SwordItem.class);
                        if (mc.player.getInventory().selectedSlot != slot) {
                            if (slot == -1) {
                                return;
                            }
                            ChatUtils.sendMessage(AntiWeaknessMode.getValue()); //comment this out if it doesnt work, as its for debugging later
                            InventoryUtils.switchSlot(slot, AntiWeaknessMode.getValue().equals(AntiWeaknessMode.SilentSwitch)); //smth seems to not work correctly here as only silent switch works correctly
                        }
                    }
                }
                mc.interactionManager.attackEntity(mc.player, entity);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
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

    public enum AutoSwitch {
        None,
        MainHand,
        SilentSwitch
    }

    public enum AntiWeaknessMode {
        None,
        Normal,
        SilentSwitch
    }
}
