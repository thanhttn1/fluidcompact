package me.thanhtnt2.fluidcompact.config;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings of the compat modules, stored in {@code config/fluidcompact-compat.toml}.
 *
 * <p>It lives in its own file on purpose: the original {@link FluidCompactConfig} keys
 * (finite water, drain percent, ...) are never rewritten when a new mod gets supported.
 */
public final class FluidCompactCompatConfig {

    // Default class names (not block ids) so every block of a pipe family is covered, whatever its id
    private static final List<String> DEFAULT_PASSABLE_BLOCK_CLASSES = List.of(
            "aztech.modern_industrialization.pipes.impl.PipeBlock",
            "dev.technici4n.moderndynamics.pipe.PipeBlock",
            "mcjty.xnet.modules.cables.blocks.GenericCableBlock",
            "com.refinedmods.refinedstorage.common.networking.CableBlock",
            "com.refinedmods.refinedstorage.common.support.AbstractDirectionalCableBlock",
            "mekanism.common.block.transmitter.BlockTransmitter");

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue COMPAT_ENABLED;

    public static final ModConfigSpec.BooleanValue LOG_FILE_ENABLED;
    public static final ModConfigSpec.BooleanValue LOG_VERBOSE;
    public static final ModConfigSpec.BooleanValue LOG_DEBUG_MODE;
    public static final ModConfigSpec.IntValue LOG_VERBOSE_THROTTLE_MS;

    public static final ModConfigSpec.BooleanValue ELECTRIC_PUMP_ENABLED;
    public static final ModConfigSpec.BooleanValue ELECTRIC_PUMP_WATER;
    public static final ModConfigSpec.BooleanValue ELECTRIC_PUMP_LAVA;
    public static final ModConfigSpec.BooleanValue ELECTRIC_PUMP_TAGGED_FLUIDS;

    public static final ModConfigSpec.BooleanValue SEAWATER_PUMP_ENABLED;
    public static final ModConfigSpec.IntValue SEAWATER_DRAIN_PERCENT;
    public static final ModConfigSpec.IntValue SEAWATER_MIN_TICKS_BETWEEN_INTAKES;
    public static final ModConfigSpec.EnumValue<ShortfallBehavior> SEAWATER_SHORTFALL;

    public static final ModConfigSpec.BooleanValue PNC_PICKUP_ENABLED;

    public static final ModConfigSpec.BooleanValue FLUIDLOGGED_ENABLED;
    public static final ModConfigSpec.BooleanValue FLUIDLOGGED_ACCEPT_DUPLICATION_RISK;
    public static final ModConfigSpec.BooleanValue FLUIDLOGGED_FLUIDLOGGABLE;
    public static final ModConfigSpec.BooleanValue FLUIDLOGGED_PERMEABLE;
    public static final ModConfigSpec.BooleanValue FLUIDLOGGED_SHAPE_INDEPENDENT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FLUIDLOGGED_BLOCK_CLASSES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FLUIDLOGGED_EXTRA_BLOCKS;

    public static final ModConfigSpec.BooleanValue FF_PIPES_ENABLED;
    public static final ModConfigSpec.BooleanValue FF_PIPES_NON_DISPLACERS;
    public static final ModConfigSpec.EnumValue<WaterlogFlowOverride> FF_PIPES_WATERLOG_MODE;
    public static final ModConfigSpec.BooleanValue FF_PIPES_ALLOW_EXPERIMENTAL_MODES;
    public static final ModConfigSpec.BooleanValue FF_PIPES_CLEAN_ORPHAN_WATER;
    public static final ModConfigSpec.IntValue FF_PIPES_CLEANUP_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue FF_PIPES_CLEANUP_CHUNK_RADIUS;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("general");
        COMPAT_ENABLED = BUILDER
                .comment("Master switch for every compat module. Turn it off to run only the core Modern Industrialization hook.")
                .define("compatEnabled", true);
        BUILDER.pop();

        BUILDER.push("logging");
        LOG_FILE_ENABLED = BUILDER
                .comment("Write logs/fluidcompact.log in the game folder (the previous run is kept as fluidcompact-previous.log).")
                .define("fileEnabled", true);
        LOG_VERBOSE = BUILDER
                .comment("Also log every fluid intake. Useful for bug reports, noisy otherwise.")
                .define("verbose", false);
        LOG_DEBUG_MODE = BUILDER
                .comment("DEBUG MODE: log EVERYTHING into logs/fluidcompact.log: full config dump, every loaded mod,",
                        "every fluid intake (no throttling) and every block update of pipes. Files can get very large.",
                        "Pipe update tracing only starts after a game restart.")
                .define("debugMode", false);
        LOG_VERBOSE_THROTTLE_MS = BUILDER
                .comment("Minimum milliseconds between two verbose lines about the same machine.")
                .defineInRange("verboseThrottleMillis", 1000, 0, 600000);
        BUILDER.pop();

        BUILDER.push("mekanism_electric_pump");
        ELECTRIC_PUMP_ENABLED = BUILDER
                .comment("Force the Mekanism Electric Pump to really remove source blocks instead of duplicating them.")
                .define("enabled", true);
        ELECTRIC_PUMP_WATER = BUILDER
                .comment("Apply to water (still requires fluids.finiteWater in fluidcompact-common.toml).")
                .define("water", true);
        ELECTRIC_PUMP_LAVA = BUILDER
                .comment("Apply to lava (still requires fluids.finiteLava in fluidcompact-common.toml).")
                .define("lava", true);
        ELECTRIC_PUMP_TAGGED_FLUIDS = BUILDER
                .comment("Apply to fluids in the tag fluidcompact:managed_fluids.")
                .define("taggedFluids", true);
        BUILDER.pop();

        BUILDER.push("mekanism_elements_seawater_pump");
        SEAWATER_PUMP_ENABLED = BUILDER
                .comment("Make the Mekanism Elements Seawater Pump drain the water source below it.")
                .define("enabled", true);
        SEAWATER_DRAIN_PERCENT = BUILDER
                .comment("World water consumed per unit of seawater made. 100 = one for one, 50 = twice as efficient.")
                .defineInRange("drainPercent", 100, 1, 1000);
        SEAWATER_MIN_TICKS_BETWEEN_INTAKES = BUILDER
                .comment("Minimum ticks between two drains of the same pump. 0 = no extra limit.")
                .defineInRange("minTicksBetweenIntakes", 0, 0, 72000);
        SEAWATER_SHORTFALL = BUILDER
                .comment("When the world cannot pay a whole cycle: SCALE_DOWN produces less, STOP produces nothing.")
                .defineEnum("shortfallBehavior", ShortfallBehavior.SCALE_DOWN);
        BUILDER.pop();

        BUILDER.push("pneumaticcraft_pickup");
        PNC_PICKUP_ENABLED = BUILDER
                .comment("PneumaticCraft's Liquid Hopper and Gas Lift leave a water source block in place when it has two water",
                        "neighbours, which makes their water infinite. When this is true and water is finite, they remove it like any other source.",
                        "Drones (Liquid Import) already remove every source block, so they need no hook.")
                .define("enabled", true);
        BUILDER.pop();

        BUILDER.push("fluidlogged_pipes");
        FLUIDLOGGED_ENABLED = BUILDER
                .comment("Register a Fluidlogged addon so fluids can enter and cross pipe blocks. Needs a restart.")
                .define("enabled", true);
        FLUIDLOGGED_ACCEPT_DUPLICATION_RISK = BUILDER
                .comment("DANGEROUS, keep false. Fluidlogged keeps its own hidden copy of the fluid inside a pipe, next to the",
                        "block's waterlogged flag. Together with Flowing Fluids this was seen to duplicate water.",
                        "The addon is therefore only registered when this is true.")
                .define("acceptDuplicationRisk", false);
        FLUIDLOGGED_FLUIDLOGGABLE = BUILDER
                .comment("Pipes can hold any fluid, so flowing fluid no longer destroys them.")
                .define("makeFluidloggable", true);
        FLUIDLOGGED_PERMEABLE = BUILDER
                .comment("Fluids flow through pipes instead of treating them as solid walls.")
                .define("makePermeable", true);
        FLUIDLOGGED_SHAPE_INDEPENDENT = BUILDER
                .comment("Let fluid pass whatever the pipe shape is. When false, Fluidlogged's shape-based permeability is used.")
                .define("shapeIndependent", true);
        FLUIDLOGGED_BLOCK_CLASSES = BUILDER
                .comment("Fully qualified Java class names. Every block that is an instance of one of them is covered.")
                .defineListAllowEmpty("blockClasses", DEFAULT_PASSABLE_BLOCK_CLASSES, () -> "",
                        FluidCompactCompatConfig::isNonBlankString);
        FLUIDLOGGED_EXTRA_BLOCKS = BUILDER
                .comment("Extra entries: block ids like \"modid:block\" or block tags like \"#modid:tag\".",
                        "Datapacks can also add blocks to the tag fluidcompact:fluid_passable_blocks.")
                .defineListAllowEmpty("extraBlocks", List.of(), () -> "",
                        FluidCompactCompatConfig::isNonBlankString);
        BUILDER.pop();

        BUILDER.push("flowing_fluids_pipes");
        FF_PIPES_ENABLED = BUILDER
                .comment("Teach Flowing Fluids that pipes are special blocks. Uses the same pipe list as fluidlogged_pipes.")
                .define("enabled", true);
        FF_PIPES_NON_DISPLACERS = BUILDER
                .comment("Register pipes through the Flowing Fluids API as blocks that never push fluid out of their cell.")
                .define("registerAsNonDisplacers", true);
        FF_PIPES_WATERLOG_MODE = BUILDER
                .comment("How Flowing Fluids treats waterlogged blocks (pipes). GLOBAL setting: it also affects slabs, stairs, etc.",
                        "Applied in memory when a world starts; nothing is written to Flowing Fluids' own config file.",
                        "  IGNORE (default, safe): pipes neither take nor give water through Flowing Fluids, so they cannot duplicate it.",
                        "  KEEP_FLOWING_FLUIDS_SETTING: change nothing (Flowing Fluids' own default).",
                        "  Any other value is EXPERIMENTAL: pipes then take part in the fluid simulation and water duplication was",
                        "  observed with it. It is ignored (IGNORE is used) unless allowExperimentalWaterlogModes is true.")
                .defineEnum("waterlogFlowMode", WaterlogFlowOverride.IGNORE);
        FF_PIPES_ALLOW_EXPERIMENTAL_MODES = BUILDER
                .comment("DANGEROUS: lets waterlogFlowMode use modes other than IGNORE / KEEP. Can duplicate water. Only for testing.")
                .define("allowExperimentalWaterlogModes", false);
        FF_PIPES_CLEAN_ORPHAN_WATER = BUILDER
                .comment("Remove the water left inside a waterlogged pipe once none of its non-pipe neighbours holds water.",
                        "Stops water cubes from hanging in the air around pipes and removes stuck water instead of copying it.")
                .define("cleanOrphanPipeWater", true);
        FF_PIPES_CLEANUP_INTERVAL_TICKS = BUILDER
                .comment("Ticks between two cleanup passes. Higher = less work, slower cleanup.")
                .defineInRange("cleanupIntervalTicks", 40, 5, 1200);
        FF_PIPES_CLEANUP_CHUNK_RADIUS = BUILDER
                .comment("Only pipes in loaded chunks within this many chunks of a player are cleaned. Lower = less work.")
                .defineInRange("cleanupChunkRadius", 4, 1, 16);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private FluidCompactCompatConfig() {
    }

    private static boolean isNonBlankString(Object value) {
        return value instanceof String text && !text.isBlank();
    }
}
