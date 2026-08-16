package com.eynoik.tf2teleporter.client;

import com.eynoik.tf2teleporter.TF2TeleporterMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public final class TeleporterModelLayers {
    public static final ModelLayerLocation BASE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TF2TeleporterMod.MOD_ID, "teleporter_base"), "main");
    public static final ModelLayerLocation PROPELLER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TF2TeleporterMod.MOD_ID, "teleporter_propeller"), "main");
    private TeleporterModelLayers() {}
}
