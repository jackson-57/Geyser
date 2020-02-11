/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
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

package org.geysermc.connector.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nukkitx.nbt.NbtUtils;
import com.nukkitx.nbt.stream.NBTInputStream;
import com.nukkitx.nbt.tag.CompoundTag;
import com.nukkitx.nbt.tag.ListTag;
import com.nukkitx.protocol.bedrock.data.ItemData;
import com.nukkitx.nbt.tag.Tag;
import com.nukkitx.protocol.bedrock.packet.BiomeDefinitionListPacket;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.console.GeyserLogger;
import org.geysermc.connector.network.translators.item.ItemEntry;

import java.io.*;
import java.util.*;

public class Toolbox {

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
    public static final CompoundTag BIOMES;

    public static final Collection<StartGamePacket.ItemEntry> ITEMS = new ArrayList<>();

    public static final Int2ObjectMap<ItemEntry> ITEM_ENTRIES = new Int2ObjectOpenHashMap<>();

    static {
        /* Load biomes */
        InputStream biomestream = GeyserConnector.class.getClassLoader().getResourceAsStream("bedrock/biome_definitions.dat");
        if (biomestream == null) {
            throw new AssertionError("Unable to find bedrock/biome_definitions.dat");
        }

        CompoundTag biomesTag;

        try (NBTInputStream biomenbtInputStream = NbtUtils.createNetworkReader(biomestream)){
            biomesTag = (CompoundTag) biomenbtInputStream.readTag();
            BIOMES = biomesTag;
        } catch (Exception ex) {
             GeyserConnector.getInstance().getLogger().warning("Failed to get biomes from biome definitions, is there something wrong with the file?");
            throw new AssertionError(ex);
        }

        /* Load item palette */
        InputStream stream = getResource("bedrock/items.json");

        TypeReference<List<JsonNode>> itemEntriesType = new TypeReference<List<JsonNode>>() {
        };

        List<JsonNode> itemEntries;
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

    public static void init() {
        // no-op
    }
}