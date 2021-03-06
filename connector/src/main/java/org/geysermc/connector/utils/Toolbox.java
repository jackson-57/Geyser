package org.geysermc.connector.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nukkitx.nbt.NbtUtils;
import com.nukkitx.nbt.stream.NBTInputStream;
import com.nukkitx.nbt.tag.CompoundTag;
import com.nukkitx.nbt.tag.ListTag;
import com.nukkitx.protocol.bedrock.data.ItemData;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.console.GeyserLogger;
import org.geysermc.connector.network.translators.block.BlockEntry;
import org.geysermc.connector.network.translators.item.ItemEntry;
import org.geysermc.connector.world.GlobalBlockPalette;

import java.io.*;
import java.util.*;

public class Toolbox {

    public static final Collection<StartGamePacket.ItemEntry> ITEMS = new ArrayList<>();
    public static ListTag<CompoundTag> BLOCKS;
    public static ItemData[] CREATIVE_ITEMS;

    public static final Int2ObjectMap<ItemEntry> ITEM_ENTRIES = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<BlockEntry> BLOCK_ENTRIES = new Int2ObjectOpenHashMap<>();

    public static void init() {
        InputStream stream = GeyserConnector.class.getClassLoader().getResourceAsStream("bedrock/runtime_block_states.dat");
        if (stream == null) {
            throw new AssertionError("Unable to find bedrock/runtime_block_states.dat");
        }

        Map<String, Integer> blockIdToIdentifier = new HashMap<>();
        ListTag<CompoundTag> blocksTag;

        NBTInputStream nbtInputStream = NbtUtils.createNetworkReader(stream);
        try {
            blocksTag = (ListTag<CompoundTag>) nbtInputStream.readTag();
            nbtInputStream.close();
        } catch (Exception ex) {
            GeyserLogger.DEFAULT.warning("Failed to get blocks from runtime block states, please report this error!");
            throw new AssertionError(ex);
        }

        for (CompoundTag entry : blocksTag.getValue()) {
            String name = entry.getAsCompound("block").getAsString("name");
            int id = entry.getAsShort("id");
            int data = entry.getAsShort("meta");

            blockIdToIdentifier.put(name, id);
            GlobalBlockPalette.registerMapping(id << 4 | data);
        }

        BLOCKS = blocksTag;
        InputStream stream2 = Toolbox.class.getClassLoader().getResourceAsStream("bedrock/items.json");
        if (stream2 == null) {
            throw new AssertionError("Items Table not found");
        }

        ObjectMapper startGameItemMapper = new ObjectMapper();
        List<Map> startGameItems = new ArrayList<>();
        try {
            startGameItems = startGameItemMapper.readValue(stream2, ArrayList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map entry : startGameItems) {
            ITEMS.add(new StartGamePacket.ItemEntry((String) entry.get("name"), (short) ((int) entry.get("id"))));
        }

        InputStream itemStream = Toolbox.class.getClassLoader().getResourceAsStream("mappings/items.json");
        ObjectMapper itemMapper = new ObjectMapper();
        Map<String, Map<String, Object>> items = new HashMap<>();

        try {
            items = itemMapper.readValue(itemStream, LinkedHashMap.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int itemIndex = 0;
        for (Map.Entry<String, Map<String, Object>> itemEntry : items.entrySet()) {
            ITEM_ENTRIES.put(itemIndex, new ItemEntry(itemEntry.getKey(), itemIndex, (int) itemEntry.getValue().get("bedrock_id"), (int) itemEntry.getValue().get("bedrock_data")));
            itemIndex++;
        }

        InputStream blockStream = Toolbox.class.getClassLoader().getResourceAsStream("mappings/blocks.json");
        ObjectMapper blockMapper = new ObjectMapper();
        Map<String, Map<String, Object>> blocks = new HashMap<>();

        try {
            blocks = blockMapper.readValue(blockStream, LinkedHashMap.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int blockIndex = 0;
        for (Map.Entry<String, Map<String, Object>> itemEntry : blocks.entrySet()) {
            if (!blockIdToIdentifier.containsKey(itemEntry.getValue().get("bedrock_identifier"))) {
                GeyserLogger.DEFAULT.debug("Mapping " + itemEntry.getValue().get("bedrock_identifier") + " was not found for bedrock edition!");
                BLOCK_ENTRIES.put(blockIndex, new BlockEntry(itemEntry.getKey(), blockIndex, 248, 0)); // update block
            } else {
                BLOCK_ENTRIES.put(blockIndex, new BlockEntry(itemEntry.getKey(), blockIndex, blockIdToIdentifier.get(itemEntry.getValue().get("bedrock_identifier")), (int) itemEntry.getValue().get("bedrock_data")));
            }

            blockIndex++;
        }

        InputStream creativeItemStream = Toolbox.class.getClassLoader().getResourceAsStream("bedrock/creative_items.json");
        ObjectMapper creativeItemMapper = new ObjectMapper();
        List<LinkedHashMap<String, Object>> creativeItemEntries = new ArrayList<>();

        try {
            creativeItemEntries = creativeItemMapper.readValue(creativeItemStream, ArrayList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<ItemData> creativeItems = new ArrayList<>();
        for (Map<String, Object> map : creativeItemEntries) {
            short damage = 0;
            if (map.containsKey("damage")) {
                damage = (short)(int) map.get("damage");
            }
            if (map.containsKey("nbt_b64")) {
                byte[] bytes = Base64.getDecoder().decode((String) map.get("nbt_b64"));
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                try {
                    com.nukkitx.nbt.tag.CompoundTag tag = (com.nukkitx.nbt.tag.CompoundTag) NbtUtils.createReaderLE(bais).readTag();
                    creativeItems.add(ItemData.of((int) map.get("id"), damage, 1, tag));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                creativeItems.add(ItemData.of((int) map.get("id"), damage, 1));
            }
        }

        CREATIVE_ITEMS = creativeItems.toArray(new ItemData[0]);
    }
}