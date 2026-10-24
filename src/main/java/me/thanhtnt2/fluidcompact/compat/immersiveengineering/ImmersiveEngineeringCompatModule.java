package me.thanhtnt2.fluidcompact.compat.immersiveengineering;

import me.thanhtnt2.fluidcompact.api.compat.CompatModule;
import me.thanhtnt2.fluidcompact.compat.CompatMixinState;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;

/**
 * Immersive Engineering. Only the Fluid Pump takes fluid out of the world: with Flowing Fluids it is stopped from
 * making endless water. The Fluid Placer needs nothing, it puts down one source block per 1000 mB taken from its
 * tank. The single hook is a guarded mixin, see {@code mixin.impl}.
 */
public final class ImmersiveEngineeringCompatModule implements CompatModule {

    public static final String ID = "immersiveengineering";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String modId() {
        return "immersiveengineering";
    }

    @Override
    public String displayName() {
        return "Immersive Engineering (Fluid Pump)";
    }

    @Override
    public void initialize() {
        if (CompatMixinState.isApplied(ID)) {
            FluidCompactLog.info("[immersiveengineering] Fluid Pump hook active");
            return;
        }
        FluidCompactLog.warn("[immersiveengineering] Fluid Pump hook is NOT applied; the pump keeps making endless water from two-source pools");
    }
}
