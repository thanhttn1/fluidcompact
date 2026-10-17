package me.thanhtnt2.fluidcompact.compat.modernindustrialization.mixin;

import me.thanhtnt2.fluidcompact.compat.GuardedCompatMixinPlugin;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Applies the pump hook only when Modern Industrialization is installed and its pump still adds water
 * to its tank from {@code tick()}. A missing or changed MI disables just this hook with a warning.
 */
public final class ModernIndustrializationMixinPlugin extends GuardedCompatMixinPlugin {

    private static final String TANK_INTERNAL_NAME = "aztech/modern_industrialization/inventory/ConfigurableFluidStack";

    public ModernIndustrializationMixinPlugin() {
        super("modern_industrialization",
                "aztech.modern_industrialization.machines.blockentities.AbstractWaterPumpBlockEntity",
                ModernIndustrializationMixinPlugin::tickAddsWaterToTank);
    }

    private static boolean tickAddsWaterToTank(ClassNode target) {
        for (MethodNode method : target.methods) {
            if ("tick".equals(method.name) && "()V".equals(method.desc) && callsTankIncrement(method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean callsTankIncrement(MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call
                    && "increment".equals(call.name)
                    && TANK_INTERNAL_NAME.equals(call.owner)) {
                return true;
            }
        }
        return false;
    }
}
