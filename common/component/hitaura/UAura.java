package relake.common.component.hitaura;

import lombok.Getter;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import relake.Client;
import relake.common.component.TargetComponent;
import relake.common.util.ChatUtil;
import relake.common.util.RayTraceUtil;
import relake.common.util.StopWatch;

import java.security.SecureRandom;
import java.util.Arrays;

public class UAura {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Minecraft mc = Minecraft.getInstance();
    public static long hitCounterCPSBypass;
    public static void hitCounterCPSBypassNext() {++hitCounterCPSBypass;}
    public static void hitCounterCPSBypassReset() {hitCounterCPSBypass = 0;}
    public static boolean cpsBypassTrigger() {return hitCounterCPSBypass % 7 == 3;}

    public static PlayerEntity getSelf() {return mc.player;}

    public static World getWorld() {return mc.world;}

    public static float applyGaussianJitter(float rotation) {
        final float strength = .2F;
        return (float) (rotation + (secureRandom.nextGaussian() * strength * 2.F - strength));
    }

    public static boolean randomBoolean(int chance010) {return secureRandom.nextInt(chance010 + 1) > 50;}
    public static boolean randomBoolean() {return randomBoolean(50);}
    public static float randomFloat(float min, float max) {return secureRandom.nextFloat(min, max);}
    public static float randomFloat() {return randomFloat(.0F, 1.F);}
    public static int randomInt1PosibleOrNot() {return randomBoolean() ? 1 : -1;}

    public static int getAxeSlot() {
        for (int i = 0; i < 9; i++) if (mc.player.inventory.mainInventory.get(i).getItem() instanceof AxeItem) return i;
        return -1;
    }

    public static Runnable[] hitShieldBreakTaskForUse(LivingEntity livingIn, boolean enabled) {
        final Runnable[] pre$post = new Runnable[] {() -> {}, () -> {}};
        if (!enabled) return pre$post;
        if (livingIn instanceof PlayerEntity player && Math.abs(MathHelper.wrapDegrees(mc.player.rotationYaw - player.rotationYaw - 180)) > 90) {
            final ItemStack main = player.getHeldItemMainhand(), off = player.getHeldItemOffhand();
            if (main != null && off != null) {
                final Item mainItem = main.getItem(), offItem = off.getItem();
                if (mainItem == Items.SHIELD || offItem == Items.SHIELD) {
                    final int slot, handSlot = mc.player.inventory.currentItem;
                    if ((slot = getAxeSlot()) != -1 && slot != handSlot) {
                        pre$post[0] = () -> new CHeldItemChangePacket(slot).sendSilent();
                        pre$post[1] = () -> new CHeldItemChangePacket(handSlot).sendSilent();
                    }
                }
            }
        }
        return pre$post;
    }

    public static Runnable[] resetShieldSilentTaskForUse(boolean enabled) {
        final Runnable[] pre$post = new Runnable[] {() -> {}, () -> {}};
        if (!enabled) return pre$post;
        if (mc.player.isActiveItemStackBlocking()) {
            Hand active = mc.player.getActiveHand();
            if (active == null) return pre$post;
            pre$post[0] = () -> new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN).sendSilent();
            pre$post[1] = () -> new CPlayerTryUseItemPacket(active).sendSilent();
        }
        return pre$post;
    }

    public static Runnable[] skipSilentSprintingTaskForUse(boolean enabled) {
        final Runnable[] pre$post = new Runnable[] {() -> {}, () -> {}};
        if (!enabled) return pre$post;
        // Сбрасываем спринт только в воздухе, чтобы не мешать ударам на земле
        if (mc.player.isServerSprintState() && !mc.player.isOnGround() && !mc.player.areEyesInFluid(FluidTags.WATER)) {
            pre$post[0] = () -> {
                mc.player.setSprinting(false);
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            };
            pre$post[1] = () -> {
                // Восстанавливаем спринт только если игрок не на земле (чтобы не мешать ударам)
                if (!mc.player.isOnGround()) {
                    mc.player.setSprinting(true);
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                }
            };
        }
        return pre$post;
    }
    
    // МАКСИМАЛЬНО МОЩНЫЙ сброс спринта для режима "БОМБА" (оптимизирован для регистрации урона)
    public static Runnable[] bombSprintResetTaskForUse(LivingEntity target, double[] ranges) {
        final Runnable[] pre$post = new Runnable[] {() -> {}, () -> {}};
        if (target == null) return pre$post;
        
        double distance = Math.sqrt(mc.player.getDistanceSq(target.getPosX(), target.getPosY(), target.getPosZ()));
        double maxRange = ranges[0] + ranges[1];
        
        // Агрессивный сброс спринта, но только в воздухе (чтобы не мешать ударам на земле)
        if (distance < maxRange * 1.5 && !mc.player.isOnGround() && !mc.player.areEyesInFluid(FluidTags.WATER)) {
            pre$post[0] = () -> {
                // Сброс спринта для максимального обхода
                mc.player.setSprinting(false);
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            };
            pre$post[1] = () -> {
                // Восстанавливаем спринт только в воздухе
                if (!mc.player.isOnGround()) {
                    mc.player.setSprinting(true);
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                }
            };
        }
        return pre$post;
    }

    public static double getYCapacityOnPlayerPos(int rangeY) {
        if (mc.world == null) return 1.D;
        Vector3d eyePos = mc.player.getEyePosition(mc.getRenderPartialTicks());
        double minDst = rangeY * 2.D, dst;
        double maxY = 255, minY = -64;
        final float selfWD2 = mc.player.getWidth() / 2.F - 1E-2F;
        RayTraceResult first, second;
        for (final Vector3d vec : Arrays.asList(eyePos.add(-selfWD2, .0D, -selfWD2), eyePos.add(selfWD2, .0D, selfWD2), eyePos.add(selfWD2, .0D, -selfWD2), eyePos.add(-selfWD2, .0D, selfWD2))) {
            first = mc.world.rayTraceBlocks(new RayTraceContext(vec, vec.add(0.D, -rangeY, 0.D), RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.ANY, mc.player));
            second = mc.world.rayTraceBlocks(new RayTraceContext(vec, vec.add(0.D, rangeY, 0.D), RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.ANY, mc.player));
            if (maxY > second.getHitVec().y) maxY = second.getHitVec().y;
            if (minY < first.getHitVec().y) minY = first.getHitVec().y;
            dst = maxY - minY;
            if (minDst > dst) minDst = dst;
        }
        return minDst - mc.player.getHeight();
    }

    public static double convenientFallOffset() {
        double fallOffset = mc.player.fallDistance;
        if (mc.world != null && !mc.player.isOnGround() && mc.player.getMotion().y < -.0784000015258789D && !mc.world.getBlockState(mc.player.getPosition()).getMaterial().isLiquid() && !mc.world.getBlockState(mc.player.getPosition().up()).getMaterial().isLiquid()) {
            if (mc.player.fallDistance < -mc.player.getMotion().y && mc.player.ticksOnGround > 6) fallOffset = -mc.player.getMotion().y;
        }
        return fallOffset;
    }

    public static boolean isBestMomentToHit(boolean fallCheck) {
       if (!fallCheck) return true;
            float adaptiveFallValue = 0.F, maxFallOff = .2F;
            if (cpsBypassTrigger() && UAura.getYCapacityOnPlayerPos(2) > .20000004768371582D) {
                adaptiveFallValue = maxFallOff;
            }
            final boolean hasFall = UAura.convenientFallOffset() > adaptiveFallValue || UAura.getYCapacityOnPlayerPos(2) < .1F;
            if (hasFall) return true;
                final boolean badLiquidMoment = !mc.player.movementInput.jump && (mc.player.isInWater() || mc.player.isInLava()) || mc.player.areEyesInFluid(FluidTags.WATER) || mc.player.areEyesInFluid(FluidTags.LAVA) || mc.player.isMaterialInBB(Material.WEB);
                final boolean skipFallCheck =
                badLiquidMoment ||
                (!mc.player.movementInput.jump && mc.player.ticksOnGround > 6) && Client.instance.moduleManager.attackAuraModule.groundHitting.getValue() ||
                mc.player.isOnLadder() ||
                mc.player.isElytraFlying() ||
                mc.player.isRidingHorse() ||
                mc.player.isPotionActive(Effects.BLINDNESS) || mc.player.isPotionActive(Effects.LEVITATION) || mc.player.isPotionActive(Effects.SLOW_FALLING) ||
                mc.player.abilities.isFlying ||
                mc.player.getPosY() < -64.D;
            return skipFallCheck;
    }

    public static boolean useEntity(LivingEntity livingIn, Runnable preHit, Runnable postHit, Hand hand, boolean cpsBypass) {
        if (preHit != null) preHit.run();
        if (livingIn != null) {
            assert mc.playerController != null;
            mc.playerController.attackEntity(mc.player, livingIn);
            if (cpsBypass) hitCounterCPSBypassNext();
            else hitCounterCPSBypassReset();
            cooldownTimer.reset();
            if (hand != null) mc.player.swingArm(hand);
        }
        if (postHit != null) postHit.run();
        return livingIn != null;
    }

    @Getter
    private static final StopWatch cooldownTimer = new StopWatch();

    public static long getMsCooldown() {
        long msCooldown;
        if (Client.instance.moduleManager.attackAuraModule.cooldownMode.isSelected("1.9 или выше")) {
            double attributeAttackSpeed = mc.player.getAttributeValue(Attributes.ATTACK_SPEED);
            float maxDeviation = .2F;
            msCooldown = (long) ((1.F / attributeAttackSpeed) * 1000.F * (1.F - Math.min(maxDeviation, 1.F)));
            //low hand
            if (attributeAttackSpeed == 4.D || attributeAttackSpeed == 4.4000000059604645 || attributeAttackSpeed == 4.800000011920929)
                msCooldown = 450L;
            msCooldown = Math.max(msCooldown, 450L);
        } else msCooldown = 45L;
        if (UAura.cpsBypassTrigger()) msCooldown += 50L;
        return msCooldown;
    }
    public static boolean msCooldownReached(long msOffset) {return cooldownTimer.finished(getMsCooldown() + msOffset);}
    public static boolean msCooldownReached() {return msCooldownReached(0);}
    public static boolean msCooldownHasMs(long ms) {return cooldownTimer.finished(ms);}
    public static float msCooldownPC01() {return Math.min(cooldownTimer.elapsedTime() / (float) getMsCooldown(), 1.F);}

    public static boolean anyEntityOnRay(LivingEntity livingIn, double range) {
        return livingIn != null && RayTraceUtil.isViewEntity(livingIn, MathHelper.wrapDegrees(mc.player.rotationYaw), mc.player.rotationPitch, (float) range, true);
    }

    public static boolean shouldAttack(LivingEntity livingTarget, boolean rayCast, boolean fallCheck, long cooldownMSOffset, double[] ranges) {
        //dst
        if (livingTarget != null && TargetComponent.getLookRayDistance(livingTarget) > ranges[0]) return false;

        //cooldown
        if (!UAura.msCooldownReached(cooldownMSOffset)) return false;

        //best moment
        boolean validNext = UAura.isBestMomentToHit(fallCheck);

        //ray cast rule
        if (validNext && rayCast && !anyEntityOnRay(livingTarget, ranges[2])) validNext = false;

        return validNext;
    }

    public static boolean cancelSprintTick(LivingEntity targetIn, double[] ranges, String stopSprintMode) {
        switch (stopSprintMode) {
            case "Никогда" -> {return false;}
            case "Перед ударом" -> {
                // ОПТИМИЗИРОВАННЫЙ сброс спринта - не мешает регистрации урона
                if (targetIn != null && shouldAttack(targetIn, false, false, -50L, ranges)) {
                    // Сбрасываем спринт только в воздухе для обхода, но не на земле (чтобы не мешать ударам)
                    if (!mc.player.isOnGround() && !mc.player.areEyesInFluid(FluidTags.WATER)) {
                        double motionY = mc.player.getMotion().y;
                        // Точное условие для сброса спринта перед ударом
                        if (motionY <= .0030162615090425808) {
                            return true;
                        }
                    }
                }
            }
            case "Задолго до удара", "Сильно" -> {
                if (targetIn != null && shouldAttack(targetIn, false, false, -150L, ranges)) {
                    if (!mc.player.isOnGround() && !mc.player.areEyesInFluid(FluidTags.WATER)) {
                        if (mc.player.getMotion().y <= .16477328182606651) return true;
                    }
                }
            }
            case "БОМБА" -> {
                // МАКСИМАЛЬНО АГРЕССИВНЫЙ сброс спринта - работает всегда когда есть цель
                if (targetIn != null) {
                    double distance = mc.player.getDistanceSq(targetIn.getPosX(), targetIn.getPosY(), targetIn.getPosZ());
                    // Сбрасываем спринт на любой дистанции до цели
                    if (distance < ranges[2] * ranges[2]) {
                        // Сбрасываем спринт даже на земле для максимального обхода
                        if (mc.player.isSprinting() || mc.player.isServerSprintState()) {
                            return true;
                        }
                    }
                    // Дополнительно сбрасываем при движении к цели
                    if (!mc.player.isOnGround() && !mc.player.areEyesInFluid(FluidTags.WATER)) {
                        return true;
                    }
                    // Сбрасываем даже на земле если цель близко
                    if (distance < (ranges[0] + ranges[1]) * (ranges[0] + ranges[1])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean missDetected;
    private static int counterTo0PostMissHits;
    private static int maxHitsCountOnMiss() {
        return 3;
    }
    public static void antiMissesHittingReset() {
        missDetected = false;
        counterTo0PostMissHits = 0;
    }
    public static void antiMissesHittingUpdate(LivingEntity targetIn, boolean cpsBypass, boolean rayCastCheck, boolean enabled) {
        if (targetIn == null || counterTo0PostMissHits == 0 || !enabled || targetIn.hurtTime != 0) antiMissesHittingReset();
        if (enabled && targetIn != null && UAura.msCooldownHasMs(cpsBypassTrigger() ? 250 : 150) && mc.player.isSwingInProgress) {
            if (!missDetected && counterTo0PostMissHits == 0 && targetIn.hurtTime == 0) {
                missDetected = true;
                counterTo0PostMissHits = maxHitsCountOnMiss();
            }
            if (missDetected && counterTo0PostMissHits > 0 && targetIn != null) {
                if ((!rayCastCheck || anyEntityOnRay(targetIn, 6.F)) && useEntity(targetIn, () -> {}, () -> {}, Hand.MAIN_HAND, cpsBypass)) --counterTo0PostMissHits;
            }
        }
    }
}
