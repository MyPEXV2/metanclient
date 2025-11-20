package relake.module.implement.combat;

import net.minecraft.entity.LivingEntity;
import relake.module.Module;
import relake.module.ModuleCategory;

public class HitAuraModule extends Module {
    public static LivingEntity TARGET, TARGET_ROTS;
    public HitAuraModule() {
        super("Hit Aura", "Пялит твою матуху пока она под транквилизаторами валяется в луже мочи", "Automatically attacks enemies within an available radius", ModuleCategory.Combat);
    }
}
