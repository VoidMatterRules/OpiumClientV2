package we.devs.opium.client.modules.miscellaneous;

import we.devs.opium.Opium;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.api.utilities.ChatUtils;
import we.devs.opium.api.utilities.InventoryUtils;
import we.devs.opium.client.modules.client.ModuleCommands;
import we.devs.opium.client.values.impl.ValueEnum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@RegisterModule(name="MiddleClick", tag="Middle Click", description="Add actions to middle click.", category=Module.Category.MISCELLANEOUS)
public class ModuleMiddleClick extends Module {
    public static ModuleMiddleClick INSTANCE;
    ValueEnum mode = new ValueEnum("Mode", "Mode", "", modes.XP);
    int oldSlot = -1;
    private int delay = 0;
    public boolean xping = false;

    public ModuleMiddleClick() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        this.oldSlot = mc.player.getInventory().selectedSlot;
        int pearlSlot = InventoryUtils.findItem(Items.ENDER_PEARL, 0, 9);
        if (this.mode.getValue().equals(modes.XP)) {
            if (mc.mouse.wasMiddleButtonClicked()) {
                if (this.hotbarXP() != -1) {
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.hotbarXP()));
                    mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, mc.player.getInventory().main.get(this.hotbarXP()).getCount(), mc.player.getYaw(), mc.player.getPitch()));
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.oldSlot));
                    this.xping = true;
                }
            } else {
                this.xping = false;
            }
        } else if (this.mode.getValue().equals(modes.Pearl) && mc.mouse.wasMiddleButtonClicked() && pearlSlot != -1) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(pearlSlot));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, mc.player.getInventory().main.get(pearlSlot).getCount(), mc.player.getYaw(), mc.player.getPitch()));
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.oldSlot));
        }
        if (this.mode.getValue().equals(modes.MCF)) {
            Entity entity;
            ++this.delay;
            HitResult object = mc.crosshairTarget;
            if (object == null) {
                return;
            }
            if (object.getType() == HitResult.Type.ENTITY && (entity = ((EntityHitResult) object).getEntity()) instanceof PlayerEntity && !(entity instanceof ArmorStandEntity) && !mc.player.isDead() && mc.player.canSee(entity)) {
                String ID = entity.getName().getString();
                if (mc.mouse.wasMiddleButtonClicked() && mc.currentScreen == null && !Opium.FRIEND_MANAGER.isFriend(ID) && this.delay > 10) {
                    Opium.FRIEND_MANAGER.addFriend(ID);
                    ChatUtils.sendMessage(Formatting.GREEN + "Added " + ModuleCommands.getSecondColor() + ID + ModuleCommands.getFirstColor() + " as friend");
                    this.delay = 0;
                }
                if (mc.mouse.wasMiddleButtonClicked() && mc.currentScreen == null && Opium.FRIEND_MANAGER.isFriend(ID) && this.delay > 10) {
                    Opium.FRIEND_MANAGER.removeFriend(ID);
                    ChatUtils.sendMessage(Formatting.RED + "Removed " + ModuleCommands.getSecondColor() + ID + ModuleCommands.getFirstColor() + " as friend");
                    this.delay = 0;
                }
            }
        }
    }

    private int hotbarXP() {
        for (int i = 0; i < 9; ++i) {
            if (mc.player.getInventory().getStack(i).getItem() != Items.EXPERIENCE_BOTTLE) continue;
            return i;
        }
        return -1;
    }

    public enum modes {
        MCF,
        XP,
        Pearl
    }
}
