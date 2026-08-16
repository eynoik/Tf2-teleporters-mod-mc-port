package com.eynoik.tf2teleporter.block;

import com.eynoik.tf2teleporter.blockentity.TeleporterBlockEntity;
import com.eynoik.tf2teleporter.menu.TeleporterMenu;
import com.eynoik.tf2teleporter.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class TeleporterBlock extends BaseEntityBlock {
    private static final MapCodec<TeleporterBlock> RED_CODEC = simpleCodec(properties -> new TeleporterBlock(properties, TeamColor.RED));
    private static final MapCodec<TeleporterBlock> BLUE_CODEC = simpleCodec(properties -> new TeleporterBlock(properties, TeamColor.BLUE));
    private static final VoxelShape SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 4.8D, 16.0D);
    private final TeamColor teamColor;

    public TeleporterBlock(BlockBehaviour.Properties properties, TeamColor teamColor) {
        super(properties);
        this.teamColor = teamColor;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return teamColor == TeamColor.RED ? RED_CODEC : BLUE_CODEC;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof TeleporterBlockEntity) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, menuPlayer) -> new TeleporterMenu(containerId, inventory, pos),
                            Component.literal("TF2 Teleporter")
                    ),
                    buffer -> buffer.writeBlockPos(pos)
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter) {
            teleporter.handleEntityInside(entity);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter
                && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            teleporter.unregister(serverLevel);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TeleporterBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return createTickerHelper(blockEntityType, ModBlockEntities.TELEPORTER.get(), TeleporterBlockEntity::clientTick);
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.TELEPORTER.get(), TeleporterBlockEntity::serverTick);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return false;
    }

    public enum TeamColor { RED, BLUE }
}
