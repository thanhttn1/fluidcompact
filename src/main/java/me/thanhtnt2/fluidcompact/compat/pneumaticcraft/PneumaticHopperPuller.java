package me.thanhtnt2.fluidcompact.compat.pneumaticcraft;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Lets a Liquid Hopper drain the thin leftover water of a pond into any tank, through the public NeoForge fluid
 * capability and the FluidCompact intake API. PneumaticCraft itself only picks up whole source blocks, so what
 * is left when a pond runs low would otherwise stay behind.
 *
 * <p>It never links against PneumaticCraft classes: the two direction getters are called by name.
 * Every failure falls back to "do nothing", so the hopper keeps working exactly as PneumaticCraft made it.
 */
public final class PneumaticHopperPuller {

    private static final Map<String, Method> DIRECTION_GETTERS = new ConcurrentHashMap<>();

    private PneumaticHopperPuller() {
    }

    /**
     * @param rateUnits the transfer-rate argument PneumaticCraft passes to {@code doImport} (100 mB per unit)
     * @return true when water was moved into the hopper this call
     */
    public static boolean tryPullLeftover(BlockEntity hopper, int rateUnits) {
        if (!(hopper.getLevel() instanceof ServerLevel level) || !FluidCompactApi.isReady()) {
            return false;
        }
        try {
            return pull(level, hopper, rateUnits);
        } catch (RuntimeException | LinkageError error) {
            FluidCompactLog.error("Liquid Hopper leftover water pull failed at " + hopper.getBlockPos().toShortString(), error);
            return false;
        }
    }

    private static boolean pull(ServerLevel level, BlockEntity hopper, int rateUnits) {
        if (!FluidCompactCompatConfig.PNC_LEFTOVER_WATER.get()) {
            return false;
        }
        HopperPullSettings settings = hopper.getData(PneumaticAttachments.HOPPER_PULL_SETTINGS.get());
        if (!settings.enabled() || level.getGameTime() < settings.nextAttemptGameTime()) {
            return false;
        }
        Direction input = directionOf(hopper, "getInputDirection");
        Direction output = directionOf(hopper, "getRotation");
        if (input == null || output == null) {
            return false;
        }
        BlockPos hopperPos = hopper.getBlockPos();
        BlockPos inputPos = hopperPos.relative(input);
        int leftoverLevels = leftoverLevelsAt(level, inputPos);
        boolean hasUpgrade = PneumaticPickupBridge.pickupWasAttempted(level, inputPos);
        if (leftoverLevels <= 0 || !(hasUpgrade || FluidCompactCompatConfig.PNC_LEFTOVER_WITHOUT_UPGRADE.get())) {
            return false;
        }
        FiniteFluidService service = FluidCompactApi.service();
        if (!service.isFinite(Fluids.WATER)) {
            return false;
        }
        IFluidHandler hopperTank = level.getCapability(Capabilities.FluidHandler.BLOCK, hopperPos, input);
        if (hopperTank == null) {
            return false;
        }
        int levels = levelsTheTankAccepts(hopperTank, rateUnits);
        if (!hasUpgrade) {
            // Without the upgrade the hopper only gets the thin leftover, never more than the input cell holds
            levels = Math.min(levels, leftoverLevels);
        }
        if (levels <= 0 || !hasConnectedOutput(level, hopperPos, output)) {
            return false;
        }
        return drainInto(level, hopper, settings, hopperTank, inputPos, levels);
    }

    private static boolean drainInto(ServerLevel level, BlockEntity hopper, HopperPullSettings settings,
            IFluidHandler hopperTank, BlockPos inputPos, int levels) {
        ResourceLocation machineId = BuiltInRegistries.BLOCK.getKey(hopper.getBlockState().getBlock());
        FluidIntakeRequest request = new FluidIntakeRequest(
                level, hopper.getBlockPos(), machineId, Fluids.WATER, levels, List.of(inputPos));
        FluidIntakeResult result = FluidCompactApi.service().requestIntake(request);
        if (!result.managed()) {
            return false;
        }
        if (result.drainedLevels() <= 0) {
            // Nothing left to take: stay quiet for a while instead of asking every tick
            long retryAt = level.getGameTime() + FluidCompactCompatConfig.PNC_EMPTY_RETRY_TICKS.get();
            hopper.setData(PneumaticAttachments.HOPPER_PULL_SETTINGS.get(), settings.withNextAttempt(retryAt));
            return false;
        }
        int millibuckets = result.drainedLevels() * FluidCompactApi.MILLIBUCKETS_PER_LEVEL;
        int filled = hopperTank.fill(new FluidStack(Fluids.WATER, millibuckets), IFluidHandler.FluidAction.EXECUTE);
        FluidCompactLog.debugThrottled("pnc-hopper@" + hopper.getBlockPos().asLong(),
                "Liquid Hopper leftover pull pos={} drainedLevels={} filledMb={}",
                hopper.getBlockPos().toShortString(), result.drainedLevels(), filled);
        return filled > 0;
    }

    /**
     * The partial water amount (1 to 7 levels) of a real fluid cell, the "leftover" PneumaticCraft cannot pick up.
     * Waterlogged blocks and full source blocks give 0: those are not leftover.
     */
    private static int leftoverLevelsAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof LiquidBlock)) {
            return 0;
        }
        FluidState fluidState = state.getFluidState();
        if (!fluidState.is(FluidTags.WATER) || fluidState.isSource()) {
            return 0;
        }
        return fluidState.getAmount();
    }

    // At least one whole level per call, otherwise the 100 mB rate unit would never add up to a level
    private static int levelsTheTankAccepts(IFluidHandler hopperTank, int rateUnits) {
        int wantedLevels = Math.max(1, Math.ceilDiv(Math.max(1, rateUnits) * 100, FluidCompactApi.MILLIBUCKETS_PER_LEVEL));
        int accepted = hopperTank.fill(
                new FluidStack(Fluids.WATER, wantedLevels * FluidCompactApi.MILLIBUCKETS_PER_LEVEL), IFluidHandler.FluidAction.SIMULATE);
        return accepted / FluidCompactApi.MILLIBUCKETS_PER_LEVEL;
    }

    private static boolean hasConnectedOutput(ServerLevel level, BlockPos hopperPos, Direction output) {
        if (!FluidCompactCompatConfig.PNC_REQUIRE_OUTPUT_CONTAINER.get()) {
            return true;
        }
        IFluidHandler container = level.getCapability(
                Capabilities.FluidHandler.BLOCK, hopperPos.relative(output), output.getOpposite());
        return container != null
                && container.fill(new FluidStack(Fluids.WATER, FluidCompactApi.MILLIBUCKETS_PER_LEVEL), IFluidHandler.FluidAction.SIMULATE) > 0;
    }

    private static Direction directionOf(BlockEntity hopper, String getterName) {
        try {
            Method getter = DIRECTION_GETTERS.computeIfAbsent(hopper.getClass().getName() + "#" + getterName,
                    key -> findPublicGetter(hopper.getClass(), getterName));
            return getter == null ? null : (Direction) getter.invoke(hopper);
        } catch (ReflectiveOperationException | ClassCastException exception) {
            return null;
        }
    }

    private static Method findPublicGetter(Class<?> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException missing) {
            return null;
        }
    }
}
