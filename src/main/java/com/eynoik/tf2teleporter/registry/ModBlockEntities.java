package com.eynoik.tf2teleporter.registry;

import com.eynoik.tf2teleporter.TF2TeleporterMod;
import com.eynoik.tf2teleporter.blockentity.TeleporterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TF2TeleporterMod.MOD_ID);

    public static final Supplier<BlockEntityType<TeleporterBlockEntity>> TELEPORTER = BLOCK_ENTITIES.register(
            "teleporter",
            () -> BlockEntityType.Builder.of(
                    TeleporterBlockEntity::new,
                    ModBlocks.RED_TELEPORTER.get(),
                    ModBlocks.BLUE_TELEPORTER.get()
            ).build(null)
    );

    private ModBlockEntities() {
    }
}
