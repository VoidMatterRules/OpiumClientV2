package we.devs.opium.client.modules.player;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
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

    private final ValueBoolean autoFirework = new ValueBoolean("AutoFirework", "Auto Firework", "Automatically uses a Firework when switching to an Elytra", true);
    private final ValueBoolean useInvFirework = new ValueBoolean("InventoryFireWork", "Inventory Firework", "Automatically uses a Firework from your inv and uses it over offhand", true);
    private final ValueBoolean strictSwitch = new ValueBoolean("StrictSwitch", "Strict Switch", "Switches to fireworks like Vanilla", false);
    private final ValueNumber delay = new ValueNumber("CustomDelay", "Custom Delay", "Delay between the first jump and the second one to activate the Elytra.", 4, 1, 20);

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable(false);
            return;
        }

        int fireworkSlot;
        if (useInvFirework.getValue()) {
            fireworkSlot = InventoryUtils.findItem(Items.FIREWORK_ROCKET, 0, 36);
        } else fireworkSlot = InventoryUtils.findItem(Items.FIREWORK_ROCKET, 0, 8);

        if (fireworkSlot == -1) {
            ChatUtils.sendMessage("No Fireworks Found", "Elytra Swap");
            disable(true);
            return;
        }

        // Find the best chestplate and elytra slots
        int bestChestplateSlot = findBestChestplateSlot();
        int elytraSlot = InventoryUtils.findItem(Items.ELYTRA, 0, 39);

        // Swap logic
        if (elytraSlot == 38 && bestChestplateSlot != -1) {
            // Swap chestplate to armor slot
            InventoryUtils.swapSlots(bestChestplateSlot < 9 ? bestChestplateSlot + 36 : bestChestplateSlot, 6);
        } else if (bestChestplateSlot == 38 && elytraSlot != -1) {
            // Swap elytra to armor slot
            InventoryUtils.swapSlots(elytraSlot < 9 ? elytraSlot + 36 : elytraSlot, 6);

            // Handle auto firework logic
            if (autoFirework.getValue()) {

                // Schedule Elytra flight after delay
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                int delayTicks = delay.getValue().intValue();
                long delayMillis = delayTicks * 50L; // Convert ticks to milliseconds

                assert mc.player != null;
                if (mc.player.isOnGround()) mc.player.jump();
                scheduler.schedule(() -> mc.execute(() -> {
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

                    if (!useInvFirework.getValue()) {
                        // Use firework from hotbar
                        InventoryUtils.switchToSlot(fireworkSlot, true, () -> {
                            InventoryUtils.itemUsage(Hand.MAIN_HAND);
                        });
                    } else {
                        // Use firework from inventory
                        InventoryUtils.useItemInOffhand(Items.FIREWORK_ROCKET);
                    }
                }), delayMillis, TimeUnit.MILLISECONDS);

                scheduler.shutdown();
            }
        } else if (elytraSlot != 38 && elytraSlot != -1) {
            // Swap elytra to armor slot
            InventoryUtils.swapSlots(elytraSlot < 9 ? elytraSlot + 36 : elytraSlot, 6);
        } else if (elytraSlot == -1 && bestChestplateSlot != -1 && bestChestplateSlot != 38) {
            // Swap chestplate to armor slot
            InventoryUtils.swapSlots(bestChestplateSlot < 9 ? bestChestplateSlot + 36 : bestChestplateSlot, 6);
        }

        disable(true); // Disable the module after completing the task
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