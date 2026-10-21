package me.thanhtnt2.fluidcompact.compat.pneumaticcraft.mixin;

import me.thanhtnt2.fluidcompact.compat.GuardedCompatMixinPlugin;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Applies the pickup hook only if PneumaticCraft's shared pickup routine still reads exactly one config flag.
 * The hook redirects every such read, so a second flag appearing in a future PneumaticCraft would be answered
 * wrongly; in that case the hook is switched off (with a warning) instead.
 */
public final class PneumaticCraftMixinPlugin extends GuardedCompatMixinPlugin {

    private static final String CONFIG_VALUE_INTERNAL_NAME = "net/neoforged/neoforge/common/ModConfigSpec$BooleanValue";

    public PneumaticCraftMixinPlugin() {
        super("pneumaticcraft",
                "me.desht.pneumaticcraft.common.util.FluidUtils",
                PneumaticCraftMixinPlugin::pickupReadsConfigFlag);
    }

    private static boolean pickupReadsConfigFlag(ClassNode target) {
        for (MethodNode method : target.methods) {
            if ("tryPickupFluid".equals(method.name) && countConfigFlagReads(method) == 1) {
                return true;
            }
        }
        return false;
    }

    private static int countConfigFlagReads(MethodNode method) {
        int reads = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call
                    && "get".equals(call.name)
                    && CONFIG_VALUE_INTERNAL_NAME.equals(call.owner)) {
                reads++;
            }
        }
        return reads;
    }
}
