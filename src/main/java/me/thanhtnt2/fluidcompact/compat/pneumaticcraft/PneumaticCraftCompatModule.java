package me.thanhtnt2.fluidcompact.compat.pneumaticcraft;

import me.thanhtnt2.fluidcompact.api.compat.CompatModule;
import me.thanhtnt2.fluidcompact.compat.CompatMixinState;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;

/**
 * PneumaticCraft: Repressurized. Liquid Hopper and Gas Lift stop duplicating water.
 * The hook is a mixin, see {@code mixin.impl}; the rules live in {@link PneumaticPickupBridge}.
 */
public final class PneumaticCraftCompatModule implements CompatModule {

    public static final String ID = "pneumaticcraft";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String modId() {
        return "pneumaticcraft";
    }

    @Override
    public String displayName() {
        return "PneumaticCraft: Repressurized (Liquid Hopper, Gas Lift)";
    }

    @Override
    public void initialize() {
        if (CompatMixinState.isApplied(ID)) {
            FluidCompactLog.info("[pneumaticcraft] pickup hook active (Liquid Hopper, Gas Lift); drones already remove every source");
            return;
        }
        FluidCompactLog.warn("[pneumaticcraft] pickup hook is NOT applied; Liquid Hopper and Gas Lift keep their infinite water rule");
    }
}
