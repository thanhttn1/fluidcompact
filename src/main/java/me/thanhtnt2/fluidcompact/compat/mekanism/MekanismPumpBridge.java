package me.thanhtnt2.fluidcompact.compat.mekanism;

import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import me.thanhtnt2.fluidcompact.api.FluidCompactTags;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/**
 * Decision logic for the Mekanism Electric Pump hook. Has no reference to Mekanism classes.
 *
 * <p>Mekanism already removes source blocks itself when its internal {@code shouldPump} says so, and
 * skips removal for "infinite" fluids. We only flip that answer for fluids FluidCompact manages,
 * so the world is drained by Mekanism's own, well tested pickup code.
 */
public final class MekanismPumpBridge {

    private MekanismPumpBridge() {
    }

    /** @return true when the pump must remove the source block it just sucked from */
    public static boolean shouldForceSourceRemoval(Level level, Fluid fluid) {
        if (level.isClientSide() || !FluidCompactApi.isReady()) {
            return false;
        }
        try {
            return isForced(fluid);
        } catch (RuntimeException | LinkageError error) {
            // Fail open: a broken hook must never stop Mekanism's pump from working
            FluidCompactLog.errorThrottled("mekanism-electric-pump",
                    "Mekanism electric pump hook failed for fluid " + BuiltInRegistries.FLUID.getKey(fluid), error);
            return false;
        }
    }

    private static boolean isForced(Fluid fluid) {
        if (!FluidCompactCompatConfig.ELECTRIC_PUMP_ENABLED.get()) {
            return false;
        }
        if (!FluidCompactApi.service().isFinite(fluid)) {
            return false;
        }
        if (fluid.is(FluidTags.WATER)) {
            return FluidCompactCompatConfig.ELECTRIC_PUMP_WATER.get();
        }
        if (fluid.is(FluidTags.LAVA)) {
            return FluidCompactCompatConfig.ELECTRIC_PUMP_LAVA.get();
        }
        return fluid.is(FluidCompactTags.MANAGED_FLUIDS) && FluidCompactCompatConfig.ELECTRIC_PUMP_TAGGED_FLUIDS.get();
    }
}
