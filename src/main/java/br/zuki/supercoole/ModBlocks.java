package br.zuki.supercoole;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, SuperCoolE.MOD_ID);

    public static final DeferredHolder<Block, Block> EMC_MODULE =
            BLOCKS.register("emc_transmutation_module", () -> new EmcTransmutationModuleBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(3.5f)
                            .requiresCorrectToolForDrops()));

    private ModBlocks() {}
}
