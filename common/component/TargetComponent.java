package relake.common.component;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import relake.Client;
import relake.common.InstanceAccess;
import relake.common.component.hitaura.UBoxPoints;
import relake.common.util.AuraUtil;
import relake.common.util.ChatUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.module.implement.combat.AttackAuraModule;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TargetComponent implements InstanceAccess {
    private static final List<LivingEntity> entityList = new CopyOnWriteArrayList<>();
    private int countLoadedEntities;

    public TargetComponent() {
        Client.instance.eventManager.register(this);
    }

    @EventHandler
    public void tick(TickEvent event) {
        if (countLoadedEntities != mc.world.getCountLoadedEntities()) {
            updateTargetList();
            countLoadedEntities = mc.world.getCountLoadedEntities();
        }
    }

    private void updateTargetList() {
        entityList.clear();
        entityList.addAll(mc.world.loadedLivingEntityList()
                .stream()
                .filter(entity -> entity != mc.player)
                .toList());
    }

    public static List<LivingEntity> getTargets(final double range) {
        return entityList.stream().filter(entity -> getLookRayDistance(entity) <= range && mc.world.loadedLivingEntityList().contains(entity)).filter(ENTITY_FILTER).collect(Collectors.toList());
    }

    public static List<Entity> getTargets(final double range, Predicate<Entity> predicate) {
        return entityList.stream().filter(entity -> getLookRayDistance(entity) <= range && mc.world.loadedLivingEntityList().contains(entity)).filter(predicate).collect(Collectors.toList());
    }

    public static LivingEntity getTarget(final double range) {
        return find(range).orElse(null);
    }

    public static LivingEntity getStableTarget(final double range, LivingEntity lastTarget, boolean stable) {
        return findStable(range, lastTarget, stable).orElse(null);
    }

    private static final Predicate<LivingEntity> ENTITY_FILTER = TargetComponent::isValid;

    public static boolean isValid(final LivingEntity entity) {
        final AttackAuraModule attackAuraModule = Client.instance.moduleManager.attackAuraModule;
        if (entity == null) return false;
        if (entity.getHealth() <= 0 || !entity.isAlive() || entity.equals(mc.player)) return false;
        if (entity instanceof ArmorStandEntity) return false;
        if (!attackAuraModule.targets.isSelected("Невидимые") && entity.isInvisible()) return false;
        if (entity instanceof PlayerEntity player) {
            if (!attackAuraModule.targets.isSelected("Игроки")) return false;
            if (!attackAuraModule.targets.isSelected("Голые") && player.isNaked()) return false;
            if (Client.instance.friendManager.isFriend(player.getNotHidedName().getString())) return false;
        }
        if ((entity instanceof AnimalEntity || entity instanceof MobEntity) && !attackAuraModule.targets.isSelected("Мобы")) return false;
        if (!attackAuraModule.ignoreWalls.getValue() && UBoxPoints.entityBoxVec3dsAlternates(entity).isEmpty()) return false;

        return true;
    }

    private static Optional<LivingEntity> find(final double range) {
        List<LivingEntity> validTargets = getTargets(range);
        final AttackAuraModule attackAuraModule = Client.instance.moduleManager.attackAuraModule;
        if (validTargets.isEmpty()) return Optional.empty();
        if (attackAuraModule.sortMode.isSelected("Всему")) {
            validTargets.sort(Comparator.comparingDouble(TargetComponent::compareArmor).thenComparingDouble(TargetComponent::getEntityHealth).thenComparingDouble(entity -> AuraUtil.getVector(entity).length()));
        } else if (attackAuraModule.sortMode.isSelected("Дистанции")) {
            validTargets.sort(Comparator.comparingDouble(TargetComponent::getLookRayDistance).thenComparingDouble(TargetComponent::getEntityHealth));
        } else if (attackAuraModule.sortMode.isSelected("Дистанции")) {
            validTargets.sort(Comparator.comparingDouble(TargetComponent::getEntityHealth).thenComparingDouble(TargetComponent::getLookRayDistance));
        }
        return Optional.of(validTargets.get(0));
    }

    private static Optional<LivingEntity> findStable(final double range, LivingEntity finded, boolean stable) {
        return stable && finded != null && mc.world.loadedLivingEntityList().contains(finded) && getLookRayDistance(finded) <= range && isValid(finded) ? Optional.of(finded) : find(range);
    }

    private static double compareArmor(LivingEntity entity) {
        return (entity instanceof PlayerEntity player) ? -getEntityArmor(player) : -entity.getTotalArmorValue();
    }

    private static double getEntityArmor(PlayerEntity entity) {
        double totalArmor = 0.0;

        for (ItemStack armorStack : entity.inventory.armorInventory) {
            if (armorStack != null && armorStack.getItem() instanceof ArmorItem) {
                totalArmor += getProtectionLvl(armorStack);
            }
        }
        return totalArmor;
    }

    private static double getEntityHealth(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            double armorValue = getEntityArmor(player) / 20.0;
            return (player.getHealthFixed() + player.getAbsorptionAmount()) * armorValue;
        } else if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.getHealthFixed() + livingEntity.getAbsorptionAmount();
        }
        return 0.0;
    }

    private static double getProtectionLvl(ItemStack stack) {
        double damageReduce = 0.0;
        if (stack.getItem() instanceof ArmorItem armor) {
            damageReduce = armor.getDamageReduceAmount();
            if (stack.isEnchanted()) {
                damageReduce += (double) EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) * 0.25;
            }
        }
        return damageReduce;
    }

    public static double getLookRayDistance(Entity toEntity) {
        final double dstSqrt = mc.player.getDistance(toEntity);
        if (dstSqrt < 9.D) {
            final double rayDistance = UBoxPoints.getBestVector3dOnEntityBox(toEntity).distanceTo(mc.player.getEyePosition(mc.getRenderPartialTicks()));
            if (rayDistance < dstSqrt) return rayDistance;
        }
        return dstSqrt;
    }
}
