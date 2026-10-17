package me.thanhtnt2.fluidcompact.compat.flowingfluids;

import java.util.List;
import java.util.Set;

import me.thanhtnt2.fluidcompact.api.compat.CompatModule;
import me.thanhtnt2.fluidcompact.compat.common.PipeBlockMatcher;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Makes Flowing Fluids treat pipes as blocks that stay submerged instead of solid obstacles that get drained
 * and flicker. A separate module from the original Flowing Fluids backend, which stays untouched.
 */
public final class FlowingFluidsPipesCompatModule implements CompatModule {

    public static final String ID = "flowing_fluids_pipes";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String modId() {
        return "flowing_fluids";
    }

    @Override
    public String displayName() {
        return "Flowing Fluids (submerged pipes)";
    }

    @Override
    public void initialize() {
        if (!FluidCompactCompatConfig.FF_PIPES_ENABLED.get()) {
            FluidCompactLog.info("[flowing_fluids_pipes] disabled in config");
            return;
        }
        List<Block> pipes = PipeBlockMatcher.collectBlocks();
        List<TagKey<Block>> tags = PipeBlockMatcher.collectTags();
        FluidCompactLog.info("[flowing_fluids_pipes] {} pipe block(s) matched", pipes.size());
        logMatchedBlocks(pipes);

        if (FluidCompactCompatConfig.FF_PIPES_NON_DISPLACERS.get()) {
            FlowingFluidsPipeRules.registerNonDisplacers(pipes, tags);
        }
        NeoForge.EVENT_BUS.addListener(FlowingFluidsPipeRules::onServerStarted);
        if (FluidCompactCompatConfig.FF_PIPES_CLEAN_ORPHAN_WATER.get()) {
            OrphanPipeWaterJanitor.register(Set.copyOf(pipes));
        }
        if (FluidCompactLog.isDebugMode()) {
            PipeUpdateTracer.register(Set.copyOf(pipes));
            FluidCompactLog.info("[flowing_fluids_pipes] debug mode: tracing every pipe block update");
        }
    }

    private static void logMatchedBlocks(List<Block> pipes) {
        StringBuilder ids = new StringBuilder();
        for (Block block : pipes) {
            ids.append(BuiltInRegistries.BLOCK.getKey(block)).append(' ');
        }
        FluidCompactLog.debug("[flowing_fluids_pipes] matched blocks: {}", ids.toString().trim());
    }
}
