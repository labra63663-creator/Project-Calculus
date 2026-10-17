package br.zuki.supercoole;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SuperCoolE.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EmcTransmutationModuleBlockEntity>> EMC_MODULE =
            BLOCK_ENTITY_TYPES.register("emc_transmutation_module", () ->
                    new BlockEntityType<EmcTransmutationModuleBlockEntity>(EmcTransmutationModuleBlockEntity::new, java.util.Set.of(ModBlocks.EMC_MODULE.get()), null));

    private ModBlockEntities() {}
}
