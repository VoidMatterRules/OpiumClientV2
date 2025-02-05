package we.devs.opium.client.modules.player;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.api.utilities.InventoryUtils;
import we.devs.opium.client.values.impl.ValueBoolean;
import we.devs.opium.client.values.impl.ValueNumber;

import java.util.Random;

@RegisterModule(name = "Elytra Swap", tag = "Elytra Swap", description = "Automatically swap elytra and chestplate", category = Module.Category.PLAYER)
public class ModuleElytraSwap extends Module {

    private final ValueBoolean autoFirework = new ValueBoolean("AutoFirework", "Auto Firework", "Automatically uses a Firework when switching to an Elytra", true);
    private final ValueBoolean useInvFirework = new ValueBoolean("InventoryFireWork", "Inventory Firework", "Automatically uses a Firework from your inv and uses it over offhand", true);
    private final ValueBoolean strictSwitch = new ValueBoolean("StrictSwitch", "Strict Switch", "Switches to fireworks like Vanilla", false);
    private final ValueNumber delay = new ValueNumber("CustomDelay", "Custom Delay", "Delay between the first jump and the second one to activate the Elytra.", 4, 1, 20);

    private final Random random = new Random();
    private int ticksSinceEnabled = 0;
    private boolean hasSwapped = false;

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable(false);
            return;
        }

        ticksSinceEnabled = 0;
        hasSwapped = false;
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            disable(false);
            return;
        }

        ticksSinceEnabled++;

        // Find the best chestplate and elytra slots
        int bestChestplateSlot = findBestChestplateSlot();
        int elytraSlot = InventoryUtils.findItem(Items.ELYTRA, 0, 39);

        // Swap logic
        if (!hasSwapped) {
            if (elytraSlot == 38 && bestChestplateSlot != -1) {
                // Swap chestplate to armor slot
                InventoryUtils.swapSlots(bestChestplateSlot < 9 ? bestChestplateSlot + 36 : bestChestplateSlot, 6);
                hasSwapped = true;
            } else if (bestChestplateSlot == 38 && elytraSlot != -1) {
                // Swap elytra to armor slot
                InventoryUtils.swapSlots(elytraSlot < 9 ? elytraSlot + 36 : elytraSlot, 6);
                hasSwapped = true;

                // Handle auto firework logic
                if (autoFirework.getValue()) {
                    int fireworkSlot = InventoryUtils.findItem(Items.FIREWORK_ROCKET, 0, 36);
                    if (fireworkSlot != -1) {
                        if (!useInvFirework.getValue()) {
                            InventoryUtils.switchSlot(fireworkSlot, !strictSwitch.getValue());
                        }

                        // Schedule Elytra flight after a delay
                        int delayTicks = delay.getValue().intValue() + random.nextInt(3); // Add randomness to delay
                        ticksSinceEnabled = -delayTicks; // Reset counter for delay
                    }
                }
            } else if (elytraSlot != 38 && elytraSlot != -1) {
                // Swap elytra to armor slot
                InventoryUtils.swapSlots(elytraSlot < 9 ? elytraSlot + 36 : elytraSlot, 6);
                hasSwapped = true;
            } else if (elytraSlot == -1 && bestChestplateSlot != -1 && bestChestplateSlot != 38) {
                // Swap chestplate to armor slot
                InventoryUtils.swapSlots(bestChestplateSlot < 9 ? bestChestplateSlot + 36 : bestChestplateSlot, 6);
                hasSwapped = true;
            }
        }

        // Handle auto firework after delay
        if (hasSwapped && autoFirework.getValue() && ticksSinceEnabled >= 0) {
            assert mc.player != null;
            if (mc.player.isOnGround()) mc.player.jump();
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

            if (!useInvFirework.getValue()) {
                InventoryUtils.itemUsage(Hand.MAIN_HAND);
            } else {
                InventoryUtils.useItemInOffhand(Items.FIREWORK_ROCKET);
            }

            disable(true); // Disable the module after completing the task
        }
    }

    private int findBestChestplateSlot() {
        int bestSlot = -1;
        int bestLevel = -1;

        for (int i = 0; i <= 39; i++) {
            assert mc.player != null;
            Item item = mc.player.getInventory().getStack(i).getItem();
            int itemLevel = getChestplateLevel(item);
            if (itemLevel > bestLevel) {
                bestSlot = i;
                bestLevel = itemLevel;
            }
        }

        return bestSlot;
    }

    private int getChestplateLevel(Item item) {
        if (item.equals(Items.LEATHER_CHESTPLATE)) return 1;
        else if (item.equals(Items.CHAINMAIL_CHESTPLATE)) return 2;
        else if (item.equals(Items.GOLDEN_CHESTPLATE)) return 3;
        else if (item.equals(Items.IRON_CHESTPLATE)) return 4;
        else if (item.equals(Items.DIAMOND_CHESTPLATE)) return 5;
        else if (item.equals(Items.NETHERITE_CHESTPLATE)) return 6;
        return -1;
    }
}