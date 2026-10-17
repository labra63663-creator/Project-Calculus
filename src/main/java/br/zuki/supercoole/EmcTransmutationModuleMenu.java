package br.zuki.supercoole;

import java.math.BigInteger;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.SimpleContainer;

public class EmcTransmutationModuleMenu extends AbstractContainerMenu {
    public static final int TOP_PANEL_WIDTH = 144;
    public static final int TOP_PANEL_HEIGHT = 100;
    public static final int INV_PANEL_WIDTH = 176;
    public static final int INV_PANEL_HEIGHT = 84;
    public static final int SLOT_SIZE = 16;
    public static final int IMAGE_WIDTH = INV_PANEL_WIDTH;
    public static final int IMAGE_HEIGHT = TOP_PANEL_HEIGHT + 8 + INV_PANEL_HEIGHT;
    public static final int TOP_PANEL_OFFSET_X = (IMAGE_WIDTH - TOP_PANEL_WIDTH) / 2;
    public static final int SLOT_X = TOP_PANEL_OFFSET_X + (TOP_PANEL_WIDTH - SLOT_SIZE) / 2;
    public static final int SLOT_Y = (TOP_PANEL_HEIGHT - SLOT_SIZE) / 2;
    public static final int INV_Y = TOP_PANEL_HEIGHT + 8 + 4;

    private final BlockPos pos;
    private final Level level;
    private final SimpleContainer input = new SimpleContainer(1);
    private final ContainerData data;

    public EmcTransmutationModuleMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        this(containerId, inv, buf.readBlockPos());
    }

    public EmcTransmutationModuleMenu(int containerId, Inventory inv) {
        this(containerId, inv, BlockPos.ZERO);
    }

    public EmcTransmutationModuleMenu(int containerId, Inventory inv, BlockPos pos) {
        super(ModMenus.EMC_MODULE.get(), containerId);
        this.pos = pos;
        this.level = inv.player.level();

        this.data = new SimpleContainerData(2);
        addDataSlots(this.data);

        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EmcTransmutationModuleBlockEntity module) {
                long emc = module.getEmcLong();
                this.data.set(0, (int) (emc >>> 32));
                this.data.set(1, (int) emc);
            }
        }

        // Input slot (centered)
        addSlot(new Slot(input, 0, SLOT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(net.minecraft.world.item.ItemStack stack) {
                return isValidInput(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                EmcTransmutationModuleMenu.this.onInputChanged();
            }
        });

        // Player inventory
        int invY = INV_Y;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, invY + 58));
        }
    }

    public static boolean isValidInput(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        try {
            moze_intel.projecte.api.ItemInfo info = moze_intel.projecte.api.ItemInfo.fromStack(stack);
            if (!moze_intel.projecte.api.proxy.IEMCProxy.INSTANCE.hasValue(info)) return false;
            return moze_intel.projecte.api.proxy.IEMCProxy.INSTANCE.getValue(info) > 0;
        } catch (Throwable t) {
            return false;
        }
    }

    private void onInputChanged() {
        if (level.isClientSide()) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof EmcTransmutationModuleBlockEntity module)) return;
        ItemStack stack = input.getItem(0);
        if (stack.isEmpty()) return;
        module.handleTransmutationInput(stack);
        long emc = module.getEmcLong();
        this.data.set(0, (int) (emc >>> 32));
        this.data.set(1, (int) emc);
        input.setItem(0, ItemStack.EMPTY);
        // Update data slots (EMC) from provider
        try {
            // Access via reflection? Instead store module reference? We'll read from block entity each tick not possible; set now via module's available EMC if exposed.
        } catch (Throwable ignored) {
        }
        broadcastChanges();
    }

    public long getEmc() {
        if (level.isClientSide()) {
            long hi = data.get(0);
            long lo = data.get(1) & 0xFFFFFFFFL;
            return (hi << 32) | lo;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EmcTransmutationModuleBlockEntity module) {
            return module.getEmcLong();
        }
        return 0L;
    }

    @Override
    public boolean stillValid(Player player) {
        if (pos.equals(BlockPos.ZERO)) return true;
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!isValidInput(stack)) return ItemStack.EMPTY;
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setChanged();
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
