package relake.module.implement.combat;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.StringUtils;
import relake.Client;
import relake.event.EventHandler;
import relake.event.impl.player.PlayerEvent;
import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.common.util.MoveUtil;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.MultiSelectSetting;

import java.util.Random;

public class TriggerBotModule extends Module {

    private final Random random = new Random();

    public final Setting<Float> attackRange = new FloatSetting("Радиус атаки")
            .range(1, 6)
            .setValue(3F);

    private final Setting<Boolean> players = new BooleanSetting("Игроки")
            .setValue(false);

    private final Setting<Boolean> friends = new BooleanSetting("Друзья")
            .setValue(false);

    private final Setting<Boolean> naked = new BooleanSetting("Голые")
            .setValue(false);

    private final Setting<Boolean> invisible = new BooleanSetting("Невидимые")
            .setValue(false);

    private final Setting<Boolean> mobs = new BooleanSetting("Мобы")
            .setValue(false);

    private final MultiSelectSetting targets = new MultiSelectSetting("Кого атаковать")
            .setValue("Игроки",
                    "Голые",
                    "Невидимые",
                    "Мобы");

    public final Setting<Boolean> onlyCrits = new BooleanSetting("Только криты")
            .setValue(false);

    public TriggerBotModule() {
        super("Trigger Bot", "Автоматически бьёт по вреждебной цели на курсоре", "Automatically hits a hostile target on the cursor", ModuleCategory.Combat);
        registerComponent(attackRange, targets, onlyCrits);
        //multibox setvalue
        targets.getSelected().add("Игроки");
        targets.getSelected().add("Голые");
        targets.getSelected().add("Невидимые");
        targets.getSelected().add("Мобы");
        targets.getSelected().add("Преследовать одного");
    }

    @EventHandler
    public void player(PlayerEvent playerEvent) {
        if (mc.pointedEntity == null || Client.instance.moduleManager.attackAuraModule.getTarget() != null) return;

        if (validateEntity((LivingEntity) mc.pointedEntity)) {
            if (shouldAttack()) {
                boolean sprint = Client.instance.moduleManager.sprintModule.isEnabled();

                if (CEntityActionPacket.lastUpdatedSprint && !sprint) {
                    mc.player.setSprinting(false);
                    mc.player.setServerSprintState(false);
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                    sprint = true;
                }

                mc.playerController.attackEntity(mc.player, mc.pointedEntity);
                mc.player.swingArm(Hand.MAIN_HAND);

                if (sprint) {
                    mc.player.setSprinting(true);
                    mc.player.setServerSprintState(true);
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                }
            }
        }
    }

    private boolean validateEntity(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            if (!players.getValue()) return false;

            if (!naked.getValue() && player.getTotalArmorValue() <= 0) return false;

            if (!invisible.getValue() && player.isInvisible()) return false;

            if (!friends.getValue() && Client.instance.friendManager.isFriend(StringUtils.stripControlCodes(entity.getNotHidedName().getString()))) return false;
        }

        if (entity instanceof ClientPlayerEntity) return false;

        if (entity.getHealth() <= 0) return false;

        if (entity instanceof ArmorStandEntity) return false;

        if (entity instanceof MonsterEntity && !mobs.getValue()) return false;

        if (entity instanceof AnimalEntity && !mobs.getValue()) return false;

        return !(mc.player.getDistance(entity) > attackRange.getValue());
    }

    public boolean shouldAttack() {
        if (mc.player.getCooledAttackStrength(0.5f) < random.nextFloat(0.91f, 0.93f)) {
            return false;
        }

        if (!shouldCancelCritical() && onlyCrits.getValue()) {
            int posY = (int) mc.player.getPosY();
            int posYCeil = (int) Math.ceil(mc.player.getPosY());
            if (posY != posYCeil && mc.player.isOnGround() && MoveUtil.isBlockAboveHead()) {
                return true;
            }
            return mc.player.fallDistance > 0;
        }

        return true;
    }

    private boolean shouldCancelCritical() {
        boolean isDeBuffed = mc.player.isPotionActive(Effects.LEVITATION) || mc.player.isPotionActive(Effects.BLINDNESS) || mc.player.isPotionActive(Effects.SLOW_FALLING);
        boolean isInLiquid = mc.player.areEyesInFluid(FluidTags.WATER) || mc.player.areEyesInFluid(FluidTags.LAVA);
        boolean isFlying = mc.player.abilities.isFlying || mc.player.isElytraFlying();
        boolean isClimbing = mc.player.isOnLadder();
        boolean isCantJump = mc.player.isPassenger();

        return isDeBuffed || isInLiquid || isFlying || isClimbing || isCantJump;
    }
}
