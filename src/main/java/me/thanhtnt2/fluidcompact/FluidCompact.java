package me.thanhtnt2.fluidcompact;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import me.thanhtnt2.fluidcompact.compat.CompatBootstrap;
import me.thanhtnt2.fluidcompact.config.FluidCompactCompatConfig;
import me.thanhtnt2.fluidcompact.config.FluidCompactConfig;
import me.thanhtnt2.fluidcompact.core.FluidCompactAttachments;
import me.thanhtnt2.fluidcompact.core.FluidCompactBootstrap;
import me.thanhtnt2.fluidcompact.logging.FluidCompactLog;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/** Common entry point. Contains no client code, so it is safe on dedicated servers. */
@Mod(FluidCompact.MODID)
public final class FluidCompact {

    public static final String MODID = "fluidcompact";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FluidCompact(IEventBus modEventBus, ModContainer modContainer) {
        FluidCompactAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, FluidCompactConfig.SPEC);
        modEventBus.addListener(this::onCommonSetup);
        registerCompatLayer(modEventBus, modContainer);
    }

    // Compat modules live in their own packages and config file; this is their single entry point
    private static void registerCompatLayer(IEventBus modEventBus, ModContainer modContainer) {
        FluidCompactLog.initialize();
        modContainer.registerConfig(ModConfig.Type.COMMON, FluidCompactCompatConfig.SPEC, "fluidcompact-compat.toml");
        CompatBootstrap.registerBuiltInModules();
        // Added after the core listener on purpose: its setup work is queued first, so the API is bound first
        modEventBus.addListener(CompatBootstrap::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // enqueueWork so the API is bound on the main thread before any other mod's setup work runs
        event.enqueueWork(FluidCompactBootstrap::initialize);
    }
}
