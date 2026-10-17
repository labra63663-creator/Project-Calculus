package br.zuki.supercoole;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, SuperCoolE.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> EMC_MODULE =
            ITEMS.register("emc_transmutation_module", () -> new BlockItem(
                    ModBlocks.EMC_MODULE.get(),
                    new Item.Properties()));

    private ModItems() {}
}
