package relake.module.implement.combat;

import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.animation.excellent.util.Easings;
import relake.common.component.TargetComponent;
import relake.common.component.hitaura.UAura;
import relake.common.component.hitaura.URotate;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.ActionEvent;
import relake.event.impl.player.MovementInputEvent;
import relake.event.impl.player.MovementInputKeysEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.world.Render3D;
import relake.settings.Setting;
import relake.settings.implement.*;

import java.security.SecureRandom;

public class AttackAuraModule extends Module {
    
    private final SecureRandom random = new SecureRandom();

    public final SelectSetting rotationModeImpl = new SelectSetting("Ротация головы")
            .setValue("None",
                    "Advanced",
                    "Matrix",
                    "Snap",
                    "Snap smooth",
                    "FunTime",
                    "SpookyTime Anarchy",
                    "SpookyTime Duel");


    private final Setting<Boolean> viewLock = new BooleanSetting("Поворачивать камеру")
            .setValue(false).setVisible(() -> !rotationModeImpl.isSelected("None"));

    private final SelectSetting rangeModeImpl = new SelectSetting("Мод радиуса атаки")
            .setValue("Сильный античит",
                    "NCP или аналоги",
                    "Античит AAC",
                    "Античит Matrix",
                    "Слабый античит",
                    "Свои настройки");

    private final Setting<Float> attackRange = new FloatSetting("Радиус атаки")
            .range(1.F, 6.F, .05F)
            .setValue(3.F)
            .setVisible(() -> rangeModeImpl.isSelected("Свои настройки"));

    private final Setting<Float> preRange = new FloatSetting("Доп. дистанция")
            .range(0.F, 3.F, .1F)
            .setValue(.0F)
            .setVisible(() -> rangeModeImpl.isSelected("Свои настройки"));

    public final Setting<Boolean> ignoreWalls = new BooleanSetting("Игнорировать стены")
            .setValue(true);

    private final Setting<Boolean> shieldBreaker = new BooleanSetting("Ломать щит")
            .setValue(true);

    private final SelectSetting eatFixMode = new SelectSetting("При поедании").setValue("Бить дальше", "Прекратить бить", "NCP обход");

    // Легитные настройки согласно теории
    private final Setting<Boolean> legitHitChance = new BooleanSetting("Шанс удара (легит)")
            .setValue(false);
    
    private final Setting<Float> hitChancePercent = new FloatSetting("Процент попаданий")
            .range(50.F, 100.F, 1.F)
            .setValue(85.F)
            .setVisible(() -> legitHitChance.getValue());
    
    private final Setting<Boolean> postHitRotation = new BooleanSetting("Отведение после удара")
            .setValue(false);
    
    private final SelectSetting postHitRotationType;

    public final Setting<String> mainSettings = new DelimiterSetting("Основные настройки");

    private final SelectSetting stopSprint = new SelectSetting("Сброс спринта")
            .setValue("Никогда",
                    "Незаметно",
                    "Перед ударом",
                    "Задолго до удара",
                    "Сильно",
                    "БОМБА",
                    "Легитный");

    public final SelectSetting sortMode = new SelectSetting("Сортировать по")
            .setValue("Всему",
                    "Дистанции",
                    "Здоровью");

    public final SelectSetting moveCorrection = new SelectSetting("Выбор коррекции")
            .setValue("Свободный",
                    "Фокусированный");


    public final MultiSelectSetting targets = new MultiSelectSetting("Кого атаковать")
            .setValue("Игроки",
                    "Голые",
                    "Невидимые",
                    "Мобы",
                    "Преследовать одного");

    public final SelectSetting cooldownMode = new SelectSetting("Комбат куллдаун").setValue("1.8.9 или ниже", "1.9 или выше");


    public final Setting<String> otherSettings = new DelimiterSetting("Прочие настройки");
    public final Setting<Boolean> groundHitting = new BooleanSetting("Позволить комбить").setValue(true);
    public final Setting<Boolean> cpsBypass = new BooleanSetting("Обход CPS").setValue(true);
    public final Setting<Boolean> renderBoxPoint = new BooleanSetting("Показать точку ротейта").setValue(false);
    public final Setting<Boolean> postMissSpamming = new BooleanSetting("Закликать посте мисса").setValue(false);
    @Getter
    private LivingEntity target;

    private final URotate pro_rotation;
    
    // Легитный сброс спринта - счетчик для блокировки движения
    private int p = 0;

    public AttackAuraModule() {
        super("Attack Aura", "Автоматически атакует врагов в доступном радиусе", "Automatically attacks enemies within an available radius", ModuleCategory.Combat);
        
        // Инициализация postHitRotationType с setVisible
        postHitRotationType = new SelectSetting("Тип отведения")
                .setValue("30-40°", "360°", "Случайное");
        postHitRotationType.setVisible(() -> postHitRotation.getValue());
        
        registerComponent(rotationModeImpl, viewLock, rangeModeImpl, attackRange, preRange, ignoreWalls, shieldBreaker, legitHitChance, hitChancePercent, postHitRotation, postHitRotationType, mainSettings, eatFixMode, stopSprint, sortMode, targets, moveCorrection, otherSettings, groundHitting, cpsBypass, renderBoxPoint, postMissSpamming, cooldownMode);
        this.pro_rotation = URotate.create(this);

        //mode setvalue
        rotationModeImpl.setSelected("FunTime");
        rangeModeImpl.setSelected("Свои настройки");
        eatFixMode.setSelected("Бить дальше");
        stopSprint.setSelected("Незаметно");
        sortMode.setSelected("Дистанции");
        moveCorrection.setSelected("Свободный");

        //multibox setvalue
        targets.getSelected().add("Игроки");
        targets.getSelected().add("Голые");
        targets.getSelected().add("Невидимые");
        targets.getSelected().add("Мобы");
        targets.getSelected().add("Преследовать одного");

        cooldownMode.setSelected("1.9 или выше");
    }

    public boolean isViewLockMoment() {
        return this.isEnabled() && target != null && viewLock.getValue() && !rotationModeImpl.isSelected("None");
    }

    //use a ray-cast check
    public boolean isRayCastRuleToAttack() {
        return !(rotationModeImpl.isSelected("None") || rotationModeImpl.isSelected("Matrix"));
    }

    //calc range & pre-range
    public double[] getRanges() {
        return switch (rangeModeImpl.getSelected()) {
            case "Сильный античит" -> new double[]{3.05F, .4D};
            case "NCP или аналоги" -> new double[]{4.05D, 2.7D};
            case "Античит AAC" -> new double[]{3.56D, 1.15D};
            case "Античит Matrix" -> new double[]{3.3D, .65D};
            case "Слабый античит" -> new double[]{6.D, .0D};
            case "Свои настройки" -> new double[]{attackRange.getValue(), preRange.getValue()};
            default -> new double[]{3.F, 1.F};
        };
    }

    //vectors point & rotation
    private Vector3d lastHandledTargetPointVector, prevHandledTargetPointVector;

    private Vector3d getVectorInterpolated(Vector3d oldVec, Vector3d vec, float partialTicks) {
        return new Vector3d(MathUtil.lerp(oldVec.x, vec.x, partialTicks), MathUtil.lerp(oldVec.y, vec.y, partialTicks), MathUtil.lerp(oldVec.z, vec.z, partialTicks));
    }

    private Vector2f lastHandledVectorToRotation;

    private void resetAllVectors() {
        prevHandledTargetPointVector = null;
        lastHandledTargetPointVector = null;
        lastHandledVectorToRotation = null;
    }

    private void updateAllVectors(Vector3d point, Vector2f rotation) {
        prevHandledTargetPointVector = lastHandledTargetPointVector;
        lastHandledTargetPointVector = point;
        lastHandledVectorToRotation = rotation;
    }

    //attack aura action void
    @EventHandler
    public void tick(TickEvent event) {

        //getting range and pre-range values
        double[] ranges = getRanges();
        if (Client.instance.moduleManager.elytraTargetModule.isEnabled() && mc.player.isElytraFlying())
            ranges[1] = Client.instance.moduleManager.elytraTargetModule.detectionRange.getValue();
        ranges = new double[]{ranges[0], ranges[1], ranges[0] + ranges[1]};

        //update targets

        target = TargetComponent.getStableTarget(ranges[2], target, targets.isSelected("Преследовать одного"));

        //target filter not null
        if (target == null || mc.world.getEntityByID(target.getEntityId()) == null) {
            this.resetAllVectors();
            return;
        }

        //update rotations
        final URotate.Vector3d2fRB rotationData = pro_rotation.updateRotationData(rotationModeImpl.getSelected(), lastHandledVectorToRotation == null ? new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch) : lastHandledVectorToRotation, ranges[2]);
        if (Client.instance.moduleManager.strafeModule.callStopRotates) {
            this.resetAllVectors();
            pro_rotation.rotatePrimitiveToSend(null);
        } else if (rotationData.isToUse()) {
            this.updateAllVectors(rotationData.getVec3d(), rotationData.getVec2f());
            pro_rotation.rotatePrimitiveToSend(lastHandledVectorToRotation);
        } else this.resetAllVectors();

        //anti miss hitting
        UAura.antiMissesHittingUpdate(target, cpsBypass.getValue(), this.isRayCastRuleToAttack(), postMissSpamming.getValue());

        // Проверка 180-градусного сектора для SpookyTime Anarchy (Killaura работает только в фронтальной полусфере)
        if (rotationModeImpl.isSelected("SpookyTime Anarchy") && target != null) {
            float playerYaw = mc.player.rotationYaw;
            Vector3d targetPos = target.getPositionVec();
            Vector3d playerPos = mc.player.getPositionVec();
            Vector3d vec = targetPos.subtract(playerPos);
            float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
            float yawDiff = Math.abs(MathHelper.wrapDegrees(yawToTarget - playerYaw));
            // Если цель не в пределах 90 градусов в каждую сторону (итого 180 градусов), не атакуем
            if (yawDiff > 90.0F) {
                return;
            }
        }
        
        //if can attack (без задержек для максимальной эффективности - нулевая задержка для мгновенных атак)
        if (!UAura.shouldAttack(target, this.isRayCastRuleToAttack(), Client.instance.moduleManager.attackAuraModule.cooldownMode.isSelected("1.9 или выше"), 0L, ranges)) return;

        //control eating rules
        if (mc.player.isEating()) {
            if (eatFixMode.isSelected("Прекратить бить")) return;
            else if (eatFixMode.isSelected("NCP обход"))
                new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, new BlockPos(-1, -1, -1), Direction.DOWN).sendSilent();
        }

        //tasks on attack
        final Runnable[]
                shieldBreak = UAura.hitShieldBreakTaskForUse(target, shieldBreaker.getValue()),
                shieldPressBypass = UAura.resetShieldSilentTaskForUse(true),
                skipSilentSprint = stopSprint.isSelected("БОМБА") 
                    ? UAura.bombSprintResetTaskForUse(target, ranges)
                    : UAura.skipSilentSprintingTaskForUse(!stopSprint.isSelected("Никогда"));
        final Runnable preHitSendCodeSingleTick = () -> {
            //pre hit send
            skipSilentSprint[0].run();
            shieldPressBypass[0].run();
            shieldBreak[0].run();
        }, postHitSendCodeSingleTick = () -> {
            //post hit send
            shieldBreak[1].run();
            shieldPressBypass[1].run();
            skipSilentSprint[1].run();
        };

        // Шанс удара (легит) - согласно теории
        if (legitHitChance.getValue()) {
            float hitChance = hitChancePercent.getValue() / 100.F;
            if (random.nextFloat() > hitChance) {
                // Промах - не атакуем, но делаем анимацию взмаха
                mc.player.swingArm(Hand.MAIN_HAND);
                return;
            }
        }
        
        // Легитный сброс спринта - логика сброса перед атакой
        if (stopSprint.isSelected("Легитный") && target != null) {
            boolean sprinting = mc.player.isSprinting() && mc.player.fallDistance > 0;
            if (sprinting) {
                p = 1;
                if (mc.player.isServerSprintState()) {
                    return;
                }
            }
        }
        
        //do attacking
        boolean hitSuccess = UAura.useEntity(target, preHitSendCodeSingleTick, postHitSendCodeSingleTick, Hand.MAIN_HAND, cpsBypass.getValue());
        
        // Отведение после удара (легит) - согласно теории
        if (hitSuccess && postHitRotation.getValue() && target != null) {
            pro_rotation.applyPostHitRotation(postHitRotationType.getSelected());
        }
    }

    //rendering a rotation point in 3d as cube with glow in the area of target bounding box
    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (renderBoxPoint.getValue() && lastHandledTargetPointVector != null && target != null) {
            final Vector3d renderPoint = prevHandledTargetPointVector == null ? lastHandledTargetPointVector : getVectorInterpolated(prevHandledTargetPointVector, lastHandledTargetPointVector, event.getTicks());
            float cooledPCWave = UAura.msCooldownPC01();
            cooledPCWave = (cooledPCWave > .5F ? 1.F - cooledPCWave : cooledPCWave) * 2.F;
            cooledPCWave = (float) Easings.QUAD_IN_OUT.ease(cooledPCWave);
            final float scale = .05F + .025F * cooledPCWave;
            final int colorOutline = Client.instance.moduleManager.hudModule.color.getValue().getRGB(), colorFill = ColorUtil.multDark(colorOutline, .5F), colorDepth = colorOutline, colorVisualHackGlow = ColorUtil.multDark(colorOutline, .2F);
            final AxisAlignedBB aabb = new AxisAlignedBB(renderPoint, renderPoint).grow(scale / 2.F);
            Render3D.setup3dForBlockPos(event, () -> {
                GL11.glLineWidth(.1F);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                Render3D.drawCanisterBox(event.getStack(), BUFFER, TESSELLATOR, aabb.grow(-.015F), false, false, true, 0, 0, colorDepth);
                Render3D.drawCanisterBox(event.getStack(), BUFFER, TESSELLATOR, aabb, true, true, true, colorOutline, colorOutline, colorFill);
                int iterationsGlow = 15;
                GL11.glLineWidth(1.F);
                for (int iteration = 0; iteration < iterationsGlow; iteration++) {
                    Render3D.drawCanisterBox(event.getStack(), BUFFER, TESSELLATOR, aabb.grow(iteration * .0035F), true, false, false, ColorUtil.multAlpha(colorVisualHackGlow, 1.F - (iteration / (float) iterationsGlow)), 0, 0);
                }
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }, true, true);
        }
    }

    //cancel sprint pre attack
    @EventHandler
    public void onAction(ActionEvent event) {
        //presumably, it will be possible to pre-attack
        if (UAura.cancelSprintTick(target, getRanges(), stopSprint.getSelected())) event.setSprintState(false);
    }

    @EventHandler
    public void onMoveKeys(MovementInputKeysEvent event) {
        String sprintMode = stopSprint.getSelected();
        if (sprintMode.equalsIgnoreCase("Сильно") && UAura.cancelSprintTick(target, getRanges(), "Сильно")) {
            event.stopWASD();
        } else if (sprintMode.equalsIgnoreCase("БОМБА") && UAura.cancelSprintTick(target, getRanges(), "БОМБА")) {
            // Максимально агрессивный сброс - останавливаем движение для максимального обхода
            event.stopWASD();
        }
    }
    
    // Легитный сброс спринта - обработчик MovementInputEvent
    @EventHandler
    public void onInput(MovementInputEvent event) {
        if (stopSprint.isSelected("Легитный")) {
            if (p > 0) {
                event.setForward(0);
                p--;
            }
        }
    }

    //reset all fields
    @Override
    public void disable() {
        super.disable();
        target = null;
        this.resetAllVectors();
        UAura.antiMissesHittingReset();
        UAura.hitCounterCPSBypassReset();
        pro_rotation.resetRotateVector();
    }

    //reset all fields
    @Override
    public void enable() {
        super.enable();
        target = null;
        this.resetAllVectors();
        UAura.antiMissesHittingReset();
        UAura.hitCounterCPSBypassReset();
        pro_rotation.resetRotateVector();
    }
}