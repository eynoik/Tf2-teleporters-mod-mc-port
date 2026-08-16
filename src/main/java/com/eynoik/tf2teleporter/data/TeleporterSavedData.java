package com.eynoik.tf2teleporter.data;

import com.eynoik.tf2teleporter.blockentity.TeleporterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Per-dimension teleporter registry. A frequency has at most two endpoints.
 */
public final class TeleporterSavedData extends SavedData {
    public static final int MIN_FREQUENCY = 1;
    public static final int MAX_FREQUENCY = 99;
    private static final String FILE_NAME = "tf2teleporter_frequencies";
    private static final Factory<TeleporterSavedData> FACTORY =
            new Factory<>(TeleporterSavedData::new, TeleporterSavedData::load);

    private final Map<Integer, List<BlockPos>> endpoints = new HashMap<>();

    public static TeleporterSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_NAME);
    }

    public static TeleporterSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        TeleporterSavedData data = new TeleporterSavedData();
        ListTag frequencies = tag.getList("Frequencies", Tag.TAG_COMPOUND);
        for (int i = 0; i < frequencies.size(); i++) {
            CompoundTag entry = frequencies.getCompound(i);
            int frequency = entry.getInt("Frequency");
            if (!isValidFrequency(frequency)) {
                continue;
            }

            long[] packedPositions = entry.getLongArray("Positions");
            List<BlockPos> list = data.endpoints.computeIfAbsent(frequency, ignored -> new ArrayList<>(2));
            for (long packed : packedPositions) {
                BlockPos pos = BlockPos.of(packed);
                if (!list.contains(pos) && list.size() < 2) {
                    list.add(pos);
                }
            }
            if (list.isEmpty()) {
                data.endpoints.remove(frequency);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag frequencies = new ListTag();
        endpoints.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(mapEntry -> {
                    CompoundTag entry = new CompoundTag();
                    entry.putInt("Frequency", mapEntry.getKey());
                    long[] packed = mapEntry.getValue().stream().mapToLong(BlockPos::asLong).toArray();
                    entry.putLongArray("Positions", packed);
                    frequencies.add(entry);
                });
        tag.put("Frequencies", frequencies);
        return tag;
    }

    public boolean canAssign(ServerLevel level, int frequency, BlockPos position) {
        if (!isValidFrequency(frequency)) {
            return false;
        }
        cleanupLoadedInvalid(level, frequency);
        List<BlockPos> list = endpoints.get(frequency);
        return list == null || list.contains(position) || list.size() < 2;
    }

    /**
     * Atomically moves an endpoint to a new frequency. If the new frequency is full,
     * the old assignment is left untouched.
     */
    public boolean assign(ServerLevel level, int oldFrequency, int newFrequency, BlockPos position) {
        if (!canAssign(level, newFrequency, position)) {
            return false;
        }
        if (oldFrequency == newFrequency) {
            ensurePresent(newFrequency, position);
            return true;
        }

        if (isValidFrequency(oldFrequency)) {
            removeInternal(oldFrequency, position);
        }
        ensurePresent(newFrequency, position);
        setDirty();
        return true;
    }

    public void remove(int frequency, BlockPos position) {
        if (removeInternal(frequency, position)) {
            setDirty();
        }
    }

    public Optional<BlockPos> counterpart(ServerLevel level, int frequency, BlockPos position) {
        if (!isValidFrequency(frequency)) {
            return Optional.empty();
        }
        cleanupLoadedInvalid(level, frequency);
        List<BlockPos> list = endpoints.get(frequency);
        if (list == null || list.size() != 2 || !list.contains(position)) {
            return Optional.empty();
        }
        return list.stream().filter(pos -> !pos.equals(position)).findFirst();
    }

    public boolean isPaired(ServerLevel level, int frequency, BlockPos position) {
        return counterpart(level, frequency, position).isPresent();
    }

    public int occupancy(ServerLevel level, int frequency) {
        if (!isValidFrequency(frequency)) {
            return 0;
        }
        cleanupLoadedInvalid(level, frequency);
        List<BlockPos> list = endpoints.get(frequency);
        return list == null ? 0 : list.size();
    }

    public static boolean isValidFrequency(int frequency) {
        return frequency >= MIN_FREQUENCY && frequency <= MAX_FREQUENCY;
    }

    private void ensurePresent(int frequency, BlockPos position) {
        List<BlockPos> list = endpoints.computeIfAbsent(frequency, ignored -> new ArrayList<>(2));
        if (!list.contains(position)) {
            list.add(position.immutable());
            setDirty();
        }
    }

    private boolean removeInternal(int frequency, BlockPos position) {
        List<BlockPos> list = endpoints.get(frequency);
        if (list == null) {
            return false;
        }
        boolean changed = list.remove(position);
        if (list.isEmpty()) {
            endpoints.remove(frequency);
        }
        return changed;
    }

    /**
     * Do not treat unloaded chunks as stale: an endpoint may legitimately be far away.
     * We only prune an endpoint when its chunk is loaded and the block entity is definitely gone
     * or points at a different frequency.
     */
    private void cleanupLoadedInvalid(ServerLevel level, int frequency) {
        List<BlockPos> list = endpoints.get(frequency);
        if (list == null || list.isEmpty()) {
            return;
        }

        boolean changed = list.removeIf(pos -> {
            if (!level.hasChunkAt(pos)) {
                return false;
            }
            return !(level.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter)
                    || teleporter.getFrequency() != frequency;
        });

        if (list.isEmpty()) {
            endpoints.remove(frequency);
        }
        if (changed) {
            setDirty();
        }
    }
}
