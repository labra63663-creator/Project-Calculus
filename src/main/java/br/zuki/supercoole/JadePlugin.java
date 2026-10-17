package br.zuki.supercoole;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ITooltip;

@WailaPlugin("projcalc")
public class JadePlugin implements IWailaPlugin {
    private static final ResourceLocation EMC_DATA = ResourceLocation.fromNamespaceAndPath(SuperCoolE.MOD_ID, "emc");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new IServerDataProvider<BlockAccessor>() {
            @Override
            public ResourceLocation getUid() {
                return EMC_DATA;
            }

            @Override
            public void appendServerData(CompoundTag data, BlockAccessor accessor) {
                if (accessor.getBlockEntity() instanceof EmcTransmutationModuleBlockEntity module) {
                    data.putLong("emc", module.getEmcLong());
                    if (module.getOwner() != null) {
                        String name = module.getOwner().toString();
                        if (accessor.getLevel() instanceof net.minecraft.server.level.ServerLevel sl) {
                            net.minecraft.server.level.ServerPlayer player = sl.getServer().getPlayerList().getPlayer(module.getOwner());
                            if (player != null) {
                                name = player.getGameProfile().getName();
                            } else {
                                try {
                                    java.util.Optional<com.mojang.authlib.GameProfile> profile = sl.getServer().getProfileCache().get(module.getOwner());
                                    if (profile.isPresent() && profile.get().getName() != null) {
                                        name = profile.get().getName();
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                        data.putString("owner", name);
                    }
                }
            }
        }, EmcTransmutationModuleBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new IComponentProvider<BlockAccessor>() {
            @Override
            public ResourceLocation getUid() {
                return EMC_DATA;
            }

            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                CompoundTag data = accessor.getServerData();
                if (data == null) return;
                if (data.contains("emc")) {
                    tooltip.add(Component.literal("EMC: " + formatEmc(data.getLong("emc"))));
                }
                if (data.contains("owner")) {
                    tooltip.add(Component.literal("Owner: " + data.getString("owner")));
                }
            }
        }, EmcTransmutationModuleBlock.class);
    }

    private static String formatEmc(long value) {
        if (value < 5000) {
            return String.format(java.util.Locale.GERMANY, "%,d", value);
        }
        long[] divisors = {1_000_000_000_000L, 1_000_000_000L, 1_000_000L, 1_000L};
        String[] suffixes = {"T", "B", "M", "K"};
        for (int i = 0; i < divisors.length; i++) {
            if (value >= divisors[i]) {
                double scaled = (double) value / divisors[i];
                if (scaled >= 10 || scaled == Math.floor(scaled)) {
                    return String.format(java.util.Locale.US, "%.0f%s", scaled, suffixes[i]);
                }
                return String.format(java.util.Locale.US, "%.1f%s", scaled, suffixes[i]);
            }
        }
        return String.valueOf(value);
    }
}
