/*
 * Copyright (c) 2019 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.connector.network.translators.inventory;

import com.nukkitx.protocol.bedrock.data.ContainerType;
import com.nukkitx.protocol.bedrock.data.InventoryAction;
import com.nukkitx.protocol.bedrock.packet.ContainerSetDataPacket;
import org.geysermc.connector.inventory.Inventory;
import org.geysermc.connector.network.session.GeyserSession;

public class BrewingStandInventoryTranslator extends BlockInventoryTranslator {
    public BrewingStandInventoryTranslator() {
        super(5, 117 << 4, ContainerType.BREWING_STAND);
    }

    @Override
    public void openInventory(GeyserSession session, Inventory inventory) {
        super.openInventory(session, inventory);
        ContainerSetDataPacket dataPacket = new ContainerSetDataPacket();
        dataPacket.setWindowId((byte) inventory.getId());
        dataPacket.setProperty(ContainerSetDataPacket.BREWING_STAND_FUEL_TOTAL);
        dataPacket.setValue(20);
        session.getUpstream().sendPacket(dataPacket);
    }

    @Override
    public void updateProperty(GeyserSession session, Inventory inventory, int key, int value) {
        ContainerSetDataPacket dataPacket = new ContainerSetDataPacket();
        dataPacket.setWindowId((byte) inventory.getId());
        switch (key) {
            case 0:
                dataPacket.setProperty(ContainerSetDataPacket.BREWING_STAND_BREW_TIME);
                break;
            case 1:
                dataPacket.setProperty(ContainerSetDataPacket.BREWING_STAND_FUEL_AMOUNT);
                break;
            default:
                return;
        }
        dataPacket.setValue(value);
        session.getUpstream().sendPacket(dataPacket);
    }

    @Override
    public int bedrockSlotToJava(InventoryAction action) {
        int slotnum = super.bedrockSlotToJava(action);
        switch (slotnum) {
            case 0:
                return 3;
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            default:
                return slotnum;
        }
    }

    @Override
    public int javaSlotToBedrock(int slotnum) {
        switch (slotnum) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 0;
            default:
                return slotnum;
        }
    }
}
