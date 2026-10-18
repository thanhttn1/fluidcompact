package me.thanhtnt2.fluidcompact.compat.flowingfluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Removes the water left inside a waterlogged pipe when no neighbouring block outside the pipe network
 * holds water any more. Water is only ever deleted here, never created, so this can lose at most one
 * bucket per cleaned pipe and can never duplicate water.
 *
 * <p>Only loaded chunks near players are scanned, and only every few ticks, to keep the cost negligible.
 */
final class OrphanPipeWaterJanitor {

    private final Set<Block> pipes;

    private OrphanPipeWaterJanitor(Set<Block> pipes) {
        this.pipes = pipes;
    }

    static void register(Set<Block> pipes) {
        NeoForge.EVENT_BUS.addListener(new OrphanPipeWaterJanitor(pipes)::onLevelTick);
    }

    private void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (level.getGameTime() % FluidCompactCompatConfig.FF_PIPES_CLEANUP_INTERVAL_TICKS.get() != 0L) {
            return;
        }
        try {
            cleanAroundPlayers(level);
        } catch (RuntimeException exception) {
            // A cleanup problem must never crash the server tick
            FluidCompactLog.error("Orphan pipe water cleanup failed", exception);
        }
    }

    private void cleanAroundPlayers(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }
        int radius = FluidCompactCompatConfig.FF_PIPES_CLEANUP_CHUNK_RADIUS.get();
        LongSet visitedChunks = new LongOpenHashSet();
        int cleaned = 0;
        for (ServerPlayer player : players) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cleaned += cleanChunkOnce(level, center.x + dx, center.z + dz, visitedChunks);
                }
            }
        }
        if (cleaned > 0) {
            FluidCompactLog.debug("[flowing_fluids_pipes] cleaned {} orphan waterlogged pipe(s) in {}", cleaned, level.dimension().location());
        }
    }

    private int cleanChunkOnce(ServerLevel level, int chunkX, int chunkZ, LongSet visitedChunks) {
        if (!visitedChunks.add(ChunkPos.asLong(chunkX, chunkZ))) {
            return 0;
        }
        // getChunkNow never loads or generates a chunk, it only returns one that is already there
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) {
            return 0;
        }
        List<BlockPos> orphans = findOrphans(level, chunk);
        int cleaned = 0;
        for (BlockPos pos : orphans) {
            cleaned += drainPipe(level, pos);
        }
        return cleaned;
    }

    private List<BlockPos> findOrphans(ServerLevel level, LevelChunk chunk) {
        List<BlockPos> orphans = new ArrayList<>();
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            BlockState state = blockEntity.getBlockState();
            traceHiddenFluid(level, state, blockEntity.getBlockPos());
            if (isWaterloggedPipe(state) && !hasWaterOutsidePipes(level, blockEntity.getBlockPos())) {
                orphans.add(blockEntity.getBlockPos().immutable());
            }
        }
        return orphans;
    }

    // Debug aid: fluid that the world reports inside a pipe whose own block state says "empty" is held somewhere else
    private void traceHiddenFluid(ServerLevel level, BlockState state, BlockPos pos) {
        if (!FluidCompactLog.isDebugMode() || !pipes.contains(state.getBlock()) || !state.getFluidState().isEmpty()) {
            return;
        }
        if (!level.getFluidState(pos).isEmpty()) {
            FluidCompactLog.trace("HIDDEN fluid in pipe block={} pos={} worldFluid={} (block state is empty)",
                    BuiltInRegistries.BLOCK.getKey(state.getBlock()), pos.toShortString(),
                    BuiltInRegistries.FLUID.getKey(level.getFluidState(pos).getType()));
        }
    }

    private boolean isWaterloggedPipe(BlockState state) {
        return pipes.contains(state.getBlock())
                && state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED);
    }

    private boolean hasWaterOutsidePipes(ServerLevel level, BlockPos pipePos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pipePos.relative(direction);
            if (!level.hasChunkAt(neighbour)) {
                // Unknown neighbour: keep the water rather than guess
                return true;
            }
            // Pipes next to pipes would keep each other "wet" forever, so they do not count as a water source
            if (pipes.contains(level.getBlockState(neighbour).getBlock())) {
                continue;
            }
            if (level.getFluidState(neighbour).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    private int drainPipe(ServerLevel level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (!isWaterloggedPipe(current)) {
            return 0;
        }
        level.setBlock(pos, current.setValue(BlockStateProperties.WATERLOGGED, false), Block.UPDATE_ALL);
        FluidCompactLog.trace("Cleaned orphan pipe water block={} pos={}", BuiltInRegistries.BLOCK.getKey(current.getBlock()), pos.toShortString());
        return 1;
    }
}
