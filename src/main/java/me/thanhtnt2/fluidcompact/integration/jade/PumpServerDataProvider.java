package me.thanhtnt2.fluidcompact.integration.jade;

import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import me.thanhtnt2.fluidcompact.core.FluidCompactAttachments;
import me.thanhtnt2.fluidcompact.core.PumpStatistics;
import me.thanhtnt2.fluidcompact.integration.mi.MiPumpTargeting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/** Sends finite-fluid information about a pump to the client that is looking at it. */
enum PumpServerDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity pump = accessor.getBlockEntity();
        if (!MiPumpTargeting.isWaterPump(pump) || !FluidCompactApi.isReady()) {
            return;
        }
        data.putBoolean(PumpJadeKeys.FINITE, FluidCompactApi.service().isFinite(Fluids.WATER));
        if (!pump.hasData(FluidCompactAttachments.PUMP_STATISTICS.get())) {
            return;
        }
        PumpStatistics statistics = pump.getData(FluidCompactAttachments.PUMP_STATISTICS.get());
        data.putLong(PumpJadeKeys.TOTAL_DRAINED_LEVELS, statistics.totalDrainedLevels());
        data.putLong(PumpJadeKeys.STARVED_INTAKES, statistics.starvedIntakes());
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        // Cheap client-side filter so Jade does not query the server for every hovered block
        return MiPumpTargeting.isWaterPump(accessor.getBlockEntity());
    }

    @Override
    public ResourceLocation getUid() {
        return PumpJadeKeys.PROVIDER_ID;
    }
}
