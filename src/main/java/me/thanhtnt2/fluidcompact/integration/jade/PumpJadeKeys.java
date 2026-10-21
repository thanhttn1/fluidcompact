package me.thanhtnt2.fluidcompact.integration.jade;

import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import net.minecraft.resources.ResourceLocation;

/** Keys shared by the server and client halves of the Jade integration. */
final class PumpJadeKeys {

    static final ResourceLocation PROVIDER_ID =
            ResourceLocation.fromNamespaceAndPath(FluidCompactApi.MOD_ID, "pump_status");

    static final String FINITE = "fluidcompact_finite";
    static final String TOTAL_DRAINED_LEVELS = "fluidcompact_total_drained_levels";
    static final String STARVED_INTAKES = "fluidcompact_starved_intakes";

    private PumpJadeKeys() {
    }
}
