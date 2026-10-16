package me.thanhtnt2.fluidcompact.client.yacl;

import java.util.ArrayList;
import java.util.List;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import me.thanhtnt2.fluidcompact.api.compat.CompatModule;
import me.thanhtnt2.fluidcompact.api.compat.CompatModuleRegistry;
import me.thanhtnt2.fluidcompact.api.compat.CompatStatus;
import me.thanhtnt2.fluidcompact.config.BackendPreference;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import me.thanhtnt2.fluidcompact.config.FluidCompactConfig;
import me.thanhtnt2.fluidcompact.config.ShortfallBehavior;
import me.thanhtnt2.fluidcompact.config.WaterlogFlowOverride;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Config screen built with YACL. Client only, and only ever loaded when YACL is installed.
 *
 * <p>Every option is bound straight to the existing {@link ModConfigSpec} values, so the screen and the
 * toml files can never drift apart. The original core keys are only read and written, never redefined.
 */
public final class YaclConfigScreenFactory {

    private static final String KEY_PREFIX = "fluidcompact.option.";

    private YaclConfigScreenFactory() {
    }

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("fluidcompact.configuration.title"))
                .category(buildCoreCategory())
                .category(buildModCompactListCategory())
                .category(buildLoggingCategory())
                .save(YaclConfigScreenFactory::saveAll)
                .build()
                .generateScreen(parent);
    }

    // ---------------------------------------------------------------- categories

    private static ConfigCategory buildCoreCategory() {
        OptionGroup general = OptionGroup.createBuilder()
                .name(Component.translatable("fluidcompact.group.core"))
                .option(LabelOption.create(Component.translatable("fluidcompact.notice.unofficial").withStyle(ChatFormatting.GRAY)))
                .option(toggle("enableFiniteFluids", FluidCompactConfig.ENABLE_FINITE_FLUIDS, true))
                .option(choice("backendPreference", FluidCompactConfig.BACKEND_PREFERENCE, BackendPreference.class, true))
                .option(toggle("finiteWater", FluidCompactConfig.FINITE_WATER, true))
                .option(toggle("finiteLava", FluidCompactConfig.FINITE_LAVA, true))
                .option(toggle("finiteTaggedFluids", FluidCompactConfig.FINITE_TAGGED_FLUIDS, true))
                .build();
        return ConfigCategory.createBuilder()
                .name(Component.translatable("fluidcompact.tab.core"))
                .group(general)
                .build();
    }

    private static ConfigCategory buildModCompactListCategory() {
        ConfigCategory.Builder category = ConfigCategory.createBuilder()
                .name(Component.translatable("fluidcompact.tab.mod_compact_list"))
                .tooltip(Component.translatable("fluidcompact.tab.mod_compact_list.tooltip"))
                .group(buildOverviewGroup())
                .group(buildMasterGroup())
                .group(buildModernIndustrializationGroup())
                .group(buildMekanismGroup())
                .group(buildMekanismElementsGroup())
                .group(buildFluidloggedGroup())
                .group(buildFlowingFluidsPipesGroup());
        category.group(textList("fluidloggedBlockClasses", FluidCompactCompatConfig.FLUIDLOGGED_BLOCK_CLASSES, isLoaded("fluidlogged")));
        category.group(textList("fluidloggedExtraBlocks", FluidCompactCompatConfig.FLUIDLOGGED_EXTRA_BLOCKS, isLoaded("fluidlogged")));
        return category.build();
    }

    private static ConfigCategory buildLoggingCategory() {
        OptionGroup.Builder group = OptionGroup.createBuilder()
                .name(Component.translatable("fluidcompact.group.logging"))
                .option(toggle("logFileEnabled", FluidCompactCompatConfig.LOG_FILE_ENABLED, true))
                .option(toggle("logDebug", FluidCompactCompatConfig.LOG_DEBUG_MODE, true))
                .option(toggle("logVerbose", FluidCompactCompatConfig.LOG_VERBOSE, true))
                .option(slider("logThrottle", FluidCompactCompatConfig.LOG_VERBOSE_THROTTLE_MS, 0, 10000, 100, true));
        if (FluidCompactLog.logFilePath() != null) {
            group.option(LabelOption.create(Component.literal(FluidCompactLog.logFilePath().toString()).withStyle(ChatFormatting.GRAY)));
        }
        return ConfigCategory.createBuilder()
                .name(Component.translatable("fluidcompact.tab.logging"))
                .group(group.build())
                .build();
    }

    // ---------------------------------------------------------------- groups of the "Mod Compact List"

    private static OptionGroup buildOverviewGroup() {
        OptionGroup.Builder group = OptionGroup.createBuilder()
                .name(Component.translatable("fluidcompact.group.overview"))
                .description(OptionDescription.of(Component.translatable("fluidcompact.group.overview.desc")));
        for (CompatModule module : CompatModuleRegistry.snapshot()) {
            group.option(LabelOption.create(statusLine(module)));
        }
        return group.build();
    }

    private static OptionGroup buildMasterGroup() {
        return OptionGroup.createBuilder()
                .name(Component.translatable("fluidcompact.group.general"))
                .option(toggle("compatEnabled", FluidCompactCompatConfig.COMPAT_ENABLED, true))
                .build();
    }

    private static OptionGroup buildModernIndustrializationGroup() {
        boolean installed = isLoaded("modern_industrialization");
        return OptionGroup.createBuilder()
                .name(Component.translatable("fluidcompact.group.mi"))
                .description(OptionDescription.of(Component.translatable("fluidcompact.group.mi.desc")))
                .option(slider("miDrainPercent", FluidCompactConfig.DRAIN_PERCENT, 1, 1000, 5, installed))
                .option(slider("miMinTicks", FluidCompactConfig.MIN_TICKS_BETWEEN_INTAKES, 0, 6000, 20, installed))
                .build();
    }

    private static OptionGroup buildMekanismGroup() {
        boolean installed = isLoaded("mekanism");
        return OptionGroup.createBuilder()
                .name(Component.translatable("fluidcompact.group.mekanism"))
                .description(OptionDescription.of(Component.translatable("fluidcompact.group.mekanism.desc")))
                .option(toggle("electricPumpEnabled", FluidCompactCompatConfig.ELECTRIC_PUMP_ENABLED, installed))
                .option(toggle("electricPumpWater", FluidCompactCompatConfig.ELECTRIC_PUMP_WATER, installed))
                .option(toggle("electricPumpLava", FluidCompactCompatConfig.ELECTRIC_PUMP_LAVA, installed))
                .option(toggle("electricPumpTagged", FluidCompactCompatConfig.ELECTRIC_PUMP_TAGGED_FLUIDS, installed))
                .build();
    }

    private static OptionGroup buildMekanismElementsGroup() {
        boolean installed = isLoaded("mekanismelements");
        return OptionGroup.createBuilder()
                .name(Component.translatable("fluidcompact.group.mekanism_elements"))
                .description(OptionDescription.of(Component.translatable("fluidcompact.group.mekanism_elements.desc")))
                .option(toggle("seawaterEnabled", FluidCompactCompatConfig.SEAWATER_PUMP_ENABLED, installed))
                .option(slider("seawaterDrainPercent", FluidCompactCompatConfig.SEAWATER_DRAIN_PERCENT, 1, 1000, 5, installed))
                .option(slider("seawaterMinTicks", FluidCompactCompatConfig.SEAWATER_MIN_TICKS_BETWEEN_INTAKES, 0, 6000, 20, installed))
                .option(choice("seawaterShortfall", FluidCompactCompatConfig.SEAWATER_SHORTFALL, ShortfallBehavior.class, installed))
                .build();
    }

    private static OptionGroup buildFluidloggedGroup() {
        boolean installed = isLoaded("fluidlogged");
        return OptionGroup.createBuilder()
                .name(Component.translatable("fluidcompact.group.fluidlogged"))
                .description(OptionDescription.of(Component.translatable("fluidcompact.group.fluidlogged.desc")))
                .option(toggle("fluidloggedEnabled", FluidCompactCompatConfig.FLUIDLOGGED_ENABLED, installed))
                .option(toggle("fluidloggedFluidloggable", FluidCompactCompatConfig.FLUIDLOGGED_FLUIDLOGGABLE, installed))
                .option(toggle("fluidloggedPermeable", FluidCompactCompatConfig.FLUIDLOGGED_PERMEABLE, installed))
                .option(toggle("fluidloggedShapeIndependent", FluidCompactCompatConfig.FLUIDLOGGED_SHAPE_INDEPENDENT, installed))
                .build();
    }

    private static OptionGroup buildFlowingFluidsPipesGroup() {
        boolean installed = isLoaded("flowing_fluids");
        return OptionGroup.createBuilder()
                .name(Component.translatable("fluidcompact.group.flowing_fluids_pipes"))
                .description(OptionDescription.of(Component.translatable("fluidcompact.group.flowing_fluids_pipes.desc")))
                .option(toggle("ffPipesEnabled", FluidCompactCompatConfig.FF_PIPES_ENABLED, installed))
                .option(toggle("ffPipesNonDisplacers", FluidCompactCompatConfig.FF_PIPES_NON_DISPLACERS, installed))
                .option(choice("ffPipesWaterlogMode", FluidCompactCompatConfig.FF_PIPES_WATERLOG_MODE, WaterlogFlowOverride.class, installed))
                .build();
    }

    // ---------------------------------------------------------------- option builders

    private static Option<Boolean> toggle(String key, ModConfigSpec.BooleanValue value, boolean available) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable(KEY_PREFIX + key))
                .description(OptionDescription.of(Component.translatable(KEY_PREFIX + key + ".desc")))
                .binding(value.getDefault(), value::get, value::set)
                .controller(TickBoxControllerBuilder::create)
                .available(available)
                .build();
    }

    private static Option<Integer> slider(String key, ModConfigSpec.IntValue value, int min, int max, int step, boolean available) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable(KEY_PREFIX + key))
                .description(OptionDescription.of(Component.translatable(KEY_PREFIX + key + ".desc")))
                .binding(value.getDefault(), value::get, value::set)
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(min, max).step(step))
                .available(available)
                .build();
    }

    private static <E extends Enum<E>> Option<E> choice(
            String key, ModConfigSpec.EnumValue<E> value, Class<E> type, boolean available) {
        return Option.<E>createBuilder()
                .name(Component.translatable(KEY_PREFIX + key))
                .description(OptionDescription.of(Component.translatable(KEY_PREFIX + key + ".desc")))
                .binding(value.getDefault(), value::get, value::set)
                .controller(option -> EnumControllerBuilder.create(option).enumClass(type))
                .available(available)
                .build();
    }

    private static ListOption<String> textList(
            String key, ModConfigSpec.ConfigValue<List<? extends String>> value, boolean available) {
        return ListOption.<String>createBuilder()
                .name(Component.translatable(KEY_PREFIX + key))
                .description(OptionDescription.of(Component.translatable(KEY_PREFIX + key + ".desc")))
                .binding(new ArrayList<String>(value.getDefault()),
                        () -> new ArrayList<String>(value.get()),
                        edited -> value.set(new ArrayList<>(edited)))
                .controller(StringControllerBuilder::create)
                .initial("")
                .available(available)
                .build();
    }

    // ---------------------------------------------------------------- helpers

    private static Component statusLine(CompatModule module) {
        CompatStatus status = CompatModuleRegistry.statusOf(module.id());
        ChatFormatting color = switch (status) {
            case ACTIVE -> ChatFormatting.GREEN;
            case FAILED -> ChatFormatting.RED;
            case DISABLED, PENDING -> ChatFormatting.YELLOW;
            case MISSING_MOD -> ChatFormatting.GRAY;
        };
        Component label = Component.translatable("fluidcompact.status." + status.name().toLowerCase(java.util.Locale.ROOT));
        return Component.literal(module.displayName() + ": ").append(label.copy().withStyle(color));
    }

    private static boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    private static void saveAll() {
        try {
            FluidCompactConfig.SPEC.save();
            FluidCompactCompatConfig.SPEC.save();
            FluidCompactLog.info("Config saved from the YACL screen");
        } catch (RuntimeException exception) {
            FluidCompactLog.error("Could not save the FluidCompact config", exception);
        }
    }
}
