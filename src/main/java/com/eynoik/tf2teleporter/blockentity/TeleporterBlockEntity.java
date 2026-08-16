package com.eynoik.tf2teleporter.blockentity;

import com.eynoik.tf2teleporter.data.TeleporterSavedData;
import com.eynoik.tf2teleporter.registry.ModBlockEntities;
import com.eynoik.tf2teleporter.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TeleporterBlockEntity extends BlockEntity {
    private static final int CHARGE_TICKS = 33;
    private static final int CONTACT_GRACE_TICKS = 10;
    private static final int DESTINATION_COOLDOWN_TICKS = 20;
    private static final Map<UUID, Long> TELEPORT_COOLDOWN_UNTIL = new HashMap<>();

    private int frequency = -1;
    private boolean active;

    private final Map<UUID, Integer> charge = new HashMap<>();
    private final Map<UUID, Long> lastContact = new HashMap<>();

    // Legacy-style client animation state. The old renderer multiplied this yaw by 20 degrees.
    private double clientYaw;
    private double previousClientYaw;
    private int clientSpinSoundTicks = 42;

    public TeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TELEPORTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TeleporterBlockEntity teleporter) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        teleporter.refreshActive(serverLevel);

        long gameTime = serverLevel.getGameTime();
        Iterator<Map.Entry<UUID, Long>> iterator = teleporter.lastContact.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (gameTime - entry.getValue() > CONTACT_GRACE_TICKS) {
                teleporter.charge.remove(entry.getKey());
                iterator.remove();
            }
        }

        if ((gameTime & 255L) == 0L) {
            TELEPORT_COOLDOWN_UNTIL.entrySet().removeIf(entry -> entry.getValue() < gameTime - 40L);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TeleporterBlockEntity teleporter) {
        teleporter.previousClientYaw = teleporter.clientYaw;
        if (!teleporter.active) {
            return;
        }

        // Original 1.7.10 code randomized delay from 200..799 every tick and then advanced
        // yaw by 1000/(delay+200); the renderer multiplied yaw by 20 degrees.
        int delay = 200 + level.random.nextInt(600);
        teleporter.clientYaw += 1000.0D / (delay + 200.0D);

        if (teleporter.clientSpinSoundTicks > 42) {
            level.playLocalSound(
                    pos.getX() + 0.5D,
                    pos.getY(),
                    pos.getZ() + 0.5D,
                    ModSounds.SPIN.get(),
                    SoundSource.MASTER,
                    0.08F,
                    1.0F,
                    false
            );
            teleporter.clientSpinSoundTicks = 0;
        } else {
            teleporter.clientSpinSoundTicks++;
        }
    }

    public void handleEntityInside(Entity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity)) {
            return;
        }
        if (!active || !TeleporterSavedData.isValidFrequency(frequency)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        UUID id = entity.getUUID();
        lastContact.put(id, gameTime);

        if (TELEPORT_COOLDOWN_UNTIL.getOrDefault(id, Long.MIN_VALUE) > gameTime) {
            charge.remove(id);
            return;
        }

        int ticks = charge.merge(id, 1, Integer::sum);
        if (ticks < CHARGE_TICKS) {
            return;
        }

        charge.remove(id);
        teleport(serverLevel, entity);
    }

    private void teleport(ServerLevel level, Entity entity) {
        TeleporterSavedData data = TeleporterSavedData.get(level);
        Optional<BlockPos> counterpart = data.counterpart(level, frequency, worldPosition);
        if (counterpart.isEmpty()) {
            setActive(false);
            return;
        }

        BlockPos destination = counterpart.get();
        // Load the destination only when somebody actually uses the teleporter.
        level.getChunkAt(destination);
        if (!(level.getBlockEntity(destination) instanceof TeleporterBlockEntity destinationTeleporter)
                || destinationTeleporter.frequency != frequency) {
            data.remove(frequency, destination);
            refreshActive(level);
            return;
        }

        double x = destination.getX() + 0.5D;
        double y = destination.getY() + 0.3D;
        double z = destination.getZ() + 0.5D;

        playTeleportEffects(level, worldPosition);
        playTeleportEffects(level, destination);

        entity.teleportTo(x, y, z);
        entity.fallDistance = 0.0F;
        TELEPORT_COOLDOWN_UNTIL.put(entity.getUUID(), level.getGameTime() + DESTINATION_COOLDOWN_TICKS);

        destinationTeleporter.charge.remove(entity.getUUID());
        destinationTeleporter.lastContact.put(entity.getUUID(), level.getGameTime());
    }

    private static void playTeleportEffects(ServerLevel level, BlockPos pos) {
        level.playSound(
                null,
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                ModSounds.TELEPORT.get(),
                SoundSource.MASTER,
                0.30F,
                1.0F
        );
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                pos.getX() + 0.5D,
                pos.getY() + 1.5D,
                pos.getZ() + 0.5D,
                10,
                1.0D,
                1.0D,
                1.0D,
                0.0D
        );
    }

    public boolean applyFrequency(ServerLevel level, int newFrequency) {
        if (!TeleporterSavedData.isValidFrequency(newFrequency)) {
            return false;
        }

        TeleporterSavedData data = TeleporterSavedData.get(level);
        Optional<BlockPos> oldCounterpart = data.counterpart(level, frequency, worldPosition);
        if (!data.assign(level, frequency, newFrequency, worldPosition)) {
            return false;
        }

        frequency = newFrequency;
        setChanged();
        syncToClient();

        oldCounterpart.ifPresent(pos -> refreshLoadedTeleporter(level, pos));
        data.counterpart(level, frequency, worldPosition).ifPresent(pos -> refreshLoadedTeleporter(level, pos));
        refreshActive(level);
        return true;
    }

    public boolean canUseFrequency(ServerLevel level, int candidate) {
        return TeleporterSavedData.get(level).canAssign(level, candidate, worldPosition);
    }

    public void unregister(ServerLevel level) {
        if (!TeleporterSavedData.isValidFrequency(frequency)) {
            return;
        }
        TeleporterSavedData data = TeleporterSavedData.get(level);
        Optional<BlockPos> oldCounterpart = data.counterpart(level, frequency, worldPosition);
        data.remove(frequency, worldPosition);
        oldCounterpart.ifPresent(pos -> refreshLoadedTeleporter(level, pos));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel && TeleporterSavedData.isValidFrequency(frequency)) {
            TeleporterSavedData data = TeleporterSavedData.get(serverLevel);
            data.assign(serverLevel, frequency, frequency, worldPosition);
            refreshActive(serverLevel);
        }
    }

    private void refreshActive(ServerLevel level) {
        boolean shouldBeActive = TeleporterSavedData.isValidFrequency(frequency)
                && TeleporterSavedData.get(level).isPaired(level, frequency, worldPosition);
        setActive(shouldBeActive);
    }

    private void setActive(boolean newActive) {
        if (active == newActive) {
            return;
        }
        active = newActive;
        setChanged();
        syncToClient();
    }

    private static void refreshLoadedTeleporter(ServerLevel level, BlockPos pos) {
        if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter) {
            teleporter.refreshActive(level);
        }
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean isActive() {
        return active;
    }

    public float getRenderRotation(float partialTick) {
        return (float) ((previousClientYaw + (clientYaw - previousClientYaw) * partialTick) * 20.0D);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        frequency = tag.getInt("Frequency");
        if (!tag.contains("Frequency")) {
            frequency = -1;
        }
        active = tag.getBoolean("Active");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Frequency", frequency);
        tag.putBoolean("Active", active);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
