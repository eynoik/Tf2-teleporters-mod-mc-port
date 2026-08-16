package com.eynoik.tf2teleporter;

import com.eynoik.tf2teleporter.registry.ModBlockEntities;
import com.eynoik.tf2teleporter.registry.ModBlocks;
import com.eynoik.tf2teleporter.registry.ModMenus;
import com.eynoik.tf2teleporter.registry.ModSounds;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(TF2TeleporterMod.MOD_ID)
public final class TF2TeleporterMod {
    public static final String MOD_ID = "tf2teleporter";

    public TF2TeleporterMod(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        modBus.addListener(this::addCreativeTabItems);
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.RED_TELEPORTER_ITEM);
            event.accept(ModBlocks.BLUE_TELEPORTER_ITEM);
        }
    }
}
