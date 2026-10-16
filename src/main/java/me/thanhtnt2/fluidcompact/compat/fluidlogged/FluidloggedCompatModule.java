package me.thanhtnt2.fluidcompact.compat.fluidlogged;

import me.thanhtnt2.fluidcompact.api.compat.CompatModule;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;

/** Fluidlogged: lets water and other fluids enter and flow through pipes and cables instead of breaking or blocking. */
public final class FluidloggedCompatModule implements CompatModule {

    public static final String ID = "fluidlogged";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String modId() {
        return "fluidlogged";
    }

    @Override
    public String displayName() {
        return "Fluidlogged (fluid passes through pipes)";
    }

    @Override
    public void initialize() {
        if (!FluidCompactCompatConfig.FLUIDLOGGED_ENABLED.get()) {
            FluidCompactLog.info("[fluidlogged] disabled in config, no addon registered");
            return;
        }
        FluidloggedAddonRegistrar.register();
    }
}
