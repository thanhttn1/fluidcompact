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
        logHook(ID, "pickup hook (Liquid Hopper, Gas Lift: no infinite water)");
        logHook("pneumaticcraft_hopper", "Liquid Hopper leftover-water pull");
    }

    private static void logHook(String stateKey, String description) {
        if (CompatMixinState.isApplied(stateKey)) {
            FluidCompactLog.info("[pneumaticcraft] {} active", description);
            return;
        }
        FluidCompactLog.warn("[pneumaticcraft] {} is NOT applied; PneumaticCraft keeps its own behaviour there", description);
    }
}
