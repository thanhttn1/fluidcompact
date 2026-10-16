package me.thanhtnt2.fluidcompact.integration.jei;

import me.thanhtnt2.fluidcompact.api.FluidCompactApi;
import me.thanhtnt2.fluidcompact.api.FluidCompactCompatTags;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Info pages for the pumps of the compat modules (Mekanism family).
 * A second plugin instead of extending the first keeps the original JEI integration untouched.
 */
@JeiPlugin
public final class FluidCompactCompatJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID =
            ResourceLocation.fromNamespaceAndPath(FluidCompactApi.MOD_ID, "jei_compat_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        BuiltInRegistries.ITEM.getTag(FluidCompactCompatTags.COMPAT_FINITE_PUMP_ITEMS).ifPresent(pumps ->
                pumps.forEach(holder -> registration.addIngredientInfo(
                        holder.value(), Component.translatable("jei.fluidcompact.compat_finite_pump"))));
    }
}
