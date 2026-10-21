package me.thanhtnt2.fluidcompact.compat.alexscaves;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import me.thanhtnt2.fluidcompact.FluidCompact;
import me.thanhtnt2.fluidcompact.api.FiniteFluidService;
import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import me.thanhtnt2.fluidcompact.api.FluidIntakeRequest;
import me.thanhtnt2.fluidcompact.api.FluidIntakeResult;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Makes the Alex's Caves Drain work with finite fluids.
 *
 * <p>The original Drain removes fluid blocks one by one and puts the same number of full blocks below. With
 * Flowing Fluids a cell holds 1 to 8 levels, so that "one block" arithmetic neither matches what was removed
 * nor what Flowing Fluids allows. Here the Drain instead asks {@link FiniteFluidService} to take levels from
 * the fluid above and places exactly that many levels below.
 *
 * <p>Dry-valve fix: the Copper Valve normally sits in the cell directly above the Drain. FluidCompact sets the
 * Flowing Fluids waterlog flow mode to IGNORE, so a valve that was not waterlogged when placed (or that was
 * emptied once) can never receive water again. Looking only at that cell made the Drain stop for good, so the
 * cells touching a dry waterloggable block are probed as well, and the valve cell is never used as a source.
 *
 * <p>Called from the mixin on the server thread. Has no reference to Alex's Caves classes.
 */
public final class AlexsCavesDrainBridge {

    private static final ResourceLocation FALLBACK_MACHINE_ID =
            ResourceLocation.fromNamespaceAndPath(FluidCompact.MODID, "unknown_drain");

    // Probe order next to a dry valve: the lake surface above first, then the four sides
    private static final Direction[] VALVE_PROBE_DIRECTIONS =
            {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private AlexsCavesDrainBridge() {
    }

    /** Where the Drain starts pulling from, and which fluid it found there. */
    private record FluidSource(BlockPos origin, FlowingFluid fluid) {
    }

    /**
     * @return true when FluidCompact did the work and the original Drain logic must be skipped;
     *         false when the original logic should run unchanged
     */
    public static boolean tryHandle(BlockState drainState, Level level, BlockPos drainPos) {
        try {
            return handle(drainState, level, drainPos);
        } catch (RuntimeException exception) {
            // Fail open: nothing has been changed yet when planning throws, so the original logic may run
            FluidCompactLog.error("Alex's Caves Drain hook failed at " + drainPos.toShortString(), exception);
            return false;
        }
    }

    private static boolean handle(BlockState drainState, Level level, BlockPos drainPos) {
        if (!(level instanceof ServerLevel serverLevel) || !FluidCompactApi.isReady()) {
            return false;
        }
        if (!FluidCompactCompatConfig.ALEXS_DRAIN_ENABLED.get()) {
            return false;
        }

        Optional<FluidSource> located = locateFluidSource(serverLevel, drainPos);
        if (located.isEmpty()) {
            // Nothing to drain (or a fluid that is not level based): the original logic handles both
            logSkip(drainPos, () -> "no fluid above the Drain or beside the valve above it");
            return false;
        }
        FluidSource source = located.get();
        FiniteFluidService service = FluidCompactApi.service();
        if (!service.isFinite(source.fluid())) {
            return false;
        }

        DrainFillPlan plan = DrainFillPlan.create(serverLevel, drainPos, source.fluid(),
                FluidCompactCompatConfig.ALEXS_DRAIN_MAX_LEVELS_PER_CYCLE.get(),
                FluidCompactCompatConfig.ALEXS_DRAIN_FILL_SPREAD.get());
        if (plan.isEmpty()) {
            // Nowhere to put the fluid: taking it anyway would destroy it, so wait and look again later
            logSkip(drainPos, () -> "no room below the Drain (block below: " + describeBlockBelow(serverLevel, drainPos) + ")");
            scheduleNextCycle(serverLevel, drainPos, drainState);
            return true;
        }

        FluidIntakeResult result = service.requestIntake(buildRequest(serverLevel, drainState, drainPos, source, plan));
        if (!result.managed()) {
            return false;
        }
        moveFluid(serverLevel, drainPos, source.fluid(), plan, result);
        scheduleNextCycle(serverLevel, drainPos, drainState);
        return true;
    }

    /**
     * The cell above the Drain wins. When it holds no fluid but contains a waterloggable block (the Copper Valve),
     * the cells touching it are probed instead, because that block cannot be refilled under the IGNORE waterlog mode.
     */
    private static Optional<FluidSource> locateFluidSource(ServerLevel level, BlockPos drainPos) {
        BlockPos above = drainPos.above();
        Optional<FluidSource> direct = readFluidSource(level, above);
        if (direct.isPresent()) {
            return direct;
        }
        if (!(level.getBlockState(above).getBlock() instanceof SimpleWaterloggedBlock)) {
            return Optional.empty();
        }
        for (Direction direction : VALVE_PROBE_DIRECTIONS) {
            Optional<FluidSource> neighbour = readFluidSource(level, above.relative(direction));
            if (neighbour.isPresent()) {
                return neighbour;
            }
        }
        return Optional.empty();
    }

    private static Optional<FluidSource> readFluidSource(ServerLevel level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.isEmpty() || !(fluidState.getType() instanceof FlowingFluid flowingFluid)) {
            return Optional.empty();
        }
        return Optional.of(new FluidSource(pos.immutable(), flowingFluid));
    }

    private static FluidIntakeRequest buildRequest(
            ServerLevel level, BlockState drainState, BlockPos drainPos, FluidSource source, DrainFillPlan plan) {
        return new FluidIntakeRequest(level, drainPos, machineIdOf(drainState), source.fluid(),
                plan.totalLevels(), collectSourcePositions(level, source));
    }

    // Highest cell of the fluid column first, so the surface is lowered before the bottom is touched
    private static List<BlockPos> collectSourcePositions(ServerLevel level, FluidSource source) {
        BlockPos.MutableBlockPos cursor = source.origin().mutable();
        while (cursor.getY() < level.getMaxBuildHeight() - 1 && !level.getFluidState(cursor.above()).isEmpty()) {
            cursor.move(0, 1, 0);
        }
        List<BlockPos> sources = new ArrayList<>(2);
        sources.add(cursor.immutable());
        // Fallback only for a plain fluid cell: emptying a waterlogged block would dry it out for good
        boolean originIsPlainFluid = level.getBlockState(source.origin()).getBlock() instanceof LiquidBlock;
        if (originIsPlainFluid && !source.origin().equals(cursor)) {
            sources.add(source.origin());
        }
        return sources;
    }

    private static void moveFluid(
            ServerLevel level, BlockPos drainPos, FlowingFluid fluid, DrainFillPlan plan, FluidIntakeResult result) {
        int drainedLevels = result.drainedLevels();
        if (drainedLevels <= 0) {
            logSkip(drainPos, () -> "the finite-fluid backend removed 0 level(s); nothing was moved");
            return;
        }
        boolean playEffects = FluidCompactCompatConfig.ALEXS_DRAIN_WORLD_EFFECTS.get();
        if (playEffects) {
            DrainWorldEffects.play(DrainWorldEffects.FLUID_TAKEN, drainPos);
        }
        int placedLevels = plan.apply(level, fluid, drainedLevels);
        if (playEffects && placedLevels > 0) {
            DrainWorldEffects.play(DrainWorldEffects.FLUID_RELEASED, drainPos);
        }
        if (FluidCompactLog.isDetailLoggingActive()) {
            FluidCompactLog.debugThrottled("alexscaves-drain-" + drainPos.asLong(),
                    "[alexscaves] Drain at {} moved {} level(s): drained={} placed={}",
                    drainPos.toShortString(), result.requestedLevels(), drainedLevels, placedLevels);
        }
    }

    private static void scheduleNextCycle(ServerLevel level, BlockPos drainPos, BlockState drainState) {
        level.scheduleTick(drainPos, drainState.getBlock(), FluidCompactCompatConfig.ALEXS_DRAIN_INTERVAL_TICKS.get());
    }

    // The reason is a Supplier so no string is built while detail logging is off
    private static void logSkip(BlockPos drainPos, Supplier<String> reason) {
        if (!FluidCompactLog.isDetailLoggingActive()) {
            return;
        }
        FluidCompactLog.debugThrottled("alexscaves-drain-skip-" + drainPos.asLong(),
                "[alexscaves] Drain at {}: {}", drainPos.toShortString(), reason.get());
    }

    private static String describeBlockBelow(ServerLevel level, BlockPos drainPos) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(level.getBlockState(drainPos.below()).getBlock());
        return key != null ? key.toString() : "unknown";
    }

    private static ResourceLocation machineIdOf(BlockState drainState) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(drainState.getBlock());
        return key != null ? key : FALLBACK_MACHINE_ID;
    }
}
