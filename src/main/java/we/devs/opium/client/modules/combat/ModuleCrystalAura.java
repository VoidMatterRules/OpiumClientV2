package we.devs.opium.client.modules.combat;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3d;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.api.utilities.RotationsUtil;
import we.devs.opium.client.values.impl.*;

import java.awt.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RegisterModule(name = "CrystalAura", tag = "Crystal Aura", description = "Automatically Places and Breaks Crystals", category = we.devs.opium.api.manager.module.Module.Category.COMBAT)
public class ModuleCrystalAura extends we.devs.opium.api.manager.module.Module {

    // Categories
    private final ValueCategory general = new ValueCategory("General", "General settings.");
    private final ValueCategory switchCategory = new ValueCategory("Switch", "Switch settings.");
    private final ValueCategory placeCategory = new ValueCategory("Place", "Place settings.");
    private final ValueCategory facePlaceCategory = new ValueCategory("Face Place", "Face Place settings.");
    private final ValueCategory breakCategory = new ValueCategory("Break", "Break settings.");
    private final ValueCategory pause = new ValueCategory("Pause", "Pause settings.");
    private final ValueCategory render = new ValueCategory("Render", "Render settings.");
    private final ValueCategory rotations = new ValueCategory("Rotate", "Rotate settings.");


    // General
    private final ValueNumber targetRange = new ValueNumber("TargetRange", "Target Range", "The Range in which Players get considered as possible Targets", this.general, 12.0, 1.0, 20.0);
    private final ValueBoolean predictMovement = new ValueBoolean("PredictMovement", "Predict Movement", "Attempts to Predict the Movement of the Target", this.general, true);
    private final ValueNumber minDamage = new ValueNumber("MinDamage", "Min Damage", "Minimum Damage the Crystal needs to deal to your Target", this.general, 5.0, 0.1, 36.0);
    private final ValueNumber maxSelfDamage = new ValueNumber("MaxSelfDamage", "Max Self Damage", "Maximum Damage the Crystal is allowed to deal to yourself", this.general, 36.0f, 0.0f, 36.0f);
    private final ValueBoolean antiSuicide = new ValueBoolean("AntiSuicide", "Anti Suicide", "Stops the Crystal Aura from killing yourself", this.general, true);
    private final ValueBoolean ignoreNakeds = new ValueBoolean("IgnoreNakeds", "Ignore Nakeds", "Will not Attack Players with no Armor", this.general, false);

    // Rotations
    private final ValueBoolean rotate = new ValueBoolean("Rotate", "Rotate", "Sends Rotation Packets to the server.", this.rotations, true);
    private final ValueEnum yawStepMode = new ValueEnum("YawStepMode", "Yaw Step Mode", "Determines when the client does Yaw Steps", this.rotations, YawStepMode.All);
    private final ValueNumber yawSteps = new ValueNumber("YawSteps", "Yaw Steps", "The maximum amount of degrees that the client is allowed to rotate in one tick.", this.rotations, 180.0, 1.0, 360.0);
    private final ValueBoolean smoothRotations = new ValueBoolean("SmoothRotations", "Smooth Rotations", "Makes the Client send multiple packets to the server for a smoother server side rotations.", this.rotations, false);

    // Switch
    private final ValueNumber switchDelay = new ValueNumber("SwitchDelay", "Switch Delay", "The delay in ticks to wait to break a crystal after switching hotbar slot.", this.switchCategory, 0, 0, 20);
    private final ValueEnum autoSwitchMode = new ValueEnum("SwitchMode", "Switch Mode", "Determines how, if at all, the client will switch to the Crystals", this.switchCategory, AutoSwitchMode.Normal);
    private final ValueBoolean antiGapSwitch = new ValueBoolean("AntiGapSwitch", "Anti Gap Switch", "Wont Switch to a Crystal if you are using a Gapple.", this.switchCategory, true);
    private final ValueBoolean antiWeakness = new ValueBoolean("AntiWeakness", "Anti Weakness", "Switches to a Tool for breaking Crystals, if you have the Weakness Effect.", this.switchCategory, true);

    // Place
    private final ValueBoolean doPlace = new ValueBoolean("Place", "Place", "Makes the CA place Crystals.", this.placeCategory, true);
    private final ValueNumber placeDelay = new ValueNumber("PlaceDelay", "Place Delay", "The delay in ticks to wait to place a crystal after it's exploded.", this.placeCategory, 0, 0, 20);
    private final ValueNumber placeRange = new ValueNumber("PlaceRange", "Place Range", "Range from the Player in which to place crystals.", this.placeCategory, 4.5, 0.0, 6.0);
    private final ValueNumber placeWallsRange = new ValueNumber("WallsRange", "Walls Range", "Range in which to place crystals if Blocks are in the Way.", this.placeCategory, 4.5, 0, 6);
    private final ValueBoolean placement112 = new ValueBoolean("OldPlacements", "Old Placements", "Uses pre 1.13 Crystal Placements", this.placeCategory, false);
    private final ValueEnum support = new ValueEnum("Support", "Support", "Places support blocks if no Possible Position has been found.", this.placeCategory, SupportMode.Disabled);
    private final ValueNumber supportDelay = new ValueNumber("SupportDelay", "Support Delay", "Delay in ticks after placing support block.", this.placeCategory, 1, 0, 20);

    // Face place
    private final ValueBoolean facePlace = new ValueBoolean("FacePlace", "Face Place", "If the Target is below the set health or durability, the client will Face Place.", this.facePlaceCategory, true);
    private final ValueNumber facePlaceHealth = new ValueNumber("FacePlaceHealth", "Face Place Health", "The health the target has to be at to start Face Placing.", this.facePlaceCategory, 8, 1, 36);
    private final ValueNumber facePlaceDurability = new ValueNumber("FacePlaceDurability", "Face Place Durability", "The Threshold of durability to start Face Placing", this.facePlaceCategory, 25.0, 1.0, 100.0);

    // Break
    private final ValueBoolean doBreak = new ValueBoolean("Break", "Break", "Makes the CA break Crystals.", this.breakCategory, true);
    private final ValueNumber breakDelay = new ValueNumber("BreakDelay", "Break Delay", "The delay in ticks to wait to break a crystal after it's placed.", this.breakCategory, 0, 0, 20);
    private final ValueNumber breakRange = new ValueNumber("BreakRange", "Break Range", "Range from the Player in which to break crystals.", this.breakCategory, 4.5, 0.0, 6.0);
    private final ValueNumber breakWallsRange = new ValueNumber("BreakWallsRange", "Break Walls Range", "Range in which to break crystals if Blocks are in the Way.", this.breakCategory, 4.5, 0.0, 6.0);
    private final ValueNumber breakAttempts = new ValueNumber("BreakAttempts", "Break Attempts", "How many times to hit a crystal before ignoring it.", this.breakCategory, 2, 1, 5);
    private final ValueNumber ticksExisted = new ValueNumber("TicksExisted", "Ticks Existed", "Amount of ticks a crystal has to have existed for, for it to be attacked by the CrystalAura.", this.breakCategory, 0, 0, 20);
    private final ValueNumber attackFrequency = new ValueNumber("CPSLimiter", "CPS Limiter", "Limits the CPS to the maximum of the set Value", this.breakCategory, 25, 1, 30);
    private final ValueBoolean fastBreak = new ValueBoolean("InstantBreak", "Instant Break", "Ignores break delays and tries to break the crystal the Instant it gets Spawned in on the Clientside.", this.breakCategory, false);
    private final ValueBoolean smartDelay = new ValueBoolean("SmartDelay", "Smart Delay", "Only Breaks Crystal when the Target is Not inside of a Damage Tick", this.breakCategory, false);


    // Pause
    private final ValueEnum pauseOnUse = new ValueEnum("PauseOnUse", "Pause On Use", "Which Parts of the CrystalAura process should be paused when using an Item.", this.pause, PauseMode.None);
    private final ValueEnum pauseOnMine = new ValueEnum("PauseOnMine", "Pause On Mine", "Which Parts of the CrystalAura process should be paused when mining a Block.", this.pause, PauseMode.None);
    private final ValueBoolean pauseOnLag = new ValueBoolean("PauseOnLag", "Pause On Lag", "Pauses when detecting Server-Lag", this.pause, true);
    private final ValueNumber pauseHealth = new ValueNumber("PauseHealth", "Pause Health", "Pauses when you go below a certain health.", this.pause, 0.0, 0.0, 36.0);

    // Render
    private final ValueEnum swingMode = new ValueEnum("SwingMode", "Swing Mode", "How to swing when placing.", this.render, SwingMode.Both);
    private final ValueEnum renderMode = new ValueEnum("RenderMode", "Render Mode", "The mode to render in.", this.render, RenderMode.Normal);
    private final ValueBoolean renderPlace = new ValueBoolean("RenderPlace", "Render Place", "Renders a block overlay over the block the crystals are being placed on.", this.render, true);
    private final ValueNumber placeRenderTime = new ValueNumber("PlaceTime", "Place Time", "How long to render placements.", this.render, 10, 0, 20);
    private final ValueBoolean renderBreak = new ValueBoolean("RenderBreak", "Render Break", "Renders a block overlay over the block the crystals are broken on.", this.render, true);
    private final ValueNumber breakRenderTime = new ValueNumber("BreakTime", "Break Time", "How long to render breaking for.", this.render, 10, 0, 20);
    private final ValueNumber smoothness = new ValueNumber("Smoothness", "Smoothness", "When Smooth Render is activated determines how smoothly the render should move around.", this.render, 10, 0, 20);
    private final ValueNumber height = new ValueNumber("Height", "Height", "How tall the gradient should be.", this.render, 1.0, 0.0, 1.0);
    private final ValueNumber renderTime = new ValueNumber("RenderTime", "Render Time", "How long to render placements.", this.render, 10, 0, 20);
    private final ValueEnum shapeMode = new ValueEnum("ShapeMode", "Shape Mode", "How the shapes are rendered.", this.render, ShapeMode.Both);
    private final ValueColor sideColor = new ValueColor("SideColor", "Side Color", "The side color of the block overlay.", this.render, new java.awt.Color(255, 255, 255, 45));
    private final ValueColor lineColor = new ValueColor("LineColor", "Line Color", "The line color of the block overlay.", this.render, new java.awt.Color(255, 255, 255, 45));
    private final ValueBoolean renderDamageText = new ValueBoolean("Damage", "Damage", "Renders crystal damage text in the block overlay.", this.render, true);
    private final ValueNumber damageTextScale = new ValueNumber("DamageScale", "Damage Scale", "How big the damage text should be.", this.render, 1.25, 1.0, 4.0);

    // Fields
    private Item mainItem, offItem;

    private int breakTimer, placeTimer, switchTimer, ticksPassed;
    private final List<LivingEntity> targets = new ArrayList<>();

    private final Vec3d vec3d = new Vec3d(0, 0, 0);
    private final Vec3d playerEyePos = new Vec3d(0, 0, 0);
    private final Vector3d vec3 = new Vector3d();
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private final Box box = new Box(0, 0, 0, 0, 0, 0);

    private final Vec3d vec3dRayTraceEnd = new Vec3d(0, 0, 0);
    private RaycastContext raycastContext;

    private final IntSet placedCrystals = new IntOpenHashSet();
    private boolean placing;
    private int placingTimer;
    public int kaTimer;
    private final BlockPos.Mutable placingCrystalBlockPos = new BlockPos.Mutable();

    private final IntSet removed = new IntOpenHashSet();
    private final Int2IntMap attemptedBreaks = new Int2IntOpenHashMap();
    private final Int2IntMap waitingToExplode = new Int2IntOpenHashMap();
    private int attacks;

    private double serverYaw;

    private LivingEntity bestTarget;
    private double bestTargetDamage;
    private int bestTargetTimer;

    private boolean didRotateThisTick;
    private boolean isLastRotationPos;
    private final Vec3d lastRotationPos = new Vec3d(0, 0, 0);
    private double lastYaw, lastPitch;
    private int lastRotationTimer;

    private int placeRenderTimer, breakRenderTimer;
    private final BlockPos.Mutable placeRenderPos = new BlockPos.Mutable();
    private final BlockPos.Mutable breakRenderPos = new BlockPos.Mutable();
    private Box renderBoxOne, renderBoxTwo;

    private double renderDamage;


    @Override
    public void onEnable() {
        breakTimer = 0;
        placeTimer = 0;
        ticksPassed = 0;

        if (mc.player != null) {
            raycastContext = new RaycastContext(new Vec3d(0, 0, 0), new Vec3d(0, 0, 0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            serverYaw = mc.player.getYaw();
        }
        placing = false;
        placingTimer = 0;
        kaTimer = 0;

        attacks = 0;


        bestTargetDamage = 0;
        bestTargetTimer = 0;

        lastRotationTimer = getLastRotationStopDelay();

        placeRenderTimer = 0;
        breakRenderTimer = 0;
    }

    @Override
    public void onDisable() {
        targets.clear();

        placedCrystals.clear();

        attemptedBreaks.clear();
        waitingToExplode.clear();

        removed.clear();

        bestTarget = null;
    }

    private int getLastRotationStopDelay() {
        return Math.max(10, placeDelay.getValue().intValue() / 2 + breakDelay.getValue().intValue() / 2 + 10);
    }

    @Override
    public void onPreTick(we.devs.opium.client.events.TickEvent.Pre event) {
        // Update last rotation
        didRotateThisTick = false;
        lastRotationTimer++;

        // Decrement placing timer
        if (placing) {
            if (placingTimer > 0) placingTimer--;
            else placing = false;
        }

        if (kaTimer > 0) kaTimer--;

        if (ticksPassed < 20) ticksPassed++;
        else {
            ticksPassed = 0;
            attacks = 0;
        }

        // Decrement best target timer
        if (bestTargetTimer > 0) bestTargetTimer--;
        bestTargetDamage = 0;

        // Decrement break, place and switch timers
        if (breakTimer > 0) breakTimer--;
        if (placeTimer > 0) placeTimer--;
        if (switchTimer > 0) switchTimer--;

        // Decrement render timers
        if (placeRenderTimer > 0) placeRenderTimer--;
        if (breakRenderTimer > 0) breakRenderTimer--;

        mainItem = mc.player.getMainHandStack().getItem();
        offItem = mc.player.getOffHandStack().getItem();

        // Update waiting to explode crystals and mark them as existing if reached threshold
        for (IntIterator it = waitingToExplode.keySet().iterator(); it.hasNext(); ) {
            int id = it.nextInt();
            int ticks = waitingToExplode.get(id);

            if (ticks > 3) {
                it.remove();
                removed.remove(id);
            } else {
                waitingToExplode.put(id, ticks + 1);
            }
        }

        // Set player eye pos
        ((we.devs.opium.asm.mixins.IVec3d) playerEyePos).opium$set(mc.player.getPos().x, mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getPos().z);

        // Find targets, break and place
        findTargets();

        if (!targets.isEmpty()) {
            if (!didRotateThisTick) doBreak();
            if (!didRotateThisTick) doPlace();
        }
        if (rotate.getValue() && lastRotationTimer < getLastRotationStopDelay() && !didRotateThisTick) {
            if (smoothRotations.getValue()) {
                RotationsUtil.smoothRotate(isLastRotationPos ? lastRotationPos : new Vec3d(lastYaw, lastPitch, 0), 5.0);
            } else {
                if (rotate.getValue() && lastRotationTimer < getLastRotationStopDelay() && !didRotateThisTick) {
                    Vec2f targetRotation = isLastRotationPos
                            ? RotationsUtil.calculateRotation(lastRotationPos)
                            : new Vec2f((float) lastYaw, (float) lastPitch);

                    RotationsUtil.setCamRotation(targetRotation.x, targetRotation.y);
                    RotationsUtil.sendRotationPacket(targetRotation.x, targetRotation.y);
                }
            }
        }
    }


    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof EndCrystalEntity)) return;

        if (placing && event.entity.getBlockPos().equals(placingCrystalBlockPos)) {
            placing = false;
            placingTimer = 0;
            placedCrystals.add(event.entity.getId());
        }

        if (fastBreak.get() && !didRotateThisTick && attacks < attackFrequency.get()) {
            float damage = getBreakDamage(event.entity, true);
            if (damage > minDamage.get()) doBreak(event.entity);
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        if (event.entity instanceof EndCrystalEntity) {
            placedCrystals.remove(event.entity.getId());
            removed.remove(event.entity.getId());
            waitingToExplode.remove(event.entity.getId());
        }
    }

    private void setRotation(boolean isPos, Vec3d pos, double yaw, double pitch) {
        didRotateThisTick = true;
        isLastRotationPos = isPos;

        if (isPos) ((we.devs.opium.asm.mixins.IVec3d) lastRotationPos).opium$set(pos.x, pos.y, pos.z);
        else {
            lastYaw = yaw;
            lastPitch = pitch;
        }

        lastRotationTimer = 0;
    }

    // Break

    private void doBreak() {
        if (!doBreak.getValue() || breakTimer > 0 || switchTimer > 0 || attacks >= attackFrequency.getValue().intValue())
            return;
        if (shouldPause(PauseMode.Break)) return;

        float bestDamage = 0;
        Entity crystal = null;

        // Find best crystal to break
        for (Entity entity : mc.world.getEntities()) {
            float damage = getBreakDamage(entity, true);

            if (damage > bestDamage) {
                bestDamage = damage;
                crystal = entity;
            }
        }

        // Break the crystal
        if (crystal != null) doBreak(crystal);
    }

    private float getBreakDamage(Entity entity, boolean checkCrystalAge) {
        if (!(entity instanceof EndCrystalEntity)) return 0;

        // Check if it should already be removed
        if (removed.contains(entity.getId())) return 0;

        // Check attempted breaks
        if (attemptedBreaks.get(entity.getId()) > breakAttempts.getValue().intValue()) return 0;

        // Check crystal age
        if (checkCrystalAge && entity.age < ticksExisted.getValue().intValue()) return 0;

        // Check range
        if (isOutOfRange(entity.getPos(), entity.getBlockPos(), false)) return 0;

        // Check damage to self and anti suicide
        blockPos.set(entity.getBlockPos()).move(0, -1, 0);
        float selfDamage = DamageUtils.crystalDamage(mc.player, entity.getPos(), predictMovement.getValue(), blockPos);
        if (selfDamage > maxSelfDamage.getValue().floatValue() || (antiSuicide.getValue() && selfDamage >= EntityUtils.getTotalHealth(mc.player)))
            return 0;

        // Check damage to targets and face place
        float damage = getDamageToTargets(entity.getPos(), blockPos, true, false);
        boolean shouldFacePlace = shouldFacePlace();
        double minimumDamage = shouldFacePlace ? Math.min(minDamage.getValue().doubleValue(), 1.5d) : minDamage.getValue().doubleValue();

        if (damage < minimumDamage) return 0f;

        return damage;
    }

    private void doBreak(Entity crystal) {
        // Anti weakness
        if (antiWeakness.getValue()) {
            StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
            StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);

            // Check for strength
            if (weakness != null && (strength == null || strength.getAmplifier() <= weakness.getAmplifier())) {
                // Check if the item in your hand is already valid
                if (!isValidWeaknessItem(mc.player.getMainHandStack())) {
                    // Find valid item to break with
                    if (!InvUtils.swap(InvUtils.findInHotbar(this::isValidWeaknessItem).slot(), false)) return;

                    switchTimer = 1;
                    return;
                }
            }
        }

        // Rotate and attack
        boolean attacked = true;

        if (rotate.getValue()) {
            double yaw = Rotations.getYaw(crystal);
            double pitch = Rotations.getPitch(crystal, Target.Feet);

            if (doYawSteps(yaw, pitch)) {
                setRotation(true, crystal.getPos(), 0, 0);
                Rotations.rotate(yaw, pitch, 50, () -> attackCrystal(crystal));

                breakTimer = breakDelay.getValue().intValue();
            } else {
                attacked = false;
            }
        } else {
            attackCrystal(crystal);
            breakTimer = breakDelay.getValue().intValue();
        }

        if (attacked) {
            // Update state
            removed.add(crystal.getId());
            attemptedBreaks.put(crystal.getId(), attemptedBreaks.get(crystal.getId()) + 1);
            waitingToExplode.put(crystal.getId(), 0);

            // Break render
            breakRenderPos.set(crystal.getBlockPos().down());
            breakRenderTimer = breakRenderTime.getValue().intValue();
        }
    }

    private boolean isValidWeaknessItem(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof IMiningToolItem) || itemStack.getItem() instanceof HoeItem)
            return false;

        ToolMaterial material = ((IMiningToolItem) itemStack.getItem()).meteor$getMaterial();
        return material == ToolMaterial.DIAMOND || material == ToolMaterial.NETHERITE;
    }

    private void attackCrystal(Entity entity) {
        // Attack
        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));

        Hand hand = InvUtils.findInHotbar(Items.END_CRYSTAL).getHand();
        if (hand == null) hand = Hand.MAIN_HAND;

        if (swingMode.getValue().equals(SwingMode.Client)) mc.player.swingHand(hand);
        if (swingMode.getValue().equals(SwingMode.Packet))
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));

        attacks++;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            switchTimer = switchDelay.getValue().intValue();
        }
    }

    // Place

    private void doPlace() {
        if (!doPlace.getValue() || placeTimer > 0) return;
        if (shouldPause(PauseMode.Place)) return;

        // Return if there are no crystals in hotbar or offhand
        if (!InvUtils.testInHotbar(Items.END_CRYSTAL)) return;

        // Return if there are no crystals in either hand and auto switch mode is none
        if (!autoSwitchMode.getValue().equals(AutoSwitchMode.None)) {
            if (antiGapSwitch.getValue() && autoSwitchMode.getValue().equals(AutoSwitchMode.Normal) && offItem != Items.END_CRYSTAL) {
                if (mainItem == Items.ENCHANTED_GOLDEN_APPLE
                        || offItem == Items.ENCHANTED_GOLDEN_APPLE
                        || mainItem == Items.GOLDEN_APPLE
                        || offItem == Items.GOLDEN_APPLE) return;
            }
        } else if (mainItem != Items.END_CRYSTAL && offItem != Items.END_CRYSTAL) return;

        // Check for multiplace
        for (Entity entity : mc.world.getEntities()) {
            if (getBreakDamage(entity, false) > 0) return;
        }

        // Setup variables
        AtomicDouble bestDamage = new AtomicDouble(0);
        AtomicReference<BlockPos.Mutable> bestBlockPos = new AtomicReference<>(new BlockPos.Mutable());
        AtomicBoolean isSupport = new AtomicBoolean(!support.getValue().equals(SupportMode.Disabled));

        // Find best position to place the crystal on
        BlockIterator.register((int) Math.ceil(placeRange.getValue().doubleValue()), (int) Math.ceil(placeRange.getValue().doubleValue()), (bp, blockState) -> {
            // Check if its bedrock or obsidian and return if isSupport is false
            boolean hasBlock = blockState.isOf(Blocks.BEDROCK) || blockState.isOf(Blocks.OBSIDIAN);
            if (!hasBlock && (!isSupport.get() || !blockState.isReplaceable())) return;

            // Check if there is air on top
            blockPos.set(bp.getX(), bp.getY() + 1, bp.getZ());
            if (!mc.world.getBlockState(blockPos).isAir()) return;

            if (placement112.getValue()) {
                blockPos.move(0, 1, 0);
                if (!mc.world.getBlockState(blockPos).isAir()) return;
            }

            // Check range
            ((we.devs.opium.asm.mixins.IVec3d) vec3d).opium$set(bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5);
            blockPos.set(bp).move(0, 1, 0);
            if (isOutOfRange(vec3d, blockPos, true)) return;

            // Check damage to self and anti suicide
            float selfDamage = DamageUtils.crystalDamage(mc.player, vec3d, predictMovement.getValue(), bp);
            if (selfDamage > maxSelfDamage.getValue().floatValue() || (antiSuicide.getValue() && selfDamage >= EntityUtils.getTotalHealth(mc.player)))
                return;

            // Check damage to targets and face place
            float damage = getDamageToTargets(vec3d, bp, false, !hasBlock && support.getValue().equals(SupportMode.Fast));

            boolean shouldFacePlace = shouldFacePlace();
            double minimumDamage = Math.min(minDamage.getValue().doubleValue(), shouldFacePlace ? 1.5 : minDamage.getValue().doubleValue());

            if (damage < minimumDamage) return;

            // Check if it can be placed
            double x = bp.getX();
            double y = bp.getY() + 1;
            double z = bp.getZ();
            ((IBox) box).meteor$set(x, y, z, x + 1, y + (placement112.getValue() ? 1 : 2), z + 1);

            if (intersectsWithEntities(box)) return;

            // Compare damage
            if (damage > bestDamage.get() || (isSupport.get() && hasBlock)) {
                bestDamage.set(damage);
                bestBlockPos.get().set(bp);
            }

            if (hasBlock) isSupport.set(false);
        });

        // Place the crystal
        BlockIterator.after(() -> {
            if (bestDamage.get() == 0) return;

            BlockHitResult result = getPlaceInfo(bestBlockPos.get());

            ((we.devs.opium.asm.mixins.IVec3d) vec3d).opium$set(
                    result.getBlockPos().getX() + 0.5 + result.getSide().getVector().getX() * 1.0 / 2.0,
                    result.getBlockPos().getY() + 0.5 + result.getSide().getVector().getY() * 1.0 / 2.0,
                    result.getBlockPos().getZ() + 0.5 + result.getSide().getVector().getZ() * 1.0 / 2.0
            );

            if (rotate.getValue()) {
                double yaw = Rotations.getYaw(vec3d);
                double pitch = Rotations.getPitch(vec3d);

                if (yawStepMode.getValue().equals(YawStepMode.Break) || doYawSteps(yaw, pitch)) {
                    setRotation(true, vec3d, 0, 0);
                    Rotations.rotate(yaw, pitch, 50, () -> placeCrystal(result, bestDamage.get(), isSupport.get() ? bestBlockPos.get() : null));

                    placeTimer += placeDelay.getValue().intValue();
                }
            } else {
                placeCrystal(result, bestDamage.get(), isSupport.get() ? bestBlockPos.get() : null);
                placeTimer += placeDelay.getValue().intValue();
            }
        });
    }

    private BlockHitResult getPlaceInfo(BlockPos blockPos) {
        ((we.devs.opium.asm.mixins.IVec3d) vec3d).opium$set(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        for (Direction side : Direction.values()) {
            ((we.devs.opium.asm.mixins.IVec3d) vec3dRayTraceEnd).opium$set(
                    blockPos.getX() + 0.5 + side.getVector().getX() * 0.5,
                    blockPos.getY() + 0.5 + side.getVector().getY() * 0.5,
                    blockPos.getZ() + 0.5 + side.getVector().getZ() * 0.5
            );

            ((IRaycastContext) raycastContext).meteor$set(vec3d, vec3dRayTraceEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.raycast(raycastContext);

            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos)) {
                return result;
            }
        }

        Direction side = blockPos.getY() > vec3d.y ? Direction.DOWN : Direction.UP;
        return new BlockHitResult(vec3d, side, blockPos, false);
    }

    private void placeCrystal(BlockHitResult result, double damage, BlockPos supportBlock) {
        // Switch
        Item targetItem = supportBlock == null ? Items.END_CRYSTAL : Items.OBSIDIAN;

        FindItemResult item = InvUtils.findInHotbar(targetItem);
        if (!item.found()) return;

        int prevSlot = mc.player.getInventory().selectedSlot;

        if (!autoSwitchMode.getValue().equals(AutoSwitchMode.None) && !item.isOffhand())
            InvUtils.swap(item.slot(), false);

        Hand hand = item.getHand();
        if (hand == null) return;

        // Place
        if (supportBlock == null) {
            // Place crystal
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, result, 0));

            if (swingMode.getValue().equals(SwingMode.Client)) {
                mc.player.swingHand(hand);
            }
            if (swingMode.getValue().equals(SwingMode.Packet)) {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
            }

            placing = true;
            placingTimer = 4;
            kaTimer = 8;
            placingCrystalBlockPos.set(result.getBlockPos()).move(0, 1, 0);

            placeRenderPos.set(result.getBlockPos());
            renderDamage = damage;

            if (renderMode.getValue().equals(RenderMode.Normal)) {
                placeRenderTimer = placeRenderTime.getValue().intValue();
            } else {
                placeRenderTimer = renderTime.getValue().intValue();
                if (renderMode.getValue().equals(RenderMode.Fading)) {
                    RenderUtils.renderTickingBlock(
                            placeRenderPos, sideColor.getValue(),
                            lineColor.getValue(), shapeMode.getValue(),
                            0, renderTime.getValue(), true,
                            false
                    );
                }
            }
        } else {
            // Place support block
            BlockUtils.place(supportBlock, item, false, 0, SwingMode.Both, true, false);
            placeTimer += supportDelay.getValue().intValue();

            if (supportDelay.getValue().intValue() == 0) placeCrystal(result, damage, null);
        }

        // Switch back
        if (autoSwitchMode.getValue().equals(AutoSwitchMode.Silent)) InvUtils.swap(prevSlot, false);
    }

    // Yaw steps

    @EventHandler
    private void onPacketSent(PacketEvent.Sent event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            serverYaw = ((PlayerMoveC2SPacket) event.packet).getYaw((float) serverYaw);
        }
    }

    public boolean doYawSteps(double targetYaw, double targetPitch) {
        targetYaw = MathHelper.wrapDegrees(targetYaw) + 180;
        double serverYaw = MathHelper.wrapDegrees(this.serverYaw) + 180;

        if (distanceBetweenAngles(serverYaw, targetYaw) <= yawSteps.getValue().doubleValue()) return true;

        double delta = Math.abs(targetYaw - serverYaw);
        double yaw = this.serverYaw;

        if (serverYaw < targetYaw) {
            if (delta < 180) yaw += yawSteps.getValue().doubleValue();
            else yaw -= yawSteps.getValue().doubleValue();
        } else {
            if (delta < 180) yaw -= yawSteps.getValue().doubleValue();
            else yaw += yawSteps.getValue().doubleValue();
        }

        setRotation(false, null, yaw, targetPitch);
        Rotations.rotate(yaw, targetPitch, -100, null); // Priority -100 so it sends the packet as the last one, im pretty sure it doesn't matte but idc
        return false;
    }

    private static double distanceBetweenAngles(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360;
        return phi > 180 ? 360 - phi : phi;
    }

    // Face place

    private boolean shouldFacePlace() {
        if (!facePlace.getValue()) return false;

        // Checks if the provided crystal position should face place to any target
        for (LivingEntity target : targets) {
            if (EntityUtils.getTotalHealth(target) <= facePlaceHealth.getValue()) return true;

            for (ItemStack itemStack : target.getArmorItems()) {
                if ((double) (itemStack.getMaxDamage() - itemStack.getDamage()) / itemStack.getMaxDamage() * 100 <= facePlaceDurability.getValue().doubleValue())
                    return true;

            }
        }

        return false;
    }

    // Others

    private boolean shouldPause(PauseMode process) {
        if (mc.player.isUsingItem() || mc.options.useKey.isPressed()) {
            if (pauseOnUse.getValue().equals(PauseMode.Both)) return true;
        }

        if (pauseOnLag.getValue() && TickRate.INSTANCE.getTimeSinceLastTick() >= 1.0f) return true;
        if (pauseOnMine.getValue().equals(process) && mc.interactionManager.isBreakingBlock()) return true;
        return (EntityUtils.getTotalHealth(mc.player) <= pauseHealth.getValue());
    }

    private boolean isOutOfRange(Vec3d vec3d, BlockPos blockPos, boolean place) {
        ((IRaycastContext) raycastContext).meteor$set(playerEyePos, vec3d, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);

        BlockHitResult result = mc.world.raycast(raycastContext);

        if (result == null || !result.getBlockPos().equals(blockPos)) // Is behind wall
            return !PlayerUtils.isWithin(vec3d, (place ? placeWallsRange : breakWallsRange).getValue());
        return !PlayerUtils.isWithin(vec3d, (place ? placeRange : breakRange).getValue());
    }

    private LivingEntity getNearestTarget() {
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity target : targets) {
            double distance = PlayerUtils.squaredDistanceTo(target);

            if (distance < nearestDistance) {
                nearestTarget = target;
                nearestDistance = distance;
            }
        }

        return nearestTarget;
    }

    private float getDamageToTargets(Vec3d vec3d, BlockPos obsidianPos, boolean breaking, boolean fast) {
        float damage = 0;

        if (fast) {
            LivingEntity target = getNearestTarget();
            if (!(smartDelay.getValue() && breaking && target.hurtTime > 0))
                damage = DamageUtils.crystalDamage(target, vec3d, predictMovement.get(), obsidianPos);
        } else {
            for (LivingEntity target : targets) {
                if (smartDelay.getValue() && breaking && target.hurtTime > 0) continue;

                float dmg = DamageUtils.crystalDamage(target, vec3d, predictMovement.getValue(), obsidianPos);

                // Update best target
                if (dmg > bestTargetDamage) {
                    bestTarget = target;
                    bestTargetDamage = dmg;
                    bestTargetTimer = 10;
                }

                damage += dmg;
            }
        }

        return damage;
    }

    @Override
    public String getInfoString() {
        return bestTarget != null && bestTargetTimer > 0 ? EntityUtils.getName(bestTarget) : null;
    }

    private void findTargets() {
        targets.clear();

        // Living Entities
        for (Entity entity : mc.world.getEntities()) {
            // Ignore non-living
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            // Player
            if (livingEntity instanceof PlayerEntity player) {
                if (player.getAbilities().creativeMode || livingEntity == mc.player) continue;
                if (!player.isAlive() || !Friends.get().shouldAttack(player)) continue;

                if (ignoreNakeds.getValue()) {
                    if (player.getOffHandStack().isEmpty()
                            && player.getMainHandStack().isEmpty()
                            && player.getInventory().armor.get(0).isEmpty()
                            && player.getInventory().armor.get(1).isEmpty()
                            && player.getInventory().armor.get(2).isEmpty()
                            && player.getInventory().armor.get(3).isEmpty()
                    ) continue;
                }
            }

            // Animals, water animals, monsters, bats, misc
            if (!(entities.get().contains(livingEntity.getType()))) continue;

            // Close enough to damage
            if (livingEntity.squaredDistanceTo(mc.player) > targetRange.getValue().doubleValue() * targetRange.getValue().doubleValue())
                continue;

            targets.add(livingEntity);
        }
    }

    private boolean intersectsWithEntities(Box box) {
        return EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !removed.contains(entity.getId()));
    }

    // Rendering

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (renderMode.getValue().equals(RenderMode.None)) return;

        switch (renderMode.getValue()) {
            case Normal -> {
                if (renderPlace.getValue() && placeRenderTimer > 0) {
                    event.renderer.box(placeRenderPos, sideColor.getValue(), lineColor.getValue(), shapeMode.getValue(), 0);
                }
                if (renderBreak.getValue() && breakRenderTimer > 0) {
                    event.renderer.box(breakRenderPos, sideColor.getValue(), lineColor.getValue(), shapeMode.getValue(), 0);
                }
            }

            case Smooth -> {
                if (placeRenderTimer <= 0) return;

                if (renderBoxOne == null) renderBoxOne = new Box(placeRenderPos);
                if (renderBoxTwo == null) renderBoxTwo = new Box(placeRenderPos);
                else ((IBox) renderBoxTwo).meteor$set(placeRenderPos);

                double offsetX = (renderBoxTwo.minX - renderBoxOne.minX) / smoothness.getValue().doubleValue();
                double offsetY = (renderBoxTwo.minY - renderBoxOne.minY) / smoothness.getValue().doubleValue();
                double offsetZ = (renderBoxTwo.minZ - renderBoxOne.minZ) / smoothness.getValue().doubleValue();

                ((IBox) renderBoxOne).meteor$set(
                        renderBoxOne.minX + offsetX,
                        renderBoxOne.minY + offsetY,
                        renderBoxOne.minZ + offsetZ,
                        renderBoxOne.maxX + offsetX,
                        renderBoxOne.maxY + offsetY,
                        renderBoxOne.maxZ + offsetZ
                );

                event.renderer.box(renderBoxOne, sideColor.getValue(), lineColor.getValue(), shapeMode.getValue(), 0);
            }

            case Gradient -> {
                if (placeRenderTimer <= 0) return;

                Color bottom = new Color(0, 0, 0, 0);

                int x = placeRenderPos.getX();
                int y = placeRenderPos.getY() + 1;
                int z = placeRenderPos.getZ();

                if (shapeMode.getValue().sides()) {
                    event.renderer.quadHorizontal(x, y, z, x + 1, z + 1, sideColor.get());
                    event.renderer.gradientQuadVertical(x, y, z, x + 1, y - height.get(), z, bottom, sideColor.get());
                    event.renderer.gradientQuadVertical(x, y, z, x, y - height.get(), z + 1, bottom, sideColor.get());
                    event.renderer.gradientQuadVertical(x + 1, y, z, x + 1, y - height.get(), z + 1, bottom, sideColor.get());
                    event.renderer.gradientQuadVertical(x, y, z + 1, x + 1, y - height.get(), z + 1, bottom, sideColor.get());
                }

                if (shapeMode.getValue().lines()) {
                    event.renderer.line(x, y, z, x + 1, y, z, lineColor.get());
                    event.renderer.line(x, y, z, x, y, z + 1, lineColor.get());
                    event.renderer.line(x + 1, y, z, x + 1, y, z + 1, lineColor.get());
                    event.renderer.line(x, y, z + 1, x + 1, y, z + 1, lineColor.get());

                    event.renderer.line(x, y, z, x, y - height.get(), z, lineColor.get(), bottom);
                    event.renderer.line(x + 1, y, z, x + 1, y - height.get(), z, lineColor.get(), bottom);
                    event.renderer.line(x, y, z + 1, x, y - height.get(), z + 1, lineColor.get(), bottom);
                    event.renderer.line(x + 1, y, z + 1, x + 1, y - height.get(), z + 1, lineColor.get(), bottom);
                }
            }
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (renderMode.get() == RenderMode.None || !renderDamageText.get()) return;
        if (placeRenderTimer <= 0 && breakRenderTimer <= 0) return;

        if (renderMode.get() == RenderMode.Smooth) {
            if (renderBoxOne == null) return;
            vec3.set(renderBoxOne.minX + 0.5, renderBoxOne.minY + 0.5, renderBoxOne.minZ + 0.5);
        } else vec3.set(placeRenderPos.getX() + 0.5, placeRenderPos.getY() + 0.5, placeRenderPos.getZ() + 0.5);

        if (NametagUtils.to2D(vec3, damageTextScale.get())) {
            NametagUtils.begin(vec3);
            TextRenderer.get().begin(1, false, true);

            String text = String.format("%.1f", renderDamage);
            double w = TextRenderer.get().getWidth(text) / 2;
            TextRenderer.get().render(text, -w, 0, damageColor.get(), true);

            TextRenderer.get().end();
            NametagUtils.end();
        }
    }

    public enum YawStepMode {
        Break,
        All,
    }

    public enum AutoSwitchMode {
        Normal,
        Silent,
        None
    }

    public enum SupportMode {
        Disabled,
        Accurate,
        Fast
    }

    public enum PauseMode {
        Both,
        Place,
        Break,
        None;

        public boolean equals(PauseMode process) {
            return this == process || this == PauseMode.Both;
        }
    }

    public enum SwingMode {
        Both,
        Packet,
        Client,
        None;

        public boolean packet() {
            return this == Packet || this == Both;
        }

        public boolean client() {
            return this == Client || this == Both;
        }
    }

    public enum RenderMode {
        Normal,
        Smooth,
        Fading,
        Gradient,
        None
    }

    public enum ShapeMode {
        Lines,
        Sides,
        Both;
    }
}
