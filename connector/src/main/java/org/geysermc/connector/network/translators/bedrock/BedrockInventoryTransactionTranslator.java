package org.geysermc.connector.network.translators.bedrock;

import com.flowpowered.math.vector.Vector3f;
import com.github.steveice10.mc.protocol.data.game.entity.player.PlayerAction;
import com.github.steveice10.mc.protocol.data.game.window.ClickItemParam;
import com.github.steveice10.mc.protocol.data.game.window.WindowAction;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockFace;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket;
import com.nukkitx.protocol.bedrock.data.ContainerId;
import com.nukkitx.protocol.bedrock.data.InventorySource;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;
import org.geysermc.connector.inventory.Inventory;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.session.cache.InventoryCache;
import org.geysermc.connector.network.translators.PacketTranslator;
import org.geysermc.connector.network.translators.TranslatorsInit;
import org.geysermc.connector.utils.LocationUtils;

public class BedrockInventoryTransactionTranslator extends PacketTranslator<InventoryTransactionPacket> {

    @Override
    public void translate(InventoryTransactionPacket packet, GeyserSession session) {
        InventoryCache inventoryCache = session.getInventoryCache();
        Inventory openInventory = session.getInventoryCache().getOpenInventory();

        int id = 0;
        if (openInventory != null && openInventory.isOpen()) {
            id = openInventory.getId();
        }

        Inventory cachedInventory = inventoryCache.getInventories().get(id);

        // TODO: Implement support for creative inventories
        switch (packet.getTransactionType()) {
            case NORMAL:
                if (packet.getActions().size() <= 2) {
                    InventorySource source = packet.getActions().get(0).getSource();
                    ContainerId containerId = packet.getActions().get(1).getSource().getContainerId();
                    if (source.getFlag() == InventorySource.Flag.DROP_ITEM && containerId == ContainerId.INVENTORY) {
                        Vector3f loc = packet.getPlayerPosition();
                        if (loc == null)
                            loc = new Vector3f(0, 0, 0);

                        ClientPlayerActionPacket playerActionPacket = new ClientPlayerActionPacket(PlayerAction.DROP_ITEM, LocationUtils.toJavaLocation(loc), BlockFace.DOWN);
                        session.getDownstream().getSession().send(playerActionPacket);
                        return;
                    }
                }

                if (packet.getActions().size() == 2) {
                    InventorySource source = packet.getActions().get(0).getSource();
                    ContainerId containerId = packet.getActions().get(1).getSource().getContainerId();

                    if (source.getType() == InventorySource.Type.CONTAINER && (containerId == ContainerId.CURSOR || containerId == ContainerId.INVENTORY)) {
                        int slot = packet.getActions().get(0).getSlot();
                        System.out.println("slot is " + slot);
                        if (id == 0) {
                            if (source.getContainerId() == ContainerId.INVENTORY) {
                                if (slot < 9)
                                    slot += cachedInventory.getSize();
                            }

                            if (source.getContainerId() == ContainerId.CRAFTING_ADD_INGREDIENT || source.getContainerId() == ContainerId.CRAFTING_REMOVE_INGREDIENT)
                                slot += 1;

                            if (source.getContainerId() == ContainerId.ARMOR)
                                slot += 5;

                        } else if (source.getContainerId() == ContainerId.INVENTORY) {
                            if (slot < 9) {
                                slot += cachedInventory.getSize() + 27;
                            } else {
                                slot -= 9;
                            }
                        }

                        System.out.println("slot is now " + slot);
                        int transactionId = inventoryCache.getTransactionId();
                        inventoryCache.setTransactionId(transactionId + 1);
                        System.out.println(transactionId + " transaction id");
                        System.out.println("inventory id " + id);

                        ClientWindowActionPacket windowActionPacket = new ClientWindowActionPacket(id, transactionId, slot + 1,
                                TranslatorsInit.getItemTranslator().translateToJava(packet.getActions().get(0).getFromItem()), WindowAction.CLICK_ITEM, ClickItemParam.LEFT_CLICK);

                        session.getDownstream().getSession().send(windowActionPacket);
                    }
                }
                break;
            case ITEM_USE:
                // TODO: Add block place support here?
                break;
            case ITEM_USE_ON_ENTITY:
                break;
            case ITEM_RELEASE:
                break;
            case INVENTORY_MISMATCH:
                break;
        }
    }
}