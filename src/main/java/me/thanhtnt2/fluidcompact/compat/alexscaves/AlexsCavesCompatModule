package me.thanhtnt2.fluidcompact.compat.alexscaves;

import me.thanhtnt2.fluidcompact.api.compat.CompatModule;
import me.thanhtnt2.fluidcompact.compat.CompatMixinState;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;

/**
 * Alex's Caves: the Drain block (switched on by redstone, for example from a Copper Valve) moves fluid
 * from above itself to below itself. The hook is a single guarded mixin, see {@code mixin.impl}.
 *
 * <p>The Copper Valve needs no hook: it only emits a redstone signal, which Alex's Caves already handles.
 */
public final class AlexsCavesCompatModule implements CompatModule {

    public static final String ID = "alexscaves";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String modId() {
        return "alexscaves";
    }

    @Override
    public String displayName() {
        return "Alex's Caves (Drain, Copper Valve)";
    }

    @Override
    public void initialize() {
        if (CompatMixinState.isApplied(ID)) {
            FluidCompactLog.info("[alexscaves] Drain hook active");
            return;
        }
        FluidCompactLog.warn("[alexscaves] Drain hook is NOT applied; the Drain keeps its original behaviour");
    }
}
