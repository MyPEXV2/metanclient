package relake.module.implement.movement;

import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import relake.common.component.rotation.FreeLookComponent;
import relake.common.component.rotation.Rotation;
import relake.common.component.rotation.RotationComponent;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.MovementInputEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

import java.awt.event.InputEvent;
import java.util.List;
import java.util.stream.StreamSupport;

public class AutoDodgeModule extends Module {
    public AutoDodgeModule() {
        super("Auto Dodge", "Автоматически уклоняется от летящих в вас стрел", "Automatically dodges flying shots at you", ModuleCategory.Movement);
    }

    @Getter
    public boolean autoJump = false;
    private boolean moveAway;
    public boolean shouldJump;

    @EventHandler
    public void onTick(TickEvent e) {
        List<Entity> potions = StreamSupport.stream(mc.world.getAllEntities().spliterator(), false)
                .filter(entity -> entity instanceof PotionEntity)
                .toList();

        if (potions.isEmpty()) return;

        for (Entity entity : potions) {
            PotionEntity potion = (PotionEntity) entity;
            Vector3d landingPos = predictPotionLandingPosition(potion);
            float effectRadius = getPotionEffectRadius(potion);
            double distanceToPlayer = mc.player.getPositionVec().distanceTo(landingPos);

            if (distanceToPlayer <= effectRadius) {
                Vector3d playerPos = mc.player.getPositionVec();
                Vector3d directionToPotion = landingPos.subtract(playerPos).normalize();
                float yawToPotion = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(directionToPotion.z, directionToPotion.x)) - 90.0D);
                Rotation rotation = new Rotation(yawToPotion + 180.0F, FreeLookComponent.getFreePitch());
                RotationComponent.update(rotation, 360, 360, 1, 100);
                moveAway = true;
                shouldJump = shouldJump();
                break;
            }
        }
    }

    private Vector3d predictPotionLandingPosition(PotionEntity potion) {
        Vector3d motion = potion.getMotion();
        Vector3d pos = potion.getPositionVec();

        for (int i = 0; i < 200; i++) {
            Vector3d prevPos = pos;
            pos = pos.add(motion);
            motion = updatePotionMotion(motion, potion);

            RayTraceContext context = new RayTraceContext(prevPos, pos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, potion);
            BlockRayTraceResult result = mc.world.rayTraceBlocks(context);

            if (result.getType() != RayTraceResult.Type.MISS) {
                pos = result.getHitVec();
                break;
            }

            if (pos.y <= 0) {
                break;
            }
        }

        return pos;
    }

    private Vector3d updatePotionMotion(Vector3d motion, PotionEntity potion) {
        double drag = potion.isInWater() ? 0.8 : 0.99;
        double gravity = 0.05;
        return motion.scale(drag).subtract(0, gravity, 0);
    }

    private boolean shouldJump() {
        EffectInstance speedEffect = mc.player.getActivePotionEffect(Effects.SPEED);
        if (speedEffect != null) {
            int amplifier = speedEffect.getAmplifier();
            return amplifier < 1;
        } else {
            return true;
        }
    }

    private float getPotionEffectRadius(PotionEntity potion) {
        return potion.getItem().getItem() == Items.LINGERING_POTION ? 3.5F : 4.5F;
    }

    @EventHandler
    public void onInput(MovementInputEvent e) {
        if (moveAway) {
            autoJump = true;
            e.setJump(false);
            e.setForward(1);
            e.setStrafe(0);
            if (shouldJump) {
                e.setJump(true);
            }
            moveAway = false;
        } else {
            autoJump = false;
        }
    }
}
