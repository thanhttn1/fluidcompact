package me.thanhtnt2.fluidcompact.compat.modernindustrialization;

import me.thanhtnt2.fluidcompact.api.compat.CompatModule;
import me.thanhtnt2.fluidcompact.compat.CompatMixinState;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;

/**
 * Modern Industrialization: Bronze, Steel and Electric Water Pumps drain the world instead of creating water.
 * The hook is a mixin, see {@code mixin.impl}; the rules live in {@link MiPumpBridge}.
 */
public final class ModernIndustrializationCompatModule implements CompatModule {

    public static final String ID = "modern_industrialization";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String modId() {
        return "modern_industrialization";
    }

    @Override
    public String displayName() {
        return "Modern Industrialization (Water Pumps)";
    }

    @Override
    public void initialize() {
        if (CompatMixinState.isApplied(ID)) {
            FluidCompactLog.info("[modern_industrialization] Water Pump hook active");
            return;
        }
        FluidCompactLog.warn("[modern_industrialization] Water Pump hook is NOT applied; pumps keep vanilla MI behaviour");
    }
}
