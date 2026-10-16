package me.thanhtnt2.fluidcompact.compat.mekanism;

import me.thanhtnt2.fluidcompact.api.compat.CompatModule;
import me.thanhtnt2.fluidcompact.compat.CompatMixinState;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;

/** Mekanism: Electric Pump stops duplicating water and lava. The hook itself is a mixin, see {@code mixin.impl}. */
public final class MekanismCompatModule implements CompatModule {

    public static final String ID = "mekanism";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String modId() {
        return "mekanism";
    }

    @Override
    public String displayName() {
        return "Mekanism (Electric Pump)";
    }

    @Override
    public void initialize() {
        if (CompatMixinState.isApplied(ID)) {
            FluidCompactLog.info("[mekanism] Electric Pump hook active");
            return;
        }
        FluidCompactLog.warn("[mekanism] Electric Pump hook is NOT applied; the pump keeps vanilla Mekanism behaviour");
    }
}
