package com.eynoik.tf2teleporter.client;

import com.eynoik.tf2teleporter.TF2TeleporterMod;
import com.eynoik.tf2teleporter.registry.ModBlockEntities;
import com.eynoik.tf2teleporter.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TF2TeleporterMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TeleporterModelLayers.BASE, TeleporterRenderer::createBaseLayer);
        event.registerLayerDefinition(TeleporterModelLayers.PROPELLER, TeleporterRenderer::createPropellerLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TELEPORTER.get(), TeleporterRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TELEPORTER.get(), TeleporterScreen::new);
    }

    private ClientModEvents() {}
}
