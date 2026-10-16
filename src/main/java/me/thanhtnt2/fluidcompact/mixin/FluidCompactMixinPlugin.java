package me.thanhtnt2.fluidcompact.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

/**
 * Applies the Modern Industrialization hook only when MI is installed and still shaped the way
 * the hook expects. A missing or changed MI therefore disables the hook with a warning
 * instead of crashing the game at boot.
 */
public final class FluidCompactMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("FluidCompact/Mixin");

    private static final String PUMP_CLASS =
            "aztech.modern_industrialization.machines.blockentities.AbstractWaterPumpBlockEntity";
    private static final String TANK_INTERNAL_NAME = "aztech/modern_industrialization/inventory/ConfigurableFluidStack";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!PUMP_CLASS.equals(targetClassName)) {
            return true;
        }
        return isPumpHookCompatible();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private boolean isPumpHookCompatible() {
        ClassNode pumpClass;
        try {
            pumpClass = MixinService.getService().getBytecodeProvider().getClassNode(PUMP_CLASS);
        } catch (Exception exception) {
            // Catch-all on purpose: the checked exception types differ between Mixin versions,
            // and "class not found" simply means Modern Industrialization is not installed
            LOGGER.info("Modern Industrialization not found, pump hook disabled");
            return false;
        }
        if (tickCallsTankIncrement(pumpClass)) {
            return true;
        }
        LOGGER.warn("Modern Industrialization pump layout changed, pump hook disabled. Update FluidCompact.");
        return false;
    }

    private boolean tickCallsTankIncrement(ClassNode pumpClass) {
        for (MethodNode method : pumpClass.methods) {
            if ("tick".equals(method.name) && "()V".equals(method.desc) && containsTankIncrement(method)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsTankIncrement(MethodNode method) {
        for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call
                    && "increment".equals(call.name)
                    && TANK_INTERNAL_NAME.equals(call.owner)) {
                return true;
            }
        }
        return false;
    }
}
