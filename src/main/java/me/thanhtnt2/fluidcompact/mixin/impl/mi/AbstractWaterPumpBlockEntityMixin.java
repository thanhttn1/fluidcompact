package me.thanhtnt2.fluidcompact.mixin.impl.mi;

import me.thanhtnt2.fluidcompact.integration.mi.MiPumpBridge;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * The only place FluidCompact touches Modern Industrialization code.
 *
 * <p>The target is referenced by name so FluidCompact compiles and loads without MI present;
 * {@code FluidCompactMixinPlugin} decides at boot whether this mixin is applied at all.
 */
@Mixin(targets = "aztech.modern_industrialization.machines.blockentities.AbstractWaterPumpBlockEntity", remap = false)
public class AbstractWaterPumpBlockEntityMixin {

    // Hooking the amount handed to the tank (instead of the source counter) lets us charge the world
    // for exactly what MI really adds, including cycles cut short by a nearly full tank
    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Laztech/modern_industrialization/inventory/ConfigurableFluidStack;increment(J)V",
                    remap = false),
            index = 0,
            remap = false)
    private long fluidcompact$limitPumpedAmount(long requestedMillibuckets) {
        return MiPumpBridge.limitPumpedMillibuckets((BlockEntity) (Object) this, requestedMillibuckets);
    }
}
