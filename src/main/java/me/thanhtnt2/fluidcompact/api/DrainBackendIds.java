package me.thanhtnt2.fluidcompact.api;

import net.minecraft.resources.ResourceLocation;

/** Ids of the backends shipped with FluidCompact. */
public final class DrainBackendIds {

    public static final ResourceLocation VANILLA =
            ResourceLocation.fromNamespaceAndPath(FluidCompactApi.MOD_ID, "vanilla");

    public static final ResourceLocation FLOWING_FLUIDS =
            ResourceLocation.fromNamespaceAndPath(FluidCompactApi.MOD_ID, "flowing_fluids");

    private DrainBackendIds() {
    }
}
