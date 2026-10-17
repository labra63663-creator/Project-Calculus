package br.zuki.supercoole;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SuperCoolE.MOD_ID)
public final class SuperCoolE {
    public static final String MOD_ID = "projcalc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SCE_TAB =
            CREATIVE_TABS.register("projcalc", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.projcalc"))
                    .icon(() -> new ItemStack(ModItems.EMC_MODULE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.EMC_MODULE.get());
                    })
                    .build());

    public SuperCoolE(IEventBus modEventBus) {
        ProjCalcLog.info("Project Calculus initialized (modId=" + MOD_ID + ")");
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerScreens);
    }

    private void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(ModMenus.EMC_MODULE.get(), EmcTransmutationModuleScreen::new);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.EMC_MODULE.get(),
                (be, side) -> (net.neoforged.neoforge.items.IItemHandler) be);

    }
}
