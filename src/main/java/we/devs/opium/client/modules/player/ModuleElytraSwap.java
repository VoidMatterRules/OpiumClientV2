package we.devs.opium.client.modules.player;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.api.utilities.ChatUtils;
import we.devs.opium.api.utilities.InventoryUtils;
import we.devs.opium.client.events.EventPacketSend;
import we.devs.opium.client.events.EventTick;

@RegisterModule(name = "Elytra Swap", tag = "Elytra Swap", description = "Automatically swap elytra and chestplate", category = Module.Category.PLAYER)
public class ModuleElytraSwap extends Module {
    @Override
    public void onEnable() {
        if(nullCheck()) {
            disable(false);
            return;
        }

        int lvl = -1;
        int armorSlot = -1;
        for (int i = 0; i <= 39; i++) {
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
