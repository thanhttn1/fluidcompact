package me.thanhtnt2.fluidcompact.compat.immersiveengineering;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

/**
 * The decisions behind the Immersive Engineering Fluid Pump hooks.
 *
 * <p><b>Endless source.</b> The pump asks {@code FluidState.canConvertToSource} in two places. When the answer is
 * yes, it fills its tank for free from any source block that has two source neighbours (vanilla's "infinite water"
 * rule) and its area search skips that fluid completely. Flowing Fluids does not change that answer, so with the
 * gamerule on the pump would make water out of nothing. Answering "no" for finite fluids sends the pump down the
 * path it already uses for lava or oil: it takes real source blocks out of the world, 1000 mB per block, which keeps
 * the water count.
 *
 * <p><b>Cobblestone.</b> On that path the pump replaces each emptied block with cobblestone. For a finite fluid
 * that would leave stone where the pond should shrink, so the replacement is skipped and Flowing Fluids refills
 * the gap.
 *
 * <p><b>Circuit breaker.</b> If a hook keeps failing (for example after an Immersive Engineering or Flowing Fluids
 * update changed something this code relies on) it switches itself off for the rest of the session and the pump
 * goes back to its original behaviour.
 */
public final class FinitePumpRules {

    // After this many errors the hooks stop for the session instead of failing on every pump operation
    private static final int MAX_FAILURES = 5;
    private static final AtomicInteger FAILURES = new AtomicInteger();
    private static volatile boolean tripped;

    private FinitePumpRules() {
    }

    /**
     * @return what the pump should believe: true only if the fluid really may be an endless source
     */
    public static boolean mayConvertToSource(FluidState fluidState, Level level, BlockPos pos) {
        boolean originalAnswer = fluidState.canConvertToSource(level, pos);
        if (!originalAnswer || tripped) {
            return originalAnswer; // Already finite (lava, oil) or switched off: nothing to change
        }
        try {
            return !(FluidCompactCompatConfig.IE_PUMP_FINITE.get() && isFinite(fluidState));
        } catch (RuntimeException | LinkageError error) {
            recordFailure(error);
            return originalAnswer;
        }
    }

    /**
     * Called just before the pump takes the block at the end of its work list. Returns that position when its fluid
     * is finite and the cobblestone replacement should be skipped, otherwise null.
     */
    public static BlockPos finiteDrainTarget(Level level, List<BlockPos> workList) {
        if (tripped || level == null || workList.isEmpty()) {
            return null;
        }
        try {
            if (!FluidCompactCompatConfig.IE_PUMP_NO_COBBLE.get()) {
                return null;
            }
            // The pump works from the end of its list, see FluidPumpBlockEntity.tickServer
            BlockPos target = workList.get(workList.size() - 1);
            return isFinite(level.getFluidState(target)) ? target.immutable() : null;
        } catch (RuntimeException | LinkageError error) {
            recordFailure(error);
            return null;
        }
    }

    /**
     * @return true when the cobblestone the pump is about to place sits exactly where a finite fluid was taken
     */
    public static boolean shouldSkipCobble(BlockPos finiteDrainTarget, BlockPos cobblePos) {
        boolean skip = finiteDrainTarget != null && finiteDrainTarget.equals(cobblePos);
        if (skip && FluidCompactLog.isDetailLoggingActive()) {
            FluidCompactLog.debugThrottled("immersiveengineering-no-cobble",
                    "[immersiveengineering] Fluid Pump emptied a finite fluid at {}: no cobblestone placed", cobblePos.toShortString());
        }
        return skip;
    }

    private static boolean isFinite(FluidState fluidState) {
        return !fluidState.isEmpty()
                && FluidCompactApi.isReady()
                && FluidCompactApi.service().isFinite(fluidState.getType());
    }

    private static void recordFailure(Throwable error) {
        FluidCompactLog.errorThrottled("immersiveengineering-pump", "Immersive Engineering pump hook failed", error);
        if (FAILURES.incrementAndGet() >= MAX_FAILURES && !tripped) {
            tripped = true;
            FluidCompactLog.warn("[immersiveengineering] Fluid Pump hooks switched off for this session after repeated errors; "
                    + "the pump is back to its original behaviour. Update FluidCompact.");
        }
    }
}
