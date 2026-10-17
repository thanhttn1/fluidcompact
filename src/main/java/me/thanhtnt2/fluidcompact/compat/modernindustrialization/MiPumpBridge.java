package me.thanhtnt2.fluidcompact.compat.modernindustrialization;

import java.util.ArrayList;
import java.util.List;

import me.thanhtnt2.fluidcompact.FluidCompact;
import me.thanhtnt2.fluidcompact.api.FiniteFluidService;
import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import me.thanhtnt2.fluidcompact.api.FluidIntakeRequest;
import me.thanhtnt2.fluidcompact.api.FluidIntakeResult;
import me.thanhtnt2.fluidcompact.config.FluidCompactConfig;
import me.thanhtnt2.fluidcompact.core.FluidCompactAttachments;
import me.thanhtnt2.fluidcompact.core.PumpStatistics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Translates one Modern Industrialization pump cycle into a {@link FluidIntakeRequest}.
 *
 * <p>Called from the single mixin hook, right before MI adds the pumped water to its tank,
 * so the amount MI wants to add is exactly the amount we must take out of the world.
 * This class has no compile-time reference to MI.
 */
public final class MiPumpBridge {

    // Same 3x3 ring MI scans around the pump (DX/DZ in AbstractWaterPumpBlockEntity), so the
    // cost lands on the very blocks the pump counted as sources
    private static final int[] RING_DX = {-1, 0, 1, 1, 1, 0, -1, -1};
    private static final int[] RING_DZ = {-1, -1, -1, 0, 1, 1, 1, 0};

    private static final Fluid PUMPED_FLUID = Fluids.WATER;

    private MiPumpBridge() {
    }

    /**
     * @param requestedMillibuckets what MI is about to insert into the pump's output tank
     * @return what MI may insert instead: the same amount when the world could pay for it,
     *         less (possibly zero) when the surrounding water ran short
     */
    public static long limitPumpedMillibuckets(BlockEntity pump, long requestedMillibuckets) {
        if (requestedMillibuckets <= 0L || !(pump.getLevel() instanceof ServerLevel level)) {
            return requestedMillibuckets;
        }
        FiniteFluidService service = FluidCompactApi.service();
        if (!service.isFinite(PUMPED_FLUID)) {
            return requestedMillibuckets;
        }

        long gameTime = level.getGameTime();
        PumpStatistics statistics = readStatistics(pump);
        if (statistics.isCoolingDown(gameTime, FluidCompactConfig.MIN_TICKS_BETWEEN_INTAKES.get())) {
            return 0L;
        }

        FluidIntakeResult result = service.requestIntake(buildRequest(level, pump, requestedMillibuckets));
        if (!result.managed() || result.requestedLevels() <= 0) {
            return requestedMillibuckets;
        }

        writeStatistics(pump, statistics.withIntake(result.drainedLevels(), result.isFullyServed(), gameTime));
        return grantedMillibuckets(requestedMillibuckets, result);
    }

    private static FluidIntakeRequest buildRequest(ServerLevel level, BlockEntity pump, long requestedMillibuckets) {
        int requestedLevels = toLevels(requestedMillibuckets);
        int levelsToDrain = Math.ceilDiv(requestedLevels * FluidCompactConfig.DRAIN_PERCENT.get(), 100);
        BlockPos pumpPos = pump.getBlockPos();
        return new FluidIntakeRequest(
                level, pumpPos, machineIdOf(pump), PUMPED_FLUID, levelsToDrain, findSourcePositions(level, pumpPos));
    }

    private static List<BlockPos> findSourcePositions(ServerLevel level, BlockPos pumpPos) {
        List<BlockPos> sources = new ArrayList<>(RING_DX.length);
        for (int index = 0; index < RING_DX.length; index++) {
            BlockPos candidate = pumpPos.offset(RING_DX[index], 0, RING_DZ[index]);
            FluidState fluidState = level.getFluidState(candidate);
            if (fluidState.isSource() && fluidState.getType().isSame(PUMPED_FLUID)) {
                sources.add(candidate);
            }
        }
        return sources;
    }

    private static long grantedMillibuckets(long requestedMillibuckets, FluidIntakeResult result) {
        if (result.isFullyServed()) {
            return requestedMillibuckets;
        }
        // Scale down proportionally so a half-paid cycle yields half the water
        return requestedMillibuckets * result.drainedLevels() / result.requestedLevels();
    }

    private static int toLevels(long millibuckets) {
        return (int) Math.ceilDiv(millibuckets, (long) FluidCompactApi.MILLIBUCKETS_PER_LEVEL);
    }

    private static ResourceLocation machineIdOf(BlockEntity pump) {
        ResourceLocation key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(pump.getType());
        return key != null ? key : ResourceLocation.fromNamespaceAndPath(FluidCompact.MODID, "unknown_pump");
    }

    private static PumpStatistics readStatistics(BlockEntity pump) {
        return pump.getData(FluidCompactAttachments.PUMP_STATISTICS.get());
    }

    private static void writeStatistics(BlockEntity pump, PumpStatistics statistics) {
        pump.setData(FluidCompactAttachments.PUMP_STATISTICS.get(), statistics);
        pump.setChanged();
    }
}
