package we.devs.opium.api.utilities;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FakePlayerEntity extends OtherClientPlayerEntity implements IMinecraft {

    private static final AtomicInteger CURRENT_ID = new AtomicInteger(1_000_000);
    private final PlayerEntity originalPlayer;

    // Constructor with player and custom name
    public FakePlayerEntity(PlayerEntity originalPlayer, String customName) {
        super(
                MinecraftClient.getInstance().world,
                new GameProfile(UUID.randomUUID(), customName)
        );
        this.originalPlayer = originalPlayer;
        initializeFakePlayer(originalPlayer);
    }

    // Constructor with player and custom GameProfile
    public FakePlayerEntity(PlayerEntity originalPlayer, GameProfile profile) {
        super(MinecraftClient.getInstance().world, profile);
        this.originalPlayer = originalPlayer;
        initializeFakePlayer(originalPlayer);
    }

    // Constructor with player, using their in-game name
    public FakePlayerEntity(PlayerEntity originalPlayer) {
        this(originalPlayer, originalPlayer.getName().getString());
    }

    // Shared initialization logic for all constructors
    private void initializeFakePlayer(PlayerEntity originalPlayer) {
        this.copyPositionAndRotation(originalPlayer);
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
        this.headYaw = originalPlayer.headYaw;
        this.prevHeadYaw = this.headYaw;
        this.bodyYaw = originalPlayer.bodyYaw;
        this.prevBodyYaw = this.bodyYaw;

        // Copy player model parts and attributes
        this.dataTracker.set(
                PlayerEntity.PLAYER_MODEL_PARTS,
                originalPlayer.getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS)
        );
        this.getAttributes().setFrom(originalPlayer.getAttributes());

        // Copy player state and inventory
        this.setPose(originalPlayer.getPose());
        this.setHealth(originalPlayer.getHealth());
        this.setAbsorptionAmount(originalPlayer.getAbsorptionAmount());
        this.getInventory().clone(originalPlayer.getInventory());

        // Assign a unique entity ID
        this.setId(CURRENT_ID.incrementAndGet());
        this.age = 100;
    }

    // Spawn the fake player in the world
    public void spawn() {
        if (mc.world != null) {
            this.unsetRemoved();
            mc.world.addEntity(this);
        }
    }

    // Remove the fake player from the world
    public void despawn() {
        if (mc.world != null) {
            mc.world.removeEntity(this.getId(), RemovalReason.DISCARDED);
            this.setRemoved(RemovalReason.DISCARDED);
        }
    }

    // Prevents certain functionality from being triggered
    @Override
    public boolean shouldRenderName() {
        return false;
    }

    // Returns the original player
    public PlayerEntity getOriginalPlayer() {
        return this.originalPlayer;
    }
}
