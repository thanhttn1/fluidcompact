package me.thanhtnt2.fluidcompact.compat.create;

import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;
import traben.flowing_fluids.api.FlowingFluidsAPI;
import traben.flowing_fluids.config.FFConfig;

/** Reads Flowing Fluids' Create settings (read only) so the startup report can spot a mismatch with FluidCompact. */
final class CreateFlowingFluidsProbe {

    private CreateFlowingFluidsProbe() {
    }

    static void report(boolean waterIsFinite) {
        try {
            FFConfig config = FlowingFluidsAPI.getInstance(FluidCompactApi.MOD_ID).getConfig();
            FluidCompactLog.info("[create] Flowing Fluids Create settings: waterWheelMode={} infinitePipes={} waterWheelFlowMaxTickInterval={}",
                    config.create_waterWheelMode, config.create_infinitePipes, config.create_waterWheelFlowMaxTickInterval);
            if (waterIsFinite && config.create_infinitePipes) {
                // Not changed on purpose: it is the pack maker's choice, but it is worth knowing about
                FluidCompactLog.warn("[create] FluidCompact treats water as finite but Flowing Fluids has create_infinitePipes=true, "
                        + "so Create's open pipe ends still make infinite water. Set it to false in flowing_fluids.json for consistent finite water.");
            }
        } catch (RuntimeException | LinkageError unavailable) {
            FluidCompactLog.warn("[create] could not read Flowing Fluids' Create settings: {}", unavailable.toString());
        }
    }
}
