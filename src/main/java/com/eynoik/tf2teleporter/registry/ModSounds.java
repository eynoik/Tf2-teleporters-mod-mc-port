package com.eynoik.tf2teleporter.registry;

import com.eynoik.tf2teleporter.TF2TeleporterMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TF2TeleporterMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SPIN = SOUNDS.register(
            "spin",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TF2TeleporterMod.MOD_ID, "spin"))
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> TELEPORT = SOUNDS.register(
            "teleport",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TF2TeleporterMod.MOD_ID, "teleport"))
    );

    private ModSounds() {
    }
}
