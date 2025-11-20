package relake.module.implement.combat;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import relake.Client;
import relake.common.util.MathUtil;
import relake.common.util.MoveUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.player.PlayerEvent;
import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.MultiSelectSetting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AutoPotionModule extends Module {
    public boolean fastThrow = false;

    public final MultiSelectSetting selectPotions = new MultiSelectSetting("Выбор")
            .setValue("Сила",
                    "Скорость",
                    "Регенерация",
                    "Огнестойкость",
                    "Исцеление");

    private final Setting<Float> hpHealPot = new FloatSetting("Кидать хилку при хп").range(4.F, 20.F, .5F).setValue(13.F).setVisible(() -> selectPotions.isSelected("Исцеление"));

    private final Setting<Float> ticksAfterSpawn = new FloatSetting("Тики после спавна").range(0.F, 200.F, 5.F).setValue(10.F);
    private final Setting<Boolean> onlySneakPitch = new BooleanSetting("Только шифтом в пол").setValue(false);
    private final Setting<Boolean> disableOnThrow = new BooleanSetting("Выключать при броске").setValue(false);

    public AutoPotionModule() {
        super("Auto Potion", "Автоматически применяет кидающиеся зелья с положительными эффектами", "Automatically applies throwing potions with positive effects", ModuleCategory.Combat);
        registerComponent(selectPotions, hpHealPot, ticksAfterSpawn, onlySneakPitch, disableOnThrow);

        selectPotions.getSelected().add("Сила");
        selectPotions.getSelected().add("Скорость");
        selectPotions.getSelected().add("Огнестойкость");
    }

    private final StopWatch stopWatch = new StopWatch();

    public boolean isCallThrow() {
        return (callThrow || fastThrow) && this.isEnabled();
    }

    private boolean posBlock(double x, double y, double z) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement();
    }

    private enum Potions {STRENGTH, SPEED, FIRERES, INSTANT_HEALTH,REGENERATION}

    private boolean isStackPotion(ItemStack stack, Potions potion) {
        if (stack == null) {return false;}
        Item item = stack.getItem();
        if (item == Items.SPLASH_POTION) {
            int id = 5;
            switch (potion) {
                case STRENGTH -> id = 5;
                case SPEED -> id = 1;
                case FIRERES -> id = 12;
                case INSTANT_HEALTH -> id = 6;
                case REGENERATION -> id = 10;
            }
            for (EffectInstance effect : PotionUtils.getEffectsFromStack(stack))
                if (effect.getPotion() == Effect.get(id))
                    return true;
        }
        return false;
    }

    private int getPotionSlot(Potions potion) {
        for (int i = 8; i >= 0; --i)
            if (isStackPotion(mc.player.inventory.getStackInSlot(i), potion)) return i;
        return -1;
    }

    private int getPotionId(Potions potion) {
        return potion == Potions.STRENGTH ? 5 : potion == Potions.SPEED ? 1 : potion == Potions.FIRERES ? 12 : potion == Potions.REGENERATION ? 10 : 6;
    }

    private boolean isActivePotion(Potions potion) {
        final int id = getPotionId(potion);
        final Effect eff = Effect.get(id);
        return mc.player != null && (id == 6 ? !(selectPotions.isSelected("Исцеление") && mc.player.getHealth() + mc.player.getAbsorptionAmount() < hpHealPot.getValue() && !mc.player.isCreative()) : mc.player.isPotionActive(eff) && mc.player.getActivePotionEffect(eff).getDuration() > 10);
    }

    private List<Potions> potsListToThrow() {
        return potsList().stream().filter(inList -> getPotionSlot(inList) != -1 && !isActivePotion(inList)).collect(Collectors.toList());
    }

    private List<Potions> potsList() {
        final List<Potions> potions = new ArrayList<>();
        if (selectPotions.isSelected("Сила")) potions.add(Potions.STRENGTH);
        if (selectPotions.isSelected("Скорость")) potions.add(Potions.SPEED);
        if (selectPotions.isSelected("Регенерация")) potions.add(Potions.REGENERATION);
        if (selectPotions.isSelected("Огнестойкость")) potions.add(Potions.FIRERES);
        if (selectPotions.isSelected("Исцеление")) potions.add(Potions.INSTANT_HEALTH);
        return potions;
    }

    private double[] s;
    private boolean g;
    private void throwingALL(final PlayerEvent event, StopWatch stopWatch, long timeWaitMS) {
        final List<Potions> throwList = potsListToThrow();
        if (throwList.size() > 0 && stopWatch.finished(timeWaitMS)) {
            float[] rotate = getRotate(posToRotate(), MoveUtil.moveYaw(event.getRotate().x));
            s = new double[] {event.getPos().x,event.getPos().y,event.getPos().z,rotate[0],rotate[1]};
            g = event.isOnGround();
            event.setRotate(new Vector2f(rotate[0], rotate[1]));
            if  (mc.player.ticksExisted < ticksAfterSpawn.getValue().intValue() + 5) {
                new CPlayerPacket.RotationPacket(rotate[0], rotate[1], event.isOnGround()).send();
            }
            callThrow = true;
            stopWatch.reset();
        }
    }

    private float[] setRotationsToVec3d(Vector3d vec) {
        double diffX = vec.x - mc.player.getEyePosition(1).x;
        double diffY = vec.y - mc.player.getEyePosition(1).y;
        double diffZ = vec.z - mc.player.getEyePosition(1).z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) ((float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90));
        float pitch = (float) ((float) (-Math.toDegrees(Math.atan2(diffY, diffXZ))));
        pitch = MathUtil.clamp(pitch, -90, 90);
        return new float[]{yaw, pitch};
    }

    private double getXVec(Vector3d vec) {
        return vec != null ? vec.x : 0;
    }

    private double getYVec(Vector3d vec) {
        return vec != null ? vec.z : 0;
    }

    private double getZVec(Vector3d vec) {
        return vec != null ? vec.z : 0;
    }

    private Vector3d posToRotate() {
        List<Vector3d> nonAirList = new ArrayList<>();
        float xzR = 1.15f;
        int yrP = 2;
        int yrM = 1;
        double xMe = mc.player.getPosX();
        double yMe = mc.player.getPosY();
        double zMe = mc.player.getPosZ();
        assert mc.world != null;
        for (double x = xMe - xzR;x < xMe + xzR;x += 1f) {
            for (double z = zMe - xzR;z < zMe + xzR;z += 1f) {
                for (double y = yMe + yrP;y > yMe - yrM;y -= 1f) {
                    final BlockPos pos = new BlockPos(x,y,z);
                    if (mc.world.isAirBlock(pos) || mc.world.getBlockState(pos).getMaterial().isReplaceable()) continue;
                    nonAirList.add(new Vector3d(pos.getX() + .5,pos.getY(),pos.getZ() + .5));
                }
            }
        }
        if (nonAirList != null && nonAirList.size() > 1)
            nonAirList.sort(Comparator.comparing(nonAir -> -mc.player.getDistanceToCoord(getXVec(nonAir), getYVec(nonAir) + mc.player.getEyeHeight() * .8D, getZVec(nonAir))));
        return nonAirList == null || nonAirList.size() == 0 ? null : nonAirList.get(0);
    }

    private float calculatePitch() {
        double dx = mc.player.getPosX() - mc.player.lastTickPosX, dz = mc.player.getPosZ() - mc.player.lastTickPosZ;
        double speed = Math.sqrt(dx * dx + dz * dz);
        float max = 178 - 89;
        double dy = mc.player.getPosY() - mc.player.lastTickPosY;
        double delta = MathUtil.clamp(Math.sqrt(speed * speed) / 0.55f, 0, 0.5) - MathUtil.clamp((Math.abs(Math.sqrt(dy * dy)) * 9F), 0, 0.333333);
        return (float) (89 - max * MathUtil.clamp(delta * 1.5F,0,1));
    }

    private float[] getRotate(Vector3d to,float oldYaw) {
        return to == null ? (posBlock(mc.player.getPosX(), mc.player.getPosY() + 2.4999f, mc.player.getPosZ()) ? new float[] {oldYaw, -89} : new float[] {oldYaw, calculatePitch()}) : setRotationsToVec3d(to);
    }

    private boolean canThrow(int minTicksAlive,boolean onlySNP) {
        return (((mc.player.collidedHorizontally || mc.player.collidedVertically || mc.player.isOnGround()) && (!onlySNP || (mc.player.isSneaking() && mc.player.rotationPitch > 85))) || fastThrow) && !(mc.player.isHandActive() && (mc.player.getActiveHand() != null && mc.player.getActiveHand().equals(Hand.MAIN_HAND)) || Client.instance.moduleManager.freeCamModule.isEnabled() || mc.player.isElytraFlying() || mc.playerController.getIsHittingBlock() || mc.player.ticksExisted < minTicksAlive);
    }

    private static boolean callThrow;

    @EventHandler
    public void onUpdate(final PlayerEvent event) {
        if (callThrow) {
            final List<Potions> throwList = potsListToThrow();
            mc.player.rotationYawHead = (float)s[3];
            mc.player.renderYawOffset = (float)s[3];
            mc.player.rotationPitchHead = (float)s[4];
            int lastSlot = mc.player.inventory.currentItem;
            int handSlot = mc.player.inventory.currentItem;
            for (final Potions toThrow : throwList) {
                int potionSlot = getPotionSlot(toThrow);
                if (potionSlot != -1) {
                    mc.player.inventory.currentItem = potionSlot;
                    if (lastSlot != potionSlot) {
                        mc.playerController.syncCurrentPlayItem();
                        lastSlot = potionSlot;
                    }
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                }
            }
            mc.player.inventory.currentItem = handSlot;
            if (lastSlot != handSlot) mc.playerController.syncCurrentPlayItem();
            callThrow = false;
            if (disableOnThrow.getValue() && throwList.isEmpty()) this.disable();
        }
        if (canThrow(ticksAfterSpawn.getValue().intValue(), onlySneakPitch.getValue())) {
            long delay = mc.player.rotationPitchHead < -58 ? 300 : MoveUtil.isMoving() ? 750 : 500;
            throwingALL(event, stopWatch, delay);
            fastThrow = false;
        }
    }
    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        //при сносе тотема ускоряет реакцию
        if (event.getPacket() instanceof SEntityStatusPacket status && mc.player != null && status.entityId == mc.player.getEntityId() && status.getOpCode() == 35) fastThrow = true;
    }

    @Override
    public void onDisable() {
        fastThrow = false;
        super.onDisable();
    }
}
