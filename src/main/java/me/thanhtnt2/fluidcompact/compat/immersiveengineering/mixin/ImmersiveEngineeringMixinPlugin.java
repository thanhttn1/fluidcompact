package me.thanhtnt2.fluidcompact.compat.immersiveengineering.mixin;

import me.thanhtnt2.fluidcompact.compat.GuardedCompatMixinPlugin;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.service.MixinService;

/**
 * Decides, for each of the two pump mixins on its own, whether Immersive Engineering still looks the way the hook
 * was written for. A hook whose target changed stays off and says so in the log; the other one is not affected.
 *
 * <ul>
 *   <li>Endless-source hook: {@code canConvertToSource} must be asked exactly once in
 *       {@code gatherInfiniteFluidFromWorld} and twice in {@code checkAreaTick}.</li>
 *   <li>Cobblestone hook: {@code tickServer} must still make exactly two {@code Utils.drainFluidBlock} calls and one
 *       {@code Level.setBlockAndUpdate} call, still read {@code Blocks.COBBLESTONE}, and the pump must still have its
 *       {@code closedList} work list and {@code placeCobble} flag.</li>
 * </ul>
 */
public final class ImmersiveEngineeringMixinPlugin extends GuardedCompatMixinPlugin {

    private static final String PUMP_CLASS = "blusunrize.immersiveengineering.common.blocks.metal.FluidPumpBlockEntity";
    private static final String GATHER_DESCRIPTOR = "(Lnet/minecraft/core/Direction;)V";
    private static final String FLUID_STATE_INTERNAL_NAME = "net/minecraft/world/level/material/FluidState";
    private static final String CONVERT_DESCRIPTOR = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z";

    private static final Logger LOGGER = LoggerFactory.getLogger("FluidCompact/Mixin");
    private static final String COBBLE_MIXIN = "FluidPumpCobbleMixin";
    private static final String UTILS_INTERNAL_NAME = "blusunrize/immersiveengineering/common/util/Utils";
    private static final String DRAIN_DESCRIPTOR = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;"
            + "Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;";
    private static final String LEVEL_INTERNAL_NAME = "net/minecraft/world/level/Level";
    private static final String SET_BLOCK_DESCRIPTOR = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z";
    private static final String BLOCKS_INTERNAL_NAME = "net/minecraft/world/level/block/Blocks";

    public ImmersiveEngineeringMixinPlugin() {
        super("immersiveengineering", PUMP_CLASS, ImmersiveEngineeringMixinPlugin::pumpAsksWhereExpected);
    }

    @Override
    public boolean shouldApplyMixin(String targetClass, String mixinClassName) {
        boolean pumpKnown = super.shouldApplyMixin(targetClass, mixinClassName);
        if (!pumpKnown || !mixinClassName.endsWith(COBBLE_MIXIN)) {
            return pumpKnown;
        }
        boolean fits = cobbleHookFits();
        if (!fits) {
            LOGGER.info("[immersiveengineering] the Fluid Pump's cobblestone handling changed, the no-cobblestone hook stays off. "
                    + "Finite fluids are still pumped as source blocks, but get cobblestone like before.");
        }
        return fits;
    }

    private static boolean cobbleHookFits() {
        try {
            ClassNode target = MixinService.getService().getBytecodeProvider().getClassNode(PUMP_CLASS);
            boolean hasWorkList = target.fields.stream()
                    .anyMatch(field -> "closedList".equals(field.name) && "Ljava/util/List;".equals(field.desc));
            boolean hasFlag = target.fields.stream()
                    .anyMatch(field -> "placeCobble".equals(field.name) && "Z".equals(field.desc));
            for (MethodNode method : target.methods) {
                if ("tickServer".equals(method.name) && "()V".equals(method.desc)) {
                    return hasWorkList && hasFlag && tickShapeMatches(method);
                }
            }
        } catch (Exception exception) {
            // Same reason as in the base class: the checked exception types differ between Mixin versions
            return false;
        }
        return false;
    }

    private static boolean tickShapeMatches(MethodNode tick) {
        int drainCalls = 0;
        int setBlockCalls = 0;
        boolean readsCobblestone = false;
        for (AbstractInsnNode instruction : tick.instructions) {
            if (instruction instanceof MethodInsnNode call) {
                if ("drainFluidBlock".equals(call.name) && DRAIN_DESCRIPTOR.equals(call.desc) && UTILS_INTERNAL_NAME.equals(call.owner)) {
                    drainCalls++;
                } else if ("setBlockAndUpdate".equals(call.name) && SET_BLOCK_DESCRIPTOR.equals(call.desc) && LEVEL_INTERNAL_NAME.equals(call.owner)) {
                    setBlockCalls++;
                }
            } else if (instruction instanceof FieldInsnNode field
                    && "COBBLESTONE".equals(field.name) && BLOCKS_INTERNAL_NAME.equals(field.owner)) {
                readsCobblestone = true;
            }
        }
        return drainCalls == 2 && setBlockCalls == 1 && readsCobblestone;
    }

    private static boolean pumpAsksWhereExpected(ClassNode target) {
        int gatherCalls = -1;
        int areaCalls = -1;
        for (MethodNode method : target.methods) {
            if ("gatherInfiniteFluidFromWorld".equals(method.name) && GATHER_DESCRIPTOR.equals(method.desc)) {
                gatherCalls = countConvertCalls(method);
            } else if ("checkAreaTick".equals(method.name) && "()V".equals(method.desc)) {
                areaCalls = countConvertCalls(method);
            }
        }
        return gatherCalls == 1 && areaCalls == 2;
    }

    private static int countConvertCalls(MethodNode method) {
        int calls = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call
                    && "canConvertToSource".equals(call.name)
                    && CONVERT_DESCRIPTOR.equals(call.desc)
                    && FLUID_STATE_INTERNAL_NAME.equals(call.owner)) {
                calls++;
            }
        }
        return calls;
    }
}
