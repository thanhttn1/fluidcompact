package me.thanhtnt2.fluidcompact.compat.pneumaticcraft.mixin;

import me.thanhtnt2.fluidcompact.compat.GuardedCompatMixinPlugin;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Applies the pickup hook only if PneumaticCraft's shared pickup routine still reads a config flag the hook can answer. */
public final class
        PneumaticCraftMixinPlugin extends GuardedCompatMixinPlugin {

    private static final String CONFIG_VALUE_INTERNAL_NAME = "net/neoforged/neoforge/common/ModConfigSpec$BooleanValue";

    public PneumaticCraftMixinPlugin() {
        super("pneumaticcraft",
                "me.desht.pneumaticcraft.common.util.FluidUtils",
                PneumaticCraftMixinPlugin::pickupReadsConfigFlag);
    }

    private static boolean pickupReadsConfigFlag(ClassNode target) {
        for (MethodNode method : target.methods) {
            if ("tryPickupFluid".equals(method.name) && readsConfigFlag(method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean readsConfigFlag(MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call
                    && "get".equals(call.name)
                    && CONFIG_VALUE_INTERNAL_NAME.equals(call.owner)) {
                return true;
            }
        }
        return false;
    }
}
