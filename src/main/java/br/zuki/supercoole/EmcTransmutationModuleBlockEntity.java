package br.zuki.supercoole;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.IItemHandler;

import de.scc.calculator.network.BulkItemStore;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

public class EmcTransmutationModuleBlockEntity extends BlockEntity implements IItemHandler, BulkItemStore {
    private static final Map<UUID, EmcTransmutationModuleBlockEntity> PRIMARY_MODULES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_ENTRIES_LOG = new ConcurrentHashMap<>();
    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final long CAPACITY_FLOOR = 2305843009213693951L;

    private UUID owner;

    private volatile java.util.List<ItemInfo> cachedKnownItems;
    private volatile long cachedKnownAt;
    private volatile java.util.List<ItemStack> cachedEntries;
    private volatile long cachedEntriesAt;
    private static final long CACHE_TTL_MS = 250;

    public EmcTransmutationModuleBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.EMC_MODULE.get(), pos, state);
    }

    public EmcTransmutationModuleBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public UUID getOwner() { return owner; }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
        if (uuid != null) {
            PRIMARY_MODULES.putIfAbsent(uuid, this);
        }
        setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (owner != null && PRIMARY_MODULES.get(owner) == this) {
            PRIMARY_MODULES.remove(owner);
        }
    }

    // ---------- NBT ----------
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("Owner")) {
            owner = tag.getUUID("Owner");
        } else if (tag.contains("Owner", Tag.TAG_STRING)) {
            try {
                owner = UUID.fromString(tag.getString("Owner"));
            } catch (IllegalArgumentException ignored) {
                owner = null;
            }
        }
        if (owner != null) {
            PRIMARY_MODULES.putIfAbsent(owner, this);
        }
    }

    // ---------- ProjectE helpers ----------
    private IKnowledgeProvider ownerProvider() {
        if (owner == null) return null;
        try {
            return ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(owner);
        } catch (Throwable t) {
            return null;
        }
    }

    private java.util.List<java.util.UUID> teamMembers() {
        java.util.List<java.util.UUID> out = new java.util.ArrayList<>();
        if (owner == null) return out;
        if (!ProjCalcConfig.TEAM_SHARING.get()) {
            out.add(owner);
            return out;
        }
        if (!(level instanceof net.minecraft.server.level.ServerLevel sl)) {
            out.add(owner);
            return out;
        }
        net.minecraft.server.level.ServerPlayer ownerPlayer = sl.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer == null) {
            out.add(owner);
            return out;
        }
        net.minecraft.world.scores.PlayerTeam team = sl.getServer().getScoreboard().getPlayersTeam(ownerPlayer.getScoreboardName());
        if (team == null) {
            out.add(owner);
            return out;
        }
        for (String name : team.getPlayers()) {
            net.minecraft.server.level.ServerPlayer p = sl.getServer().getPlayerList().getPlayerByName(name);
            if (p != null) out.add(p.getUUID());
        }
        if (out.isEmpty()) out.add(owner);
        return out;
    }

    private java.util.List<IKnowledgeProvider> providers() {
        java.util.List<IKnowledgeProvider> out = new java.util.ArrayList<>();
        for (java.util.UUID uuid : teamMembers()) {
            try {
                IKnowledgeProvider p = ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(uuid);
                if (p != null) out.add(p);
            } catch (Throwable ignored) {
            }
        }
        if (out.isEmpty()) {
            IKnowledgeProvider p = ownerProvider();
            if (p != null) out.add(p);
        }
        return out;
    }

    private void syncProvider(IKnowledgeProvider provider) {
        if (level instanceof ServerLevel serverLevel && owner != null) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(owner);
            if (player != null) {
                try {
                    provider.syncEmc(player);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private List<ItemInfo> knownItems() {
        long now = System.currentTimeMillis();
        if (cachedKnownItems != null && now - cachedKnownAt < ProjCalcConfig.CACHE_TTL_MS.get()) {
            return new ArrayList<>(cachedKnownItems);
        }
        java.util.Set<ItemInfo> set = new java.util.LinkedHashSet<>();
        for (IKnowledgeProvider p : providers()) {
            try {
                for (ItemInfo info : p.getKnowledge()) {
                    try {
                        if (IEMCProxy.INSTANCE.hasValue(info)) set.add(info);
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        cachedKnownItems = new ArrayList<>(set);
        cachedKnownAt = now;
        return new ArrayList<>(cachedKnownItems);
    }

    private void invalidateCache() {
        cachedKnownItems = null;
        cachedEntries = null;
    }

    private boolean hasKnowledge(ItemInfo info) {
        for (IKnowledgeProvider p : providers()) {
            try { if (p.hasKnowledge(info)) return true; } catch (Throwable ignored) { }
        }
        return false;
    }

    private long valueOf(ItemInfo info) {
        try {
            return IEMCProxy.INSTANCE.getValue(info);
        } catch (Throwable t) {
            return 0L;
        }
    }

    private BigInteger availableEmc() {
        BigInteger total = ZERO;
        for (IKnowledgeProvider p : providers()) {
            try {
                BigInteger emc = p.getEmc();
                if (emc != null) total = total.add(emc);
            } catch (Throwable ignored) {
            }
        }
        return total;
    }

    public long getEmcLong() {
        java.math.BigInteger emc = availableEmc();
        if (emc.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            return Long.MAX_VALUE;
        }
        return emc.longValue();
    }

    public void handleTransmutationInput(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        IKnowledgeProvider p = ownerProvider();
        if (p == null) return;
        ItemInfo info = ItemInfo.fromStack(stack);
        long value;
        try {
            if (!IEMCProxy.INSTANCE.hasValue(info)) {
                ProjCalcLog.info("Ignored item (no EMC value): " + stack);
                return;
            }
            value = IEMCProxy.INSTANCE.getValue(info);
        } catch (Throwable t) {
            ProjCalcLog.info("Ignored item (error checking value): " + stack);
            return;
        }
        if (value <= 0) return;

        if (!p.hasKnowledge(info)) {
            try {
                p.addKnowledge(info);
                ProjCalcLog.info("Learned new item: " + stack);
                if (level instanceof net.minecraft.server.level.ServerLevel sl && owner != null) {
                    net.minecraft.server.level.ServerPlayer player = sl.getServer().getPlayerList().getPlayer(owner);
                    if (player != null) p.syncKnowledgeChange(player, info, true);
                }
            } catch (Throwable ignored) {
            }
        }

        try {
            java.math.BigInteger add = java.math.BigInteger.valueOf(value).multiply(java.math.BigInteger.valueOf(stack.getCount()));
            p.setEmc(p.getEmc().add(add));
            syncProvider(p);
            invalidateCache();
            ProjCalcLog.info("Added EMC from item: " + stack + " +" + add + " -> total " + p.getEmc());
        } catch (Throwable ignored) {
        }
    }

    private int countFor(ItemInfo info) {
        long value = valueOf(info);
        if (value <= 0) return 0;
        BigInteger emc = availableEmc();
        BigInteger count = emc.divide(BigInteger.valueOf(value));
        if (count.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            return Integer.MAX_VALUE;
        }
        return count.intValue();
    }

    // ---------- IItemHandler ----------
    @Override
    public int getSlots() {
        return knownItems().size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        List<ItemInfo> items = knownItems();
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        ItemInfo info = items.get(slot);
        int count = countFor(info);
        if (count <= 0) return ItemStack.EMPTY;
        ItemStack stack = info.createStack();
        stack.setCount(count);
        return stack;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        IKnowledgeProvider p = ownerProvider();
        if (p == null) return stack;

        ItemInfo info = ItemInfo.fromStack(stack);
        long value = valueOf(info);
        if (value <= 0) return stack;
        try {
            if (!IEMCProxy.INSTANCE.hasValue(info)) return stack;
        } catch (Throwable t) {
            return stack;
        }

        if (!p.hasKnowledge(info)) {
            try {
                p.addKnowledge(info);
                ProjCalcLog.info("Learned new item: " + stack);
                if (!simulate && level instanceof ServerLevel sl && owner != null) {
                    ServerPlayer player = sl.getServer().getPlayerList().getPlayer(owner);
                    if (player != null) p.syncKnowledgeChange(player, info, true);
                }
            } catch (Throwable ignored) {
            }
        }

        BigInteger add = BigInteger.valueOf(value).multiply(BigInteger.valueOf(stack.getCount()));
        if (!simulate) {
            try {
                p.setEmc(p.getEmc().add(add));
                syncProvider(p);
                invalidateCache();
            } catch (Throwable ignored) {
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        List<ItemInfo> items = knownItems();
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        ItemInfo info = items.get(slot);
        long value = valueOf(info);
        if (value <= 0) return ItemStack.EMPTY;

        if (!hasKnowledge(info)) return ItemStack.EMPTY;

        BigInteger emc = availableEmc();
        BigInteger costPer = BigInteger.valueOf(value);
        BigInteger maxByEmc = emc.divide(costPer);
        int toExtract = amount;
        if (maxByEmc.compareTo(BigInteger.valueOf(amount)) < 0) {
            toExtract = maxByEmc.intValue();
        }
        if (toExtract <= 0) {
            ProjCalcLog.info("Insufficient EMC for " + info.createStack().getDisplayName().getString() + ": need " + costPer.multiply(BigInteger.valueOf(amount)) + ", have " + emc);
            return ItemStack.EMPTY;
        }

        BigInteger cost = costPer.multiply(BigInteger.valueOf(toExtract));
        IKnowledgeProvider target = null;
        for (IKnowledgeProvider cand : providers()) {
            try {
                if (cand.hasKnowledge(info) && cand.getEmc().compareTo(cost) >= 0) { target = cand; break; }
            } catch (Throwable ignored) {
            }
        }
        if (target == null) {
            // fallback: first provider
            if (!providers().isEmpty()) target = providers().get(0);
        }
        if (target == null) return ItemStack.EMPTY;

        if (!simulate) {
            try {
                target.setEmc(target.getEmc().subtract(cost));
                syncProvider(target);
                invalidateCache();
                ProjCalcLog.info("Extracted " + toExtract + "x " + info.createStack().getDisplayName().getString() + " (EMC -" + cost + ", remaining " + target.getEmc() + ")");
            } catch (Throwable ignored) {
            }
        }
        ItemStack stack = info.createStack();
        stack.setCount(toExtract);
        return stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        try {
            ItemInfo info = ItemInfo.fromStack(stack);
            return IEMCProxy.INSTANCE.hasValue(info);
        } catch (Throwable t) {
            return false;
        }
    }

    // ---------- BulkItemStore ----------
    @Override
    public Iterable<ItemStack> entries() {
        if (owner != null) {
            long now = System.currentTimeMillis();
            Long last = LAST_ENTRIES_LOG.get(owner);
            if (last == null || now - last > 5000) {
                LAST_ENTRIES_LOG.put(owner, now);
                ProjCalcLog.info("SCC queried module: " + computeEntries().size() + " items available, EMC=" + availableEmc());
            }
        }
        if (owner != null) {
            EmcTransmutationModuleBlockEntity primary = PRIMARY_MODULES.get(owner);
            if (primary != null && primary != this) {
                return List.of();
            }
        }
        long now = System.currentTimeMillis();
        if (cachedEntries != null && now - cachedEntriesAt < ProjCalcConfig.CACHE_TTL_MS.get()) {
            return new ArrayList<>(cachedEntries);
        }
        List<ItemStack> list = computeEntries();
        cachedEntries = new ArrayList<>(list);
        cachedEntriesAt = now;
        return new ArrayList<>(list);
    }

    private List<ItemStack> computeEntries() {
        List<ItemStack> list = new ArrayList<>();
        BigInteger emc = availableEmc();
        for (ItemInfo info : knownItems()) {
            long value = valueOf(info);
            if (value <= 0) continue;
            BigInteger count = emc.divide(BigInteger.valueOf(value));
            if (count.signum() <= 0) continue;
            int c = count.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 ? Integer.MAX_VALUE : count.intValue();
            ItemStack stack = info.createStack();
            stack.setCount(c);
            list.add(stack);
        }
        return list;
    }

    @Override
    public ItemStack takeMatching(java.util.function.Predicate<ItemStack> predicate, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        for (ItemInfo info : knownItems()) {
            ItemStack rep = info.createStack();
            rep.setCount(1);
            if (!predicate.test(rep)) continue;

            long value = valueOf(info);
            if (value <= 0) continue;
            if (!hasKnowledge(info)) continue;

            BigInteger emc = availableEmc();
            BigInteger maxByEmc = emc.divide(BigInteger.valueOf(value));
            int toExtract = amount;
            if (maxByEmc.compareTo(BigInteger.valueOf(amount)) < 0) {
                toExtract = maxByEmc.intValue();
            }
            if (toExtract <= 0) {
                ProjCalcLog.info("Insufficient EMC for takeMatching: " + info.createStack().getDisplayName().getString());
                continue;
            }

            BigInteger cost = BigInteger.valueOf(value).multiply(BigInteger.valueOf(toExtract));
            IKnowledgeProvider target = null;
            for (IKnowledgeProvider cand : providers()) {
                try { if (cand.hasKnowledge(info) && cand.getEmc().compareTo(cost) >= 0) { target = cand; break; } } catch (Throwable ignored) { }
            }
            if (target == null && !providers().isEmpty()) target = providers().get(0);
            if (target == null) continue;

            if (!simulate) {
                try {
                    target.setEmc(target.getEmc().subtract(cost));
                    syncProvider(target);
                    invalidateCache();
                } catch (Throwable ignored) {
                }
            }
            ItemStack stack = info.createStack();
            stack.setCount(toExtract);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean indexed() {
        return false;
    }

    @Override
    public boolean unlimited() {
        return false;
    }

    @Override
    public long stored() {
        long total = 0;
        for (ItemStack stack : computeEntries()) {
            total += stack.getCount();
        }
        return total;
    }

    @Override
    public long capacity() {
        long s = stored();
        return Math.max(s, CAPACITY_FLOOR);
    }
}
