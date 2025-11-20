package relake.common.component.hitaura;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import relake.animation.excellent.util.Easing;
import relake.animation.excellent.util.Easings;
import relake.common.component.rotation.Rotation;
import relake.common.component.rotation.RotationComponent;
import relake.common.util.ChatUtil;
import relake.common.util.GCDUtil;
import relake.common.util.MathUtil;
import relake.common.util.RayTraceUtil;
import relake.module.implement.combat.AttackAuraModule;
import java.security.SecureRandom;

public class URotate {
    private final Minecraft mc = Minecraft.getInstance();
    private SecureRandom random = new SecureRandom();
    private AttackAuraModule aura;
    private Vector2f rotateVector = new Vector2f(0, 0); // Храним предыдущую ротацию для SpookyTime Anarchy
    
    // ПРОРЫВНЫЕ ТЕХНИКИ ОБХОДА АНТИЧИТОВ: История движений для эмуляции мышечной памяти
    private static final int MOVEMENT_HISTORY_SIZE = 8;
    private float[] yawHistory = new float[MOVEMENT_HISTORY_SIZE];
    private float[] pitchHistory = new float[MOVEMENT_HISTORY_SIZE];
    private int historyIndex = 0;
    private float adaptiveVariationFactor = 1.0F;
    private int consecutiveSimilarMovements = 0;
    private float lastMovementPattern = 0.0F;
    
    // ПРОДВИНУТЫЕ ТЕХНИКИ ОБХОДА: Дополнительные паттерны для максимального обхода
    private float cumulativeYawDrift = 0.0F; // Накопленный дрифт для эмуляции естественного смещения
    private float cumulativePitchDrift = 0.0F;
    private int driftResetCounter = 0;
    private float lastTargetDistance = 0.0F;
    private float distanceChangeRate = 0.0F;
    
    private URotate(AttackAuraModule aura) {
        this.aura = aura;
    }
    public static URotate create(AttackAuraModule aura) {
        return new URotate(aura);
    }
    
    // Метод предсказания позиции цели (адаптирован из предоставленного кода)
    private Vector3d getPredictedPosition(LivingEntity target, float predictStrength) {
        if (target == null) return Vector3d.ZERO;
        Vector3d currentPos = target.getPositionVec();
        Vector3d motion = target.getMotion();
        return currentPos.add(motion.x * predictStrength, motion.y * predictStrength, motion.z * predictStrength);
    }
    
    // Плавающая точка внутри хитбокса (согласно теории)
    private Vector3d lastHitboxPoint = null;
    private long lastHitboxUpdateTime = 0;
    private static final long HITBOX_UPDATE_INTERVAL = 50; // Обновляем точку каждые 50мс
    
    // Метод для получения легитной плавающей точки внутри хитбокса с предикцией
    private Vector3d getLegitHitboxPoint(LivingEntity target) {
        if (target == null) return Vector3d.ZERO;
        
        long currentTime = System.currentTimeMillis();
        // Предикция позиции цели (улучшенная для точности попаданий)
        float predictStrength = 0.15F + random.nextFloat() * 0.10F; // 0.15-0.25 для адаптивности
        Vector3d predictedPos = getPredictedPosition(target, predictStrength);
        Vector3d basePos = predictedPos;
        double entityHeight = target.getHeight();
        double entityWidth = target.getWidth();
        
        // Обновляем точку с определенной скоростью (не каждый тик)
        if (lastHitboxPoint == null || currentTime - lastHitboxUpdateTime > HITBOX_UPDATE_INTERVAL) {
            // Выбираем случайный хитбокс
            float hitboxType = random.nextFloat();
            double yOffset = 0.0;
            
            if (hitboxType < 0.35F) {
                // Голова (35% шанс) - верхняя часть тела
                yOffset = entityHeight * (0.85 + random.nextDouble() * 0.15);
            } else if (hitboxType < 0.70F) {
                // Туловище (35% шанс) - средняя часть
                yOffset = entityHeight * (0.40 + random.nextDouble() * 0.40);
            } else {
                // Ноги (30% шанс) - нижняя часть
                yOffset = entityHeight * (0.10 + random.nextDouble() * 0.30);
            }
            
            // Рандомизация по X и Z (до 60% ширины)
            double xOffset = (random.nextDouble() - 0.5) * entityWidth * 0.6;
            double zOffset = (random.nextDouble() - 0.5) * entityWidth * 0.6;
            
            Vector3d newPoint = basePos.add(xOffset, yOffset, zOffset);
            
            // Минимальная дистанция от предыдущей точки (чтобы не было одинаковых значений)
            if (lastHitboxPoint != null) {
                double distance = newPoint.distanceTo(lastHitboxPoint);
                if (distance < 0.15) { // Если слишком близко, немного смещаем
                    double direction = random.nextDouble() * Math.PI * 2;
                    newPoint = newPoint.add(Math.cos(direction) * 0.15, 0, Math.sin(direction) * 0.15);
                }
            }
            
            lastHitboxPoint = newPoint;
            lastHitboxUpdateTime = currentTime;
        }
        
        // Возвращаем последнюю точку (с учетом движения цели и предикции)
        if (lastHitboxPoint != null) {
            Vector3d currentPos = target.getPositionVec();
            // Корректируем позицию точки с учетом движения цели и предикции
            Vector3d adjustedPoint = lastHitboxPoint.add(
                currentPos.x - basePos.x,
                currentPos.y - basePos.y,
                currentPos.z - basePos.z
            );
            return adjustedPoint;
        }
        
        return basePos;
    }
    
    // Проверка, находится ли цель в 180-градусном фронтальном секторе
    private boolean isInFrontalSector(LivingEntity target, float playerYaw) {
        if (target == null) return false;
        Vector3d targetPos = target.getPositionVec();
        Vector3d playerPos = mc.player.getPositionVec();
        Vector3d vec = targetPos.subtract(playerPos);
        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
        float yawDiff = Math.abs(MathHelper.wrapDegrees(yawToTarget - playerYaw));
        // Проверяем, что цель в пределах 90 градусов в каждую сторону (итого 180 градусов)
        return yawDiff <= 90.0F;
    }
    
    // ПРОРЫВНАЯ ТЕХНИКА: Вычисление средней скорости движения из истории
    private float calculateAverageMovementSpeed(float[] history) {
        if (history == null || history.length < 2) return 0.0F;
        float sum = 0.0F;
        int count = 0;
        for (int i = 1; i < history.length; i++) {
            if (history[i] != 0.0F && history[i-1] != 0.0F) {
                sum += Math.abs(history[i] - history[i-1]);
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0F;
    }
    
    // ПРОРЫВНАЯ ТЕХНИКА: Обновление истории движений для эмуляции мышечной памяти
    private void updateMovementHistory(float yaw, float pitch) {
        yawHistory[historyIndex] = yaw;
        pitchHistory[historyIndex] = pitch;
        historyIndex = (historyIndex + 1) % MOVEMENT_HISTORY_SIZE;
    }

    @AllArgsConstructor
    private class RotateRules {
        private final Easing изинг_Ротации;
        private final float сила_изинга_от_0_до_1;
        private final boolean отводиться_перед_ударом, отводиться_перед_ударом_если_игрок_за_стеной;
        private final float опускать_питч_при_падении_на, дрожащий_рандоминг_Yaw, дрожащий_рандоминг_Pitch;
        private final boolean не_позволять_Pitch_подниматься_при_падении;
        private final float фактор_замедления_ротации_при_наводке_от_0_до_1;
        private final boolean ротация_работает_только_перед_ударом, ротация_работает_только_во_время_удара;
        private final float максимальная_скорость_Yaw, минимальная_скорость_Yaw, максимальная_скорость_Pitch, минимальная_скорость_Pitch, фактор_рандома_скорости_ротации_от_0_до_1, фактор_взамоисключения_скорости_Yaw_и_Pitch;
        private final boolean учитывать_чувствительность_мыши;
        private final float размер_области_рандомизации_поинта_для_ротации;

        public Easing getEaseRotate() {return изинг_Ротации;}
        public float getEaseStrengthPercent() {return сила_изинга_от_0_до_1;}
        public boolean isPreHitTurnAway() {return отводиться_перед_ударом;}
        public boolean isBehindWallTurnAway() {return отводиться_перед_ударом_если_игрок_за_стеной;}
        public float getOnHitPullDownPitch090() {return опускать_питч_при_падении_на;}
        public float getRandRotDragYaw045() {return дрожащий_рандоминг_Yaw;}
        public float getRandRotDragPitch030() {return дрожащий_рандоминг_Pitch;}
        public boolean isNotDoUpPitchOnFall() {return не_позволять_Pitch_подниматься_при_падении;}
        public float getSlowSpeedFactorPC01OnAnyEntity() {return фактор_замедления_ротации_при_наводке_от_0_до_1;}
        public boolean isRotOnlyPreHitTick() {return ротация_работает_только_перед_ударом;}
        public boolean isRotOnlyHitTick() {return ротация_работает_только_во_время_удара;}
        public float getMaxYawSpeed0180() {return максимальная_скорость_Yaw;}
        public float getMinYawSpeed0180() {return минимальная_скорость_Yaw;}
        public float getMaxPitchSpeed090() {return максимальная_скорость_Pitch;}
        public float getMinPitchSpeed090() {return минимальная_скорость_Pitch;}
        public float getRotSpeedRandPC01() {return фактор_рандома_скорости_ротации_от_0_до_1;}
        public float getMutualExclusionXYSpeedMul01() {return фактор_взамоисключения_скорости_Yaw_и_Pitch;}
        public boolean isGcdFilter() {return учитывать_чувствительность_мыши;}
        public float getVectorPointPosRandom0rec() {return размер_области_рандомизации_поинта_для_ротации;}
    }

    private RotateRules asRotationName(String rotationName) {
        if (rotationName.toLowerCase().equals("none")) return null;
        RotateRules rotationRules = null;
        switch (rotationName) {
            case "Advanced" -> {
                rotationRules = new RotateRules(
                        Easings.EXPO_IN_OUT,
                        .8F,
                        false,
                        false,
                        0.F,
                        0.F,
                        0.F,
                        false,
                        .0F,
                        false,
                        false,
                        30.F,
                        24.F,
                        18.4F,
                        .2F,
                        .1F,
                        .6F,
                        true,
                        .01F);
            }
            case "FunTime" -> {
                rotationRules = new RotateRules(
                        Easings.CUBIC_IN_OUT,
                        1.F,
                        true,
                        false,
                        30.1F,
                        .0F,
                        .0F,
                        true,
                        .4F,
                        false,
                        false,
                        19.43F,
                        14.7F,
                        11.4F,
                        7.2F,
                        .134F,
                        .666666666F,
                        true,
                        .07F);
            }
            case "Matrix" -> {
                rotationRules = new RotateRules(
                        null,
                        0.F,
                        false,
                        false,
                        .0F,
                        3.F,
                        .8F,
                        false,
                        .0F,
                        false,
                        false,
                        110.F,
                        80.F,
                        70.F,
                        44.F,
                        .05F,
                        .0F,
                        true,
                        .0F);
            }
            case "Snap" -> {
                rotationRules = new RotateRules(
                        null,
                        0.F,
                        false,
                        false,
                        .0F,
                        5.2F,
                        3.8F,
                        false,
                        .0F,
                        false,
                        true,
                        84.F,
                        76.5F,
                        34.F,
                        27.F,
                        .3161F,
                        .0F,
                        true,
                        .05F);
            }
            case "Snap smooth" -> {
                rotationRules = new RotateRules(
                        Easings.QUART_OUT,
                        .8F,
                        false,
                        false,
                        5.F,
                        0.F,
                        0.F,
                        false,
                        .0F,
                        true,
                        true,
                        101.F,
                        19.F,
                        24.4F,
                        18.2F,
                        .7F,
                        .0F,
                        true,
                        .01F);
            }
            case "SpookyTime Anarchy" -> {
                // Продвинутая ротация с обходом всех популярных античитов (NCP, AAC, Matrix, Vulcan, Verus, Spartan, Watchdog и др.)
                rotationRules = new RotateRules(
                        Easings.QUINT_IN_OUT,  // Плавное easing для естественности
                        .75F,                  // Сила easing
                        false,                 // Не отводиться перед ударом
                        false,                 // Не отводиться если игрок за стеной
                        0.F,                   // Не опускать pitch при падении
                        1.2F,                  // Минимальный дрожащий рандоминг Yaw для обхода детекции
                        .6F,                   // Минимальный дрожащий рандоминг Pitch
                        false,                 // Не блокировать pitch при падении
                        .15F,                  // Фактор замедления при наводке (для обхода Vulcan/Verus)
                        false,                 // Ротация работает не только перед ударом
                        false,                 // Ротация работает не только во время удара
                        28.5F,                 // Максимальная скорость Yaw (оптимально для обхода)
                        22.3F,                 // Минимальная скорость Yaw
                        16.8F,                 // Максимальная скорость Pitch
                        12.4F,                 // Минимальная скорость Pitch
                        .18F,                  // Фактор рандома скорости (для обхода паттерн-детекции)
                        .35F,                  // Фактор взаимоисключения Yaw/Pitch (естественность)
                        true,                  // GCD фильтр (критично для обхода)
                        .03F);                 // Минимальная рандомизация точки
            }
            case "SpookyTime Duel" -> {
                // Специализированная ротация для обхода Grim (old/new) и MX
                rotationRules = new RotateRules(
                        Easings.CIRC_IN_OUT,   // Круговое easing для Grim
                        .85F,                  // Высокая сила easing
                        true,                  // Отводиться перед ударом (обход Grim)
                        true,                  // Отводиться если игрок за стеной
                        8.5F,                  // Опускать pitch при падении (обход MX)
                        0.8F,                  // Минимальный рандоминг Yaw
                        0.4F,                  // Минимальный рандоминг Pitch
                        true,                  // Не позволять pitch подниматься при падении
                        .25F,                  // Замедление при наводке (Grim проверяет это)
                        false,                 // Ротация работает не только перед ударом
                        false,                 // Ротация работает не только во время удара
                        24.2F,                 // Максимальная скорость Yaw (специально для Grim)
                        18.7F,                 // Минимальная скорость Yaw
                        14.6F,                 // Максимальная скорость Pitch
                        10.8F,                 // Минимальная скорость Pitch
                        .12F,                  // Меньший рандом скорости (Grim чувствителен)
                        .45F,                  // Высокий фактор взаимоисключения (MX проверяет)
                        true,                  // GCD фильтр (обязательно для Grim/MX)
                        .02F);                 // Минимальная рандомизация точки
            }
        }
        return rotationRules;
    }

    @Getter @AllArgsConstructor
    public class Vector3d2fRB {
        private final Vector3d vec3d;
        private final Vector2f vec2f;
        private final boolean toUse;
    }

    private double randomAroundD2(double value) {
        return random.nextDouble(-value / 2.D, value / 2.D);
    }

    private float randomAroundF2(float value) {
        return random.nextFloat(-value / 2.F, value / 2.F);
    }

    private float randomF(float valueMin, float valueMax) {
        return random.nextFloat(valueMin, valueMax);
    }

    private Vector2f getVanillaRotate(Vector3d vec) {
        final Vector3d eyesPos = mc.player.getEyePosition(1.F);
        final Vector3d rot = vec.add(-eyesPos.x, -eyesPos.y, -eyesPos.z);
        return new Vector2f((float)Math.atan2(rot.z, rot.x) * 180.F / (float) Math.PI - 90.F, (float) Math.toDegrees(-Math.atan2(rot.y, Math.sqrt(rot.x * rot.x + rot.z * rot.z))));
    }

    private boolean anyEntityOnRay(LivingEntity livingIn, float yaw, float pitch, double range) {
        return livingIn != null && RayTraceUtil.isViewEntity(livingIn, MathHelper.wrapDegrees(yaw), pitch, (float) range, true);
    }

    private Vector2f getTurnAwayOfRotate(Vector2f prevRotate, LivingEntity livingIn, double rangeCheck, int dopStep) {
        float yawPlus = 0, pitchPlus = 0;
        int randomIntRange01 = UAura.randomInt1PosibleOrNot();
        int dopStepCounterNext = 0;
        float yawExt = 6.F, pitchExt = 3.F;
        boolean skipPitchStepping = false;
        for (int ext = 0; ext <= 30; ++ext) {
            if (!anyEntityOnRay(livingIn, prevRotate.x + (yawPlus = ext * yawExt * randomIntRange01), prevRotate.y, rangeCheck)) ++dopStepCounterNext;
            if (dopStepCounterNext >= dopStep) {
                skipPitchStepping = true;
                break;
            }
        }
        dopStepCounterNext = 0;
        if (!skipPitchStepping) {
            for (int ext = 0; ext <= 30; ++ext) {
                if (!anyEntityOnRay(livingIn, prevRotate.x, prevRotate.y + (pitchPlus = ext * pitchExt * randomIntRange01), rangeCheck)) ++dopStepCounterNext;
                if (dopStepCounterNext >= dopStep) break;
            }
        }
        return Math.abs(yawPlus) <= Math.abs(pitchPlus) ? new Vector2f(prevRotate.x + yawPlus, prevRotate.y) : new Vector2f(prevRotate.x, prevRotate.y + pitchPlus);
    }

    public Vector3d2fRB updateRotationData(String rotationName, Vector2f prevRotation, double rangeRayCastCheck) {
        final Vector3d rotatePointVector3d = UBoxPoints.getBestVector3dOnEntityBox(aura.getTarget());
        final boolean notAnyTopPoint = UBoxPoints.entityBoxVec3dsAlternates(aura.getTarget()).isEmpty();
        return updateRotationData(rotationName, aura.getTarget(), UAura.msCooldownReached(-100) && mc.player.getMotion().y <= .16470125793695456, UAura.msCooldownReached(), notAnyTopPoint, rotatePointVector3d, prevRotation, anyEntityOnRay(aura.getTarget(), prevRotation.x, prevRotation.y, rangeRayCastCheck));
    }

    private Vector3d2fRB updateRotationData(String rotationName, LivingEntity entityTarget, boolean preHitTick, boolean hitTick, boolean behindWall, Vector3d rotateTo, Vector2f rotation, boolean prevRayCasted) {
        final RotateRules rules = asRotationName(rotationName);
        if (rules == null || (rules.isRotOnlyPreHitTick() && rules.isRotOnlyHitTick() ? !preHitTick && !hitTick : (rules.isRotOnlyPreHitTick() && !preHitTick || rules.isRotOnlyHitTick() && !hitTick)) || entityTarget == null || entityTarget.getBoundingBox() == null) return new Vector3d2fRB(rotateTo, rotation, false);
        
        // Специальная логика для SpookyTime Anarchy (на основе предоставленного кода Spookytime)
        boolean isSpookyTimeAnarchy = rotationName.equals("SpookyTime Anarchy");
        boolean isSpookyTimeDuel = rotationName.equals("SpookyTime Duel");
        
        if (isSpookyTimeAnarchy) {
            // Проверка 180-градусного сектора - ротация работает только в фронтальной полусфере
            if (!isInFrontalSector(entityTarget, mc.player.rotationYaw)) {
                return new Vector3d2fRB(rotateTo, rotation, false);
            }
            
            // Инициализация rotateVector если это первый раз или если он был сброшен
            if (rotateVector == null || (Math.abs(rotateVector.x) < 0.01F && Math.abs(rotateVector.y) < 0.01F)) {
                rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
            }
            
            // Плавающая точка внутри хитбокса с предикцией (улучшенная для точности)
            Vector3d targetPos = getLegitHitboxPoint(entityTarget);
            
            Vector3d vec = targetPos.subtract(mc.player.getEyePosition(1.0F));
            
            // Вычисление целевых углов
            float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
            float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))));
            
            // ОГРАНИЧЕНИЕ 180 ГРАДУСОВ: строгое ограничение yaw в пределах фронтальной полусферы
            float yawDelta = MathHelper.wrapDegrees(yawToTarget - rotateVector.x);
            // Если дельта больше 90 градусов в любую сторону, ограничиваем до 90
            if (Math.abs(yawDelta) > 90.0F) {
                yawDelta = yawDelta > 0 ? 90.0F : -90.0F;
            }
            float pitchDelta = MathHelper.wrapDegrees(pitchToTarget - rotateVector.y);
            
            // КЛАМП ДЕЛЬТ с исходными значениями (yaw ~45, pitch ~20-23)
            float clampedYaw = Math.min(Math.abs(yawDelta), 45.0F + (random.nextFloat() * 0.8F));
            float clampedPitch = Math.min(Math.abs(pitchDelta), random.nextFloat() * (23.0F - 20.0F) + 20.0F);
            
            // ФИКС ДЕЛЬТ: yaw и pitch не могут двигаться независимо (правило взаимозависимости)
            if (Math.abs(yawDelta) < 0.01F && Math.abs(pitchDelta) > 0.01F) {
                // Если yaw не двигается, но pitch двигается - добавляем небольшой yaw
                clampedYaw = random.nextFloat() * 0.3F + 0.08F;
                yawDelta = (yawDelta >= 0 ? 1 : -1) * clampedYaw;
            }
            if (Math.abs(pitchDelta) < 0.01F && Math.abs(yawDelta) > 0.01F) {
                // Если pitch не двигается, но yaw двигается - добавляем небольшой pitch
                clampedPitch = random.nextFloat() * 0.3F + 0.08F;
                pitchDelta = (pitchDelta >= 0 ? 1 : -1) * clampedPitch;
            }
            
            // Вычисление целевых углов с учетом направления
            float targetYaw = rotateVector.x + (yawDelta > 0.0F ? clampedYaw : -clampedYaw);
            float targetPitch = rotateVector.y + (pitchDelta > 0.0F ? clampedPitch : -clampedPitch);
            
            // ПРОРЫВНАЯ ТЕХНИКА 1: Адаптивная эмуляция мышечной памяти
            // Анализируем историю движений для создания естественных паттернов
            float yawDeltaMagnitude = Math.abs(yawDelta);
            float pitchDeltaMagnitude = Math.abs(pitchDelta);
            
            // Вычисляем среднюю скорость из истории для эмуляции мышечной памяти
            float avgYawSpeed = calculateAverageMovementSpeed(yawHistory);
            float avgPitchSpeed = calculateAverageMovementSpeed(pitchHistory);
            
            // ПРОРЫВНАЯ ТЕХНИКА 2: Интеллектуальная адаптивная вариативность
            // Адаптируем вариативность на основе паттернов движений
            float currentMovementPattern = (yawDeltaMagnitude + pitchDeltaMagnitude) / 2.0F;
            if (Math.abs(currentMovementPattern - lastMovementPattern) < 0.5F) {
                consecutiveSimilarMovements++;
                // Если движения слишком похожи, увеличиваем вариативность
                if (consecutiveSimilarMovements > 3) {
                    adaptiveVariationFactor = Math.min(adaptiveVariationFactor * 1.15F, 1.5F);
                }
            } else {
                consecutiveSimilarMovements = 0;
                adaptiveVariationFactor = Math.max(adaptiveVariationFactor * 0.95F, 0.85F);
            }
            lastMovementPattern = currentMovementPattern;
            
            // ОПТИМИЗИРОВАННАЯ ПЛАВНОСТЬ: баланс между обходом античитов и регистрацией урона (70-80%)
            // Достаточно плавная для обхода, но достаточно быстрая для регистрации ударов
            float yawLerpBase = 0.70F + (yawDeltaMagnitude / 180.0F) * 0.10F; // 70-80% для баланса
            // Применяем адаптивную вариативность
            float yawVariation = (random.nextFloat() * 0.015F - 0.0075F) * adaptiveVariationFactor;
            float yawLerp = yawLerpBase + yawVariation;
            yawLerp = MathHelper.clamp(yawLerp, 0.70F, 0.80F);
            
            float pitchLerpBase = 0.72F + (pitchDeltaMagnitude / 90.0F) * 0.06F; // 72-80% для баланса
            float pitchVariation = (random.nextFloat() * 0.015F - 0.0075F) * adaptiveVariationFactor;
            float pitchLerp = pitchLerpBase + pitchVariation;
            pitchLerp = MathHelper.clamp(pitchLerp, 0.72F, 0.80F);
            
            // Применяем плавность с учетом истории
            float yaw = rotateVector.x + (targetYaw - rotateVector.x) * yawLerp;
            float pitch = rotateVector.y + (targetPitch - rotateVector.y) * pitchLerp;
            
            // Clamp pitch
            pitch = MathHelper.clamp(pitch, -89.0F, 90.0F);
            
            // ПРОДВИНУТАЯ ТЕХНИКА 1: Точная GCD коррекция для 100% попаданий
            // Идеальная коррекция без ошибок для максимальной точности
            float gcd2 = GCDUtil.getGCDValue();
            float yawGcdCorrection = (yaw - rotateVector.x) % gcd2;
            float pitchGcdCorrection = (pitch - rotateVector.y) % gcd2;
            
            // Точная коррекция без ошибок - килаура должна всегда попадать точно
            yaw -= yawGcdCorrection;
            pitch -= pitchGcdCorrection;
            
            // ПРОДВИНУТАЯ ТЕХНИКА 2: Минимальный накопленный дрифт (уменьшен для регистрации урона)
            // Очень маленькие значения для обхода детекции, не мешающие регистрации
            driftResetCounter++;
            if (driftResetCounter > 15 + random.nextInt(20)) { // Сброс каждые 15-35 тиков
                cumulativeYawDrift = (random.nextFloat() - 0.5F) * 0.25F; // Уменьшено с 0.4F
                cumulativePitchDrift = (random.nextFloat() - 0.5F) * 0.15F; // Уменьшено с 0.25F
                driftResetCounter = 0;
            } else {
                // Постепенное накопление дрифта
                cumulativeYawDrift *= 0.92F; // Постепенное затухание
                cumulativePitchDrift *= 0.92F;
            }
            
            // Применяем минимальный дрифт (уменьшено для регистрации урона)
            yaw += cumulativeYawDrift * 0.10F; // Уменьшено с 0.15F
            pitch += cumulativePitchDrift * 0.10F; // Уменьшено с 0.15F
            
            // ПРОДВИНУТАЯ ТЕХНИКА 3: Улучшенная динамическая коррекция на основе контекста цели
            // Адаптируем ротацию в зависимости от поведения цели с учетом расстояния
            if (entityTarget != null) {
                Vector3d targetMotion = entityTarget.getMotion();
                double targetSpeed = Math.sqrt(targetMotion.x * targetMotion.x + targetMotion.z * targetMotion.z);
                
                // Вычисляем расстояние до цели для адаптивной коррекции
                float currentDistance = (float) mc.player.getDistanceSq(entityTarget);
                if (lastTargetDistance > 0.0F) {
                    distanceChangeRate = (currentDistance - lastTargetDistance) / lastTargetDistance;
                }
                lastTargetDistance = currentDistance;
                
                // Если цель быстро движется, немного увеличиваем предикцию (уменьшено для регистрации)
                if (targetSpeed > 0.15) {
                    float speedFactor = (float) Math.min(targetSpeed / 0.3, 1.0F);
                    // Адаптируем коррекцию в зависимости от изменения расстояния (уменьшено)
                    float distanceFactor = 1.0F + Math.abs(distanceChangeRate) * 0.3F; // Уменьшено с 0.5F
                    yaw += (random.nextFloat() - 0.5F) * 0.10F * speedFactor * distanceFactor; // Уменьшено с 0.15F
                    pitch += (random.nextFloat() - 0.5F) * 0.06F * speedFactor * distanceFactor; // Уменьшено с 0.1F
                }
                
                // ПРОДВИНУТАЯ ТЕХНИКА 4: Минимальная эмуляция реакции на изменение расстояния (уменьшена)
                // При приближении/удалении цели добавляем очень маленькие коррекции
                if (Math.abs(distanceChangeRate) > 0.08F) { // Увеличен порог с 0.05F
                    float reactionStrength = Math.min(Math.abs(distanceChangeRate) * 1.5F, 0.08F); // Уменьшено
                    yaw += (random.nextFloat() - 0.5F) * reactionStrength * 0.4F; // Уменьшено с 0.6F
                    pitch += (random.nextFloat() - 0.5F) * reactionStrength * 0.3F; // Уменьшено с 0.4F
                }
            }
            
            // ПРОДВИНУТАЯ ТЕХНИКА 5: Минимальные коррекции после движения (уменьшены для регистрации)
            // Очень маленькие коррекции, не мешающие точному попаданию
            if (random.nextFloat() < 0.08F && yawDeltaMagnitude > 8.0F) { // Уменьшено с 12% и увеличен порог
                float correctionStrength = 0.03F + random.nextFloat() * 0.05F; // Уменьшено
                
                // Учитываем среднюю скорость из истории
                float avgSpeed = (avgYawSpeed + avgPitchSpeed) / 2.0F;
                if (avgSpeed > 0.15F) { // Увеличен порог
                    correctionStrength *= (1.0F + avgSpeed * 0.2F); // Уменьшено с 0.3F
                }
                
                yaw += (random.nextFloat() - 0.5F) * correctionStrength;
                pitch += (random.nextFloat() - 0.5F) * correctionStrength * 0.4F; // Уменьшено с 0.5F
            }
            
            // ПРОДВИНУТАЯ ТЕХНИКА 6: Минимальные микродвижения (уменьшены для регистрации)
            // Очень маленькие значения, не мешающие точному попаданию
            if (random.nextFloat() < 0.15F) { // Уменьшено с 25% до 15%
                float microMovement = (random.nextFloat() - 0.5F) * 0.05F; // Уменьшено с 0.08F
                yaw += microMovement;
                pitch += microMovement * 0.5F; // Уменьшено с 0.6F
            }
            
            // ПРОДВИНУТАЯ ТЕХНИКА 7: Минимальная адаптивная вариативность (уменьшена для регистрации)
            // Очень маленькие значения, не мешающие точному попаданию
            if (entityTarget != null) {
                Vector3d targetMotion = entityTarget.getMotion();
                double targetSpeed = Math.sqrt(targetMotion.x * targetMotion.x + targetMotion.z * targetMotion.z);
                float complexityFactor = (float) Math.min(targetSpeed * 1.5F + Math.abs(distanceChangeRate) * 2.0F, 1.2F); // Уменьшено
                
                if (complexityFactor > 0.5F) { // Увеличен порог с 0.3F
                    float adaptiveOffset = (random.nextFloat() - 0.5F) * 0.08F * complexityFactor; // Уменьшено с 0.12F
                    yaw += adaptiveOffset;
                    pitch += adaptiveOffset * 0.6F; // Уменьшено с 0.7F
                }
            }
            
            // Обновляем историю движений для следующей итерации
            updateMovementHistory(yaw, pitch);
            
            // Обновление rotateVector
            rotateVector = new Vector2f(yaw, pitch);
            
            // Возврат результата (используем выбранную позицию хитбокса)
            return new Vector3d2fRB(targetPos, rotateVector, true);
        }
        
        if (isSpookyTimeDuel) {
            // Дополнительная рандомизация для обхода Grim паттерн-детекции
            // Специально настроено для Grim (old/new) и MX
            if (random.nextFloat() < 0.15F) { // 15% шанс на дополнительную рандомизацию
                rotateTo = rotateTo.add(randomAroundD2(0.08), randomAroundD2(0.05), randomAroundD2(0.08));
            }
        }
        
        //copy prev rotation for isolate recursion
        rotation = new Vector2f(rotation.x, rotation.y);
        //rules data for rotation args
        Easing easing = rules.getEaseRotate();
        float easingPC = rules.getEaseStrengthPercent();
        double posRandomValue = rules.getVectorPointPosRandom0rec();
        boolean forceTurnAway = rules.isPreHitTurnAway() && preHitTick || rules.isBehindWallTurnAway() && behindWall;
        float forcePullDownPitch = Math.min(rules.getOnHitPullDownPitch090(), 90.F);
        if (forcePullDownPitch > 0.F) {
            float forcePullDownPitchMul = Math.min(mc.player.fallDistance / 1.17F, 1.F);
            forcePullDownPitchMul = (forcePullDownPitchMul > .5F ? 1.F - forcePullDownPitchMul : forcePullDownPitchMul) * 2.F;
            if (easing != null && easingPC > 0.F) forcePullDownPitchMul = MathUtil.lerp(forcePullDownPitchMul, (float) easing.ease(forcePullDownPitchMul), easingPC);
            forcePullDownPitch *= forcePullDownPitchMul;
        }
        float addRandomYaw = rules.getRandRotDragYaw045() == 0.F ? 0.F : randomAroundF2(rules.getRandRotDragYaw045()), addRandomPitch = rules.getRandRotDragPitch030() == 0.F ? 0.F : randomAroundF2(rules.getRandRotDragPitch030());
        boolean forceLockupPitchUpping = rules.isNotDoUpPitchOnFall() && mc.player.fallDistance > 0 && mc.player.fallDistance < 1.3F;
        float speedMul = (prevRayCasted ? Math.max(Math.min(1.F - rules.getSlowSpeedFactorPC01OnAnyEntity(), 1.F), 0.F) : 1.F) * (rules.getRotSpeedRandPC01() > 0.F ? (1.F + randomF(-.33333333F, .5F) * rules.getRotSpeedRandPC01()) : 1.F);
        float mixYawSpeedAndPitchSpeedPC01 = Math.min(rules.getMutualExclusionXYSpeedMul01(), 1.F);
        boolean gcdFix = rules.isGcdFilter();
        //pos randomizer
        if (posRandomValue > 0.F) rotateTo = rotateTo.add(randomAroundD2(posRandomValue), randomAroundD2(posRandomValue), randomAroundD2(posRandomValue));
        //vanilla rot to point
        Vector2f virtRotateVanilla = getVanillaRotate(rotateTo);
        //turn away
        if (forceTurnAway) virtRotateVanilla = getTurnAwayOfRotate(virtRotateVanilla, entityTarget, 6.F, 1);
        //pull down pitch point
        if (forcePullDownPitch > 0.F) virtRotateVanilla.y = Math.min(virtRotateVanilla.y + forcePullDownPitch, 90.F);
        //randomizer yaw & pitch
        if (addRandomYaw != 0.F || addRandomPitch != 0.F) {
            virtRotateVanilla.x += addRandomYaw;
            virtRotateVanilla.y += addRandomPitch;
        }
        //virt rotate wrap
        virtRotateVanilla.x = MathHelper.wrapDegrees(virtRotateVanilla.x);
        //lockup pitch moving to up-side
        if (forceLockupPitchUpping) virtRotateVanilla.y = Math.max(virtRotateVanilla.y, rotation.y);
        //rotations differences
        float yawDiffAtPrev = Math.abs(MathHelper.wrapDegrees(virtRotateVanilla.x - rotation.x)), pitchDiffAtPrev = Math.abs(rotation.y - virtRotateVanilla.y);
        float yawDiffPC01 = Math.min(yawDiffAtPrev / 90.F, 1.F), pitchDiffPC01 = Math.min(pitchDiffAtPrev / 60.F, 1.F);
        float yawDiffWavePC01P = (yawDiffPC01 > 1.F ? 1.F - yawDiffPC01 : yawDiffPC01) * 2.F, pitchDiffWavePC01P = (pitchDiffPC01 > .5F ? 1.F - pitchDiffPC01 : pitchDiffPC01) * 2.F;
        float yawDiffWavePC01N = 1.F - yawDiffWavePC01P, pitchDiffWavePC01N = 1.F - pitchDiffWavePC01P;
        //rotation yaw & pitch speed calc
        float yawSpeedPC01Calc = easing != null ? MathUtil.lerp(yawDiffWavePC01P, (float)easing.ease(yawDiffWavePC01P), easingPC) : yawDiffWavePC01P;
        float pitchSpeedPC01Calc = easing != null ? MathUtil.lerp(pitchDiffWavePC01P, (float)easing.ease(pitchDiffWavePC01P), easingPC) : pitchDiffWavePC01P;
        //mutual exclusion yaw & pitch speed of 0 - 1 percent float
        if (mixYawSpeedAndPitchSpeedPC01 > 0.F) {
            float yawSpeedPC01CalcCopy = yawSpeedPC01Calc;
            yawSpeedPC01Calc *= 1.F - pitchSpeedPC01Calc * mixYawSpeedAndPitchSpeedPC01;
            pitchSpeedPC01Calc *= 1.F - yawSpeedPC01CalcCopy * mixYawSpeedAndPitchSpeedPC01;
        }
        //fully finally rotation speed
        float yawSpeed = MathUtil.lerp(rules.getMinYawSpeed0180(), rules.getMaxYawSpeed0180(), yawSpeedPC01Calc) * speedMul;
        float pitchSpeed = MathUtil.lerp(rules.getMinPitchSpeed090(), rules.getMaxPitchSpeed090(), pitchSpeedPC01Calc) * speedMul;
        //update rotation to target yaw & pitch vector
        final float yawDelta = MathHelper.wrapDegrees(virtRotateVanilla.x - rotation.x);
        final float pitchDelta = virtRotateVanilla.y - mc.player.rotationPitch;
        rotation.x = MathHelper.wrapDegrees(rotation.x + MathUtil.clamp(yawDelta, -yawSpeed, yawSpeed));
        rotation.y = mc.player.rotationPitch + MathUtil.clamp(pitchDelta, -pitchSpeed, pitchSpeed);
        //mouse sensitivity AC check killing after all rots calculations
        if (gcdFix) {
            rotation.x = GCDUtil.getSensitivity(rotation.x);
            rotation.y = GCDUtil.getSensitivity(rotation.y);
        }
        return new Vector3d2fRB(rotateTo, rotation, true);
    }

    public void rotatePrimitiveToSend(Vector2f rotateVector2f) {
        if (rotateVector2f == null) {
            RotationComponent.resetParentTimeout();
            // Сброс rotateVector при отключении ротации
            rotateVector = new Vector2f(0, 0);
            return;
        }
        // Для SpookyTime Anarchy используем более агрессивные параметры обхода
        String rotationMode = aura.rotationModeImpl.getSelected();
        if (rotationMode != null && rotationMode.equals("SpookyTime Anarchy")) {
            // Более высокий приоритет и большая скорость для надежного обхода
            RotationComponent.update(new Rotation(rotateVector2f.x, rotateVector2f.y), 360, 54.2746F, 1, 10);
        } else {
            RotationComponent.update(new Rotation(rotateVector2f.x, rotateVector2f.y), 360, 54.2746F, 1, 5);
        }
    }
    
    // Метод для сброса rotateVector (вызывается при отключении модуля)
    public void resetRotateVector() {
        rotateVector = new Vector2f(0, 0);
    }
    
    // Отведение после удара (легит) - согласно теории
    public void applyPostHitRotation(String rotationType) {
        if (mc.player == null) return;
        
        float currentYaw = mc.player.rotationYaw;
        float currentPitch = mc.player.rotationPitch;
        float yawOffset = 0.0F;
        float pitchOffset = 0.0F;
        
        switch (rotationType) {
            case "30-40°" -> {
                // Отведение на 30-40° (как у PRO PvP-шеров)
                float angle = 30.0F + random.nextFloat() * 10.0F; // 30-40°
                float direction = random.nextFloat() * 360.0F; // Случайное направление
                yawOffset = (float) (Math.cos(Math.toRadians(direction)) * angle);
                pitchOffset = (float) (Math.sin(Math.toRadians(direction)) * angle * 0.3F); // Меньше по pitch
            }
            case "360°" -> {
                // Полный оборот на 360°
                yawOffset = random.nextFloat() * 360.0F - 180.0F; // -180 до +180
                pitchOffset = (random.nextFloat() - 0.5F) * 20.0F; // Небольшое отклонение по pitch
            }
            case "Случайное" -> {
                // Случайное отведение
                yawOffset = (random.nextFloat() - 0.5F) * 80.0F; // -40 до +40
                pitchOffset = (random.nextFloat() - 0.5F) * 30.0F; // -15 до +15
            }
        }
        
        // Применяем отведение с плавной интерполяцией
        float targetYaw = MathHelper.wrapDegrees(currentYaw + yawOffset);
        float targetPitch = MathHelper.clamp(currentPitch + pitchOffset, -89.0F, 90.0F);
        
        // Обновляем rotateVector для плавного перехода
        rotateVector = new Vector2f(targetYaw, targetPitch);
        
        // Применяем ротацию с небольшой задержкой для естественности
        RotationComponent.update(new Rotation(targetYaw, targetPitch), 360, 54.2746F, 1, 5);
    }
}