package me.thanhtnt2.fluidcompact.compat.fluidlogged;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import me.thanhtnt2.fluidcompact.api.FluidCompactCompatTags;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Finds the pipe-like blocks fluids should pass through.
 * Blocks are matched by class name so a whole pipe family is covered without hard-coding registry ids
 * and without linking against the pipe mods.
 */
final class PassableBlockCollector {

    private PassableBlockCollector() {
    }

    /** Must run after all registries are filled, which is true from common setup onwards. */
    static List<Block> collectBlocks() {
        Set<String> classNames = new HashSet<>(FluidCompactCompatConfig.FLUIDLOGGED_BLOCK_CLASSES.get());
        Set<Block> matched = new LinkedHashSet<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (isInstanceOfAnyNamedClass(block.getClass(), classNames)) {
                matched.add(block);
            }
        }
        for (String entry : FluidCompactCompatConfig.FLUIDLOGGED_EXTRA_BLOCKS.get()) {
            addBlockEntry(entry, matched);
        }
        return List.copyOf(matched);
    }

    static List<TagKey<Block>> collectTags() {
        List<TagKey<Block>> tags = new ArrayList<>();
        // Always included so datapacks can extend the list without touching the config
        tags.add(FluidCompactCompatTags.FLUID_PASSABLE_BLOCKS);
        for (String entry : FluidCompactCompatConfig.FLUIDLOGGED_EXTRA_BLOCKS.get()) {
            addTagEntry(entry, tags);
        }
        return tags;
    }

    private static boolean isInstanceOfAnyNamedClass(Class<?> type, Set<String> classNames) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (classNames.contains(current.getName())) {
                return true;
            }
        }
        return false;
    }

    private static void addBlockEntry(String entry, Set<Block> target) {
        if (entry.startsWith("#")) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(entry.trim());
        if (id == null) {
            return;
        }
        BuiltInRegistries.BLOCK.getOptional(id).ifPresent(target::add);
    }

    private static void addTagEntry(String entry, List<TagKey<Block>> target) {
        if (!entry.startsWith("#")) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(entry.substring(1).trim());
        if (id != null) {
            target.add(TagKey.create(Registries.BLOCK, id));
        }
    }
}
