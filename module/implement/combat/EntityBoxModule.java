package relake.module.implement.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.MultiSelectSetting;
import relake.settings.implement.SelectSetting;

public class EntityBoxModule extends Module {
    private final Setting<Boolean> players = new BooleanSetting("Игроки").setValue(true);
    private final Setting<Boolean> invisiblePlayers = new BooleanSetting("Невидимые игроки").setValue(true).setVisible(() -> players.getValue());
    private final Setting<Boolean> nakedPlayers = new BooleanSetting("Голые игроки").setValue(true).setVisible(() -> players.getValue());
    private final Setting<Boolean> mobs = new BooleanSetting("Мобы").setValue(false);

    public final MultiSelectSetting targets = new MultiSelectSetting("Чьи хитбоксы изменять")
            .setValue("Игроки",
                    "Невидимые игроки",
                    "Голые игроки",
                    "Мобы");

    private final Setting<Float> boxRescale = new FloatSetting("Увеличить хитбокс").range(.0F, 1.5F, .05f).setValue(.25F).setVisible(() -> players.getValue() || mobs.getValue());
    private final Setting<Boolean> reaches = new BooleanSetting("Использовать ричи").setValue(false);
    private final Setting<Float> entityReachAddition = new FloatSetting("Добавить рич энтити").range(0.F, 3.F, .05F).setValue(.5F).setVisible(() -> reaches.getValue());
    private final Setting<Float> blockReachAddition = new FloatSetting("Добавить рич блоков").range(0.F, 2.F, .1F).setValue(.2F).setVisible(() -> reaches.getValue());

    public EntityBoxModule() {
        super("Entity Box", "Увеличивет зону досигаемости и увеличивает хитбоксы существ", "Increases the range and increases the hitboxes of the creatures", ModuleCategory.Combat);
        registerComponent(targets, boxRescale, reaches, entityReachAddition, blockReachAddition);
        targets.getSelected().add("Игроки");
    }

    public AxisAlignedBB influenceToEntityHitBoxExpanding(Entity entityIn, AxisAlignedBB prevBox) {
        float expand = 0.F;
        if (entityIn != null && mc.world != null && this.isEnabled() && (players.getValue() || mobs.getValue())) {
            if (entityIn instanceof LivingEntity living && living.ticksExisted > 1 && !living.equals(mc.player) && living.getDataManager() != null && living.isAlive()) {
                if (players.getValue() && living instanceof PlayerEntity player && !player.equals(mc.player) && (invisiblePlayers.getValue() || !player.isInvisible()) && (nakedPlayers.getValue() || !player.isNaked())) expand += boxRescale.getValue();
                else if (mobs.getValue()) expand += boxRescale.getValue();
            }
        }
        final double w = prevBox.maxX - prevBox.minX, h = prevBox.maxY - prevBox.minY;
        return new AxisAlignedBB(prevBox.minX - w * expand, prevBox.minY, prevBox.minZ - w * expand, prevBox.maxX + w * expand, prevBox.maxY + h * expand / 2.F, prevBox.maxZ + w * expand);
    }

    public double influenceEntityReachAddition(double previousReach) {
        return this.isEnabled() && reaches.getValue() ? previousReach + entityReachAddition.getValue() : previousReach;
    }

    public double influenceBlockReachAddition(double previousReach) {
        return this.isEnabled() && reaches.getValue() ? previousReach + blockReachAddition.getValue() : previousReach;
    }
}
