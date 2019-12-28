package org.geysermc.connector.utils;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockState;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import org.geysermc.connector.console.GeyserLogger;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.packet.UpdateBlockPacket;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.TranslatorsInit;
import org.geysermc.connector.network.translators.block.BlockEntry;
import org.geysermc.connector.network.translators.block.entity.BlockEntityTranslator;
import org.geysermc.connector.network.translators.block.entity.SignBlockEntityTranslator;
import org.geysermc.connector.world.chunk.ChunkSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChunkUtils {

    public static ChunkData translateToBedrock(Column column) {
        ChunkData chunkData = new ChunkData();

        Chunk[] chunks = column.getChunks();
        int chunkSectionCount = chunks.length;
        chunkData.sections = new ChunkSection[chunkSectionCount];

        List<CompoundTag> blockEntities = new ArrayList<>(Arrays.asList(column.getTileEntities()));
        for (int chunkY = 0; chunkY < chunkSectionCount; chunkY++) {
            chunkData.sections[chunkY] = new ChunkSection();
            Chunk chunk = chunks[chunkY];

            if (chunk == null || chunk.isEmpty())
                continue;

            ChunkSection section = chunkData.sections[chunkY];

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState blockState = chunk.get(x, y, z);
                        BlockEntry block = TranslatorsInit.getBlockTranslator().getBedrockBlock(blockState);

                        section.getBlockStorageArray()[0].setFullBlock(ChunkSection.blockPosition(x, y, z),
                                block.getBedrockId() << 4 | block.getBedrockData());

                        if (block.getJavaIdentifier().contains("waterlogged=true")) {
                            section.getBlockStorageArray()[1].setFullBlock(ChunkSection.blockPosition(x, y, z),
                                    9 << 4); // water id
                        }
                    }
                }
            }
        }

        List<com.nukkitx.nbt.tag.CompoundTag> bedrockBlockEntities = new ArrayList<>();
        for (CompoundTag tag : blockEntities) {
            Tag idTag = tag.get("id");
            if (idTag == null && !tag.contains("Sign")) {
                GeyserLogger.DEFAULT.debug("Got tag with no id: " + tag.getValue());
                continue;
            }

            String id = idTag == null ? "Sign" : BlockEntityUtils.getBedrockBlockEntityId((String) idTag.getValue());
            BlockEntityTranslator blockEntityTranslator = BlockEntityUtils.getBlockEntityTranslator(id);
            bedrockBlockEntities.add(blockEntityTranslator.getBlockEntityTag(tag, id));
        }

        chunkData.blockEntities = bedrockBlockEntities;
        return chunkData;
    }

    public static final class ChunkData {
        public ChunkSection[] sections;

        public byte[] biomes = new byte[256];
        public List<com.nukkitx.nbt.tag.CompoundTag> blockEntities = new ArrayList<>();
    }
}
