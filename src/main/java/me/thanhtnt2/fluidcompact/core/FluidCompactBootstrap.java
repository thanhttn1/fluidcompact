package me.thanhtnt2.fluidcompact.core;

import me.thanhtnt2.fluidcompact.FluidCompact;
import me.thanhtnt2.fluidcompact.api.DrainBackendRegistry;
import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import me.thanhtnt2.fluidcompact.core.backend.VanillaDrainBackend;

/** Wires the API to its default implementation. Runs once on the main thread after all mods are constructed. */
public final class FluidCompactBootstrap {

    private FluidCompactBootstrap() {
    }

    public static void initialize() {
        DrainBackendRegistry.register(new VanillaDrainBackend());
        OptionalBackends.createFlowingFluidsBackend().ifPresent(backend -> {
            DrainBackendRegistry.register(backend);
            FluidCompact.LOGGER.info("Flowing Fluids detected: finite fluids will be drained through its API");
        });
        FluidCompactApi.bind(new FiniteFluidServiceImpl());
    }
}
