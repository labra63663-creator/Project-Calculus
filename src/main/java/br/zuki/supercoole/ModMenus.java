package br.zuki.supercoole;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SuperCoolE.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<EmcTransmutationModuleMenu>> EMC_MODULE =
            MENUS.register("emc_transmutation_module", () ->
                    new MenuType<>(EmcTransmutationModuleMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenus() {}
}
