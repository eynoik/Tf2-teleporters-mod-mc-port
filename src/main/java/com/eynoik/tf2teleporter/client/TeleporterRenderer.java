package com.eynoik.tf2teleporter.client;

import com.eynoik.tf2teleporter.TF2TeleporterMod;
import com.eynoik.tf2teleporter.block.TeleporterBlock;
import com.eynoik.tf2teleporter.blockentity.TeleporterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class TeleporterRenderer implements BlockEntityRenderer<TeleporterBlockEntity> {
    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(TF2TeleporterMod.MOD_ID, "textures/model/tf2_teleporter_base.png");
    private static final ResourceLocation RED_PROPELLER_TEXTURE = ResourceLocation.fromNamespaceAndPath(TF2TeleporterMod.MOD_ID, "textures/model/tf2_teleporter_propeller_red.png");
    private static final ResourceLocation BLUE_PROPELLER_TEXTURE = ResourceLocation.fromNamespaceAndPath(TF2TeleporterMod.MOD_ID, "textures/model/tf2_teleporter_propeller_blue.png");
    private final ModelPart base;
    private final ModelPart propeller;

    public TeleporterRenderer(BlockEntityRendererProvider.Context context) {
        base = context.bakeLayer(TeleporterModelLayers.BASE);
        propeller = context.bakeLayer(TeleporterModelLayers.PROPELLER);
    }

    public static LayerDefinition createBaseLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        addBox(root,"base0",0,0,false,-12,21,-3,24,1,6);
        addBox(root,"base1",0,10,false,8,22,-10,2,1,20);
        addBox(root,"base2",0,10,false,-10,22,-10,2,1,20);
        addBox(root,"shape1",0,10,false,8,23,-11,2,1,4);
        addBox(root,"shape15",0,20,false,-11,23,8,4,1,2);
        addBox(root,"shape16",0,20,false,-11,23,-10,4,1,2);
        addBox(root,"shape17",0,20,false,7,23,-10,4,1,2);
        addBox(root,"shape18",0,20,false,7,23,8,4,1,2);
        addBox(root,"shape19",0,10,false,8,23,7,2,1,4);
        addBox(root,"shape110",0,10,false,-10,23,7,2,1,4);
        addBox(root,"shape111",0,10,false,-10,23,-11,2,1,4);
        return LayerDefinition.create(mesh,64,32);
    }

    public static LayerDefinition createPropellerLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        addBox(root,"shape1",26,23,false,-13,20,-2,13,1,4);
        addBox(root,"shape2",15,23,false,8,16,-1,4,1,2);
        addBox(root,"shape3",15,23,false,-12,16,-1,4,1,2);
        addBox(root,"shape4",0,23,false,-11,16,-2,2,1,4);
        addBox(root,"shape5",0,23,false,9,16,-2,2,1,4);
        addBox(root,"shape6",3,14,false,-14,19,-3,14,1,6);
        addBox(root,"shape7",0,0,false,-14,17,-4,14,2,8);
        addBox(root,"shape8",0,0,true,0,17,-4,14,2,8);
        addBox(root,"shape9",3,14,true,0,19,-3,14,1,6);
        addBox(root,"shape10",26,23,true,0,20,-2,13,1,4);
        return LayerDefinition.create(mesh,64,32);
    }

    private static void addBox(PartDefinition root,String name,int u,int v,boolean mirror,float x,float y,float z,float width,float height,float depth) {
        CubeListBuilder builder = CubeListBuilder.create().texOffs(u,v);
        if (mirror) builder = builder.mirror();
        root.addOrReplaceChild(name,builder.addBox(0.0F,0.0F,0.0F,width,height,depth),PartPose.offset(x,y,z));
    }

    @Override
    public void render(TeleporterBlockEntity teleporter,float partialTick,PoseStack poseStack,MultiBufferSource bufferSource,int packedLight,int packedOverlay) {
        renderPart(base,BASE_TEXTURE,poseStack,bufferSource,packedLight,packedOverlay,0.0F);
        ResourceLocation texture = RED_PROPELLER_TEXTURE;
        if (teleporter.getBlockState().getBlock() instanceof TeleporterBlock block && block.getTeamColor() == TeleporterBlock.TeamColor.BLUE) texture = BLUE_PROPELLER_TEXTURE;
        renderPart(propeller,texture,poseStack,bufferSource,packedLight,packedOverlay,teleporter.isActive()?teleporter.getRenderRotation(partialTick):0.0F);
    }

    private static void renderPart(ModelPart model,ResourceLocation texture,PoseStack poseStack,MultiBufferSource bufferSource,int packedLight,int packedOverlay,float yRotation) {
        poseStack.pushPose();
        poseStack.translate(0.5D,0.75D,0.5D);
        poseStack.scale(0.5F,0.5F,0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        if (yRotation != 0.0F) poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.render(poseStack,consumer,packedLight,packedOverlay);
        poseStack.popPose();
    }
}
