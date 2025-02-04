package we.devs.opium.client.modules.player;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.api.utilities.ChatUtils;
import we.devs.opium.api.utilities.InventoryUtils;
import we.devs.opium.client.values.impl.ValueBoolean;
import we.devs.opium.client.values.impl.ValueNumber;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RegisterModule(name = "Elytra Swap", tag = "Elytra Swap", description = "Automatically swap elytra and chestplate", category = Module.Category.PLAYER)
public class ModuleElytraSwap extends Module {

    ValueBoolean autoFirework = new ValueBoolean("AutoFirework", "Auto Firework", "Automatically uses a Firework when switching to an Elytra", true);
    ValueBoolean strictSwitch = new ValueBoolean("StrictSwitch", "Strict Switch", "Switches to fireworks like Vanilla", false);
    ValueNumber delay = new ValueNumber("CustomDelay", "Custom Delay", "Delay", 4, 1, 20);

    @Override
    public void onEnable() {
        if(nullCheck()) {
            disable(false);
            return;
        }

        int lvl = -1;
        int armorSlot = -1;
        for (int i = 0; i <= 39; i++) {
            assert mc.player != null;
            Item item = mc.player.getInventory().getStack(i).getItem();
            int itemLvl = getLevel(item);
            if(itemLvl > lvl) {
                armorSlot = i;
                lvl = itemLvl;
            }
        }
        int elytraSlot = InventoryUtils.findItem(Items.ELYTRA, 0, 39);
        if(elytraSlot == 38 && armorSlot != -1) {
            moveItem(armorSlot < 9 ? armorSlot + 36 : armorSlot, 6);
        } else if(armorSlot == 38 && elytraSlot != -1) {
            moveItem(elytraSlot < 9 ? elytraSlot + 36 : elytraSlot, 6);
            if (autoFirework.getValue()) {
                int fireworkSlot = InventoryUtils.findItem(Items.FIREWORK_ROCKET, 0, 36);
                ChatUtils.sendMessage("Firework Slot: " + fireworkSlot);
                if (fireworkSlot != -1) {
                    ChatUtils.sendMessage("Switching Slot...");
                    int lastSlot = mc.player.getInventory().selectedSlot;
                    InventoryUtils.switchSlot(fireworkSlot, !strictSwitch.getValue());
                    mc.player.jump();
                    ChatUtils.sendMessage("Should have jumped?");

                    // Schedule Elytra Flight after delay
                    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                    int delayTicks = delay.getValue().intValue();
                    long delayMillis = delayTicks * 50L; // Convert ticks to milliseconds

                    scheduler.schedule(() -> {
                        mc.execute(() -> {
                            ChatUtils.sendMessage("Activating Elytra...");
                            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                            InventoryUtils.itemUsage(Hand.MAIN_HAND);
                        });
                    }, delayMillis, TimeUnit.MILLISECONDS);

                    scheduler.shutdown();
                    InventoryUtils.switchSlot(lastSlot, !strictSwitch.getValue());
                }
            }

        } else if(elytraSlot != 38 && elytraSlot != -1) {
            moveItem(elytraSlot < 9 ? elytraSlot + 36 : elytraSlot, 6);
        } else if(elytraSlot == -1 && armorSlot != -1 && armorSlot != 38) {
            moveItem(armorSlot < 9 ? armorSlot + 36 : armorSlot, 6);
        }
        disable(true);
    }

    int getLevel(Item item) {
        if(item.equals(Items.LEATHER_CHESTPLATE)) return 1;
        else if(item.equals(Items.CHAINMAIL_CHESTPLATE)) return 2;
        else if(item.equals(Items.GOLDEN_CHESTPLATE)) return 3;
        else if(item.equals(Items.IRON_CHESTPLATE)) return 4;
        else if(item.equals(Items.DIAMOND_CHESTPLATE)) return 5;
        else if(item.equals(Items.NETHERITE_CHESTPLATE)) return 6;
        return -1;
    }

    void moveItem(int slot, int newSlot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.tick();
    }
}
