package me.thanhtnt2.fluidcompact.compat.fluidlogged;

import java.util.List;

import de.leximon.fluidlogged.api.FluidloggedRegistries;
import de.leximon.fluidlogged.config.Addon;
import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Talks to the Fluidlogged API. Only ever loaded after confirming Fluidlogged is installed.
 *
 * <p>Fluidlogged evaluates registered addons when a server starts or data packs reload, so registering
 * during common setup is early enough. Players can still switch the addon off in Fluidlogged's own config.
 */
final class FluidloggedAddonRegistrar {

    private static final ResourceLocation ADDON_ID =
            ResourceLocation.fromNamespaceAndPath(FluidCompactApi.MOD_ID, "passable_pipes");

    private static final int LOGGED_SAMPLE_SIZE = 60;

    private FluidloggedAddonRegistrar() {
    }

    static void register() {
        List<Block> blocks = PassableBlockCollector.collectBlocks();
        List<TagKey<Block>> tags = PassableBlockCollector.collectTags();

        Addon.Builder addon = Addon.builder().enabledByDefault(true);
        if (FluidCompactCompatConfig.FLUIDLOGGED_FLUIDLOGGABLE.get()) {
            addon.fluidloggableBlocks(blocks).fluidloggableBlockTags(tags);
        }
        if (FluidCompactCompatConfig.FLUIDLOGGED_PERMEABLE.get()) {
            addPermeability(addon, blocks, tags);
        }

        FluidloggedRegistries.register(FluidloggedRegistries.ADDONS, ADDON_ID, addon.build());
        logRegistration(blocks, tags);
    }

    private static void addPermeability(Addon.Builder addon, List<Block> blocks, List<TagKey<Block>> tags) {
        if (FluidCompactCompatConfig.FLUIDLOGGED_SHAPE_INDEPENDENT.get()) {
            addon.shapeIndependentFluidPermeableBlocks(blocks).shapeIndependentFluidPermeableBlockTags(tags);
            return;
        }
        addon.fluidPermeableBlocks(blocks).fluidPermeableBlockTags(tags);
    }

    private static void logRegistration(List<Block> blocks, List<TagKey<Block>> tags) {
        FluidCompactLog.info("[fluidlogged] registered addon {} with {} block(s) and {} tag(s)", ADDON_ID, blocks.size(), tags.size());
        if (blocks.isEmpty()) {
            FluidCompactLog.warn("[fluidlogged] no block matched the configured class names; pipes will keep blocking fluid");
            return;
        }
        StringBuilder sample = new StringBuilder();
        for (int index = 0; index < Math.min(blocks.size(), LOGGED_SAMPLE_SIZE); index++) {
            sample.append(BuiltInRegistries.BLOCK.getKey(blocks.get(index))).append(' ');
        }
        FluidCompactLog.info("[fluidlogged] covered blocks (first {}): {}", LOGGED_SAMPLE_SIZE, sample.toString().trim());
    }
}
