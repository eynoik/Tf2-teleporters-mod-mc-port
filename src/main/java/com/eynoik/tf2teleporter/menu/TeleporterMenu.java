package com.eynoik.tf2teleporter.menu;

import com.eynoik.tf2teleporter.blockentity.TeleporterBlockEntity;
import com.eynoik.tf2teleporter.data.TeleporterSavedData;
import com.eynoik.tf2teleporter.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

public final class TeleporterMenu extends AbstractContainerMenu {
    public static final int BUTTON_PLUS_ONE = 0;
    public static final int BUTTON_MINUS_ONE = 1;
    public static final int BUTTON_PLUS_TEN = 2;
    public static final int BUTTON_MINUS_TEN = 3;

    private final BlockPos teleporterPos;
    private final DataSlot selectedFrequency = DataSlot.standalone();
    private final DataSlot frequencyAvailable = DataSlot.standalone();

    public TeleporterMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readBlockPos(), true);
    }

    public TeleporterMenu(int containerId, Inventory playerInventory, BlockPos teleporterPos) {
        this(containerId, playerInventory, teleporterPos, false);
    }

    private TeleporterMenu(int containerId, Inventory playerInventory, BlockPos teleporterPos, boolean clientSide) {
        super(ModMenus.TELEPORTER.get(), containerId);
        this.teleporterPos = teleporterPos.immutable();
        int initial = TeleporterSavedData.MIN_FREQUENCY;
        int available = 1;
        if (!clientSide && playerInventory.player.level() instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(teleporterPos) instanceof TeleporterBlockEntity teleporter) {
            if (TeleporterSavedData.isValidFrequency(teleporter.getFrequency())) initial = teleporter.getFrequency();
            available = teleporter.canUseFrequency(serverLevel, initial) ? 1 : 0;
        }
        selectedFrequency.set(initial);
        frequencyAvailable.set(available);
        addDataSlot(selectedFrequency);
        addDataSlot(frequencyAvailable);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player.level() instanceof ServerLevel serverLevel)
                || !(serverLevel.getBlockEntity(teleporterPos) instanceof TeleporterBlockEntity teleporter)) return false;
        int delta = switch (id) {
            case BUTTON_PLUS_ONE -> 1;
            case BUTTON_MINUS_ONE -> -1;
            case BUTTON_PLUS_TEN -> 10;
            case BUTTON_MINUS_TEN -> -10;
            default -> 0;
        };
        if (delta == 0) return false;
        selectedFrequency.set(wrapFrequency(selectedFrequency.get() + delta));
        frequencyAvailable.set(teleporter.canUseFrequency(serverLevel, selectedFrequency.get()) ? 1 : 0);
        broadcastChanges();
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!(player.level() instanceof ServerLevel serverLevel)
                || !(serverLevel.getBlockEntity(teleporterPos) instanceof TeleporterBlockEntity teleporter)) return;
        int selected = selectedFrequency.get();
        if (teleporter.canUseFrequency(serverLevel, selected)) teleporter.applyFrequency(serverLevel, selected);
    }

    @Override
    public boolean stillValid(Player player) {
        if (!(player.level().getBlockEntity(teleporterPos) instanceof TeleporterBlockEntity)) return false;
        return player.distanceToSqr(teleporterPos.getX() + 0.5D, teleporterPos.getY() + 0.5D, teleporterPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    public int getSelectedFrequency() { return selectedFrequency.get(); }
    public boolean isFrequencyAvailable() { return frequencyAvailable.get() != 0; }
    public BlockPos getTeleporterPos() { return teleporterPos; }

    private static int wrapFrequency(int frequency) {
        int range = TeleporterSavedData.MAX_FREQUENCY - TeleporterSavedData.MIN_FREQUENCY + 1;
        return Math.floorMod(frequency - TeleporterSavedData.MIN_FREQUENCY, range) + TeleporterSavedData.MIN_FREQUENCY;
    }
}
