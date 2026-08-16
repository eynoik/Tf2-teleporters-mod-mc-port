package com.eynoik.tf2teleporter.registry;

import com.eynoik.tf2teleporter.TF2TeleporterMod;
import com.eynoik.tf2teleporter.block.TeleporterBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TF2TeleporterMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TF2TeleporterMod.MOD_ID);

    private static BlockBehaviour.Properties teleporterProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    public static final DeferredBlock<TeleporterBlock> RED_TELEPORTER = BLOCKS.registerBlock(
            "red_teleporter",
            properties -> new TeleporterBlock(properties, TeleporterBlock.TeamColor.RED),
            teleporterProperties()
    );

    public static final DeferredBlock<TeleporterBlock> BLUE_TELEPORTER = BLOCKS.registerBlock(
            "blue_teleporter",
            properties -> new TeleporterBlock(properties, TeleporterBlock.TeamColor.BLUE),
            teleporterProperties()
    );

    public static final DeferredItem<Item> TELEPORTER_BASE =
            ITEMS.registerSimpleItem("teleporter_base", properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> TELEPORTER_PROPELLER =
            ITEMS.registerSimpleItem("teleporter_propeller", properties -> properties.stacksTo(1));

    public static final DeferredItem<BlockItem> RED_TELEPORTER_ITEM =
            ITEMS.registerSimpleBlockItem("red_teleporter", RED_TELEPORTER, properties -> properties.stacksTo(1));
    public static final DeferredItem<BlockItem> BLUE_TELEPORTER_ITEM =
            ITEMS.registerSimpleBlockItem("blue_teleporter", BLUE_TELEPORTER, properties -> properties.stacksTo(1));

    private ModBlocks() {
    }
}
