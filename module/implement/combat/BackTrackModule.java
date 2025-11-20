package relake.module.implement.combat;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.module.implement.player.FreeCamModule;
import relake.render.world.Render3D;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.ColorSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.MultiSelectSetting;

import java.awt.*;
import java.util.List;
import java.util.*;

public class BackTrackModule extends Module {
    private final Setting<Float> maxDistance = new FloatSetting("Макс. дистанция").range(3.F, 12.F, .1F).setValue(6.F);
    private final Setting<Float> minDistance = new FloatSetting("Мин. дистанция").range(.0F, 6.F, .1F).setValue(2.7F);

    private final Setting<Float> maxSpeedThreshold = new FloatSetting("Макс. порог скорости сущ.").range(.5F, 3.F, .01F).setValue(1.F);
    private final Setting<Float> minSpeedThreshold = new FloatSetting("Мин. порог скорости сущ.").range(.01F, .3F, .01F).setValue(.08F);

    private final Setting<Float> trackTicksMax = new FloatSetting("Макс. кол.во боксов").range(1.F, 10.F, 1.F).setValue(4.F);

    //
    public final MultiSelectSetting rulesAllImpl = new MultiSelectSetting("Правила работы")
            .setValue("Без элитр",
                    "Не в воде",
                    "Только игроки");

    private final Setting<Boolean> renderTracks = new BooleanSetting("Отображать боксы").setValue(true);
    private final Setting<Color> boxesColor = new ColorSetting("Цвет боксов").setValue(ColorUtil.getColor(31, 133, 255)).setVisible(() -> renderTracks.getValue());

    public BackTrackModule() {
        super("Back track", "Добавляет хитбоксы с тех позиций где игрок или существо находилось до текущего момента, стандартные настройки предусмотрены для античита Matrix", "Adds hitboxes from the positions where the player or the creature was before the current moment, standard settings are provided for the Matrix anticheat", ModuleCategory.Combat);
        registerComponent(maxDistance, minDistance, maxSpeedThreshold, minSpeedThreshold, trackTicksMax, rulesAllImpl, renderTracks, boxesColor);
        rulesAllImpl.getSelected().add("Без элитр");
    }

    private PlayerEntity self;
    final List<Integer> trackableEntitiesId = new ArrayList<Integer>();
    final HashMap<Integer, PreviousTicksEntityTracker> tracks = new HashMap<>();

    @EventHandler
    public void onTick(TickEvent event) {
        //setup self entity
        final FreeCamModule freeCamInstance = Client.instance.moduleManager.freeCamModule;
        this.self = freeCamInstance.isEnabled() ? freeCamInstance.fakePlayer : mc.player;
        if (this.self == null) return;
        //update trackable entities collection
        this.trackableEntitiesId.clear();
        if (mc.world != null) {
            final float range = this.maxDistance.getValue(), minDistance = this.minDistance.getValue();
            this.trackableEntitiesId.addAll(mc.world.loadedLivingEntityList().stream().filter(base -> (!this.rulesAllImpl.isSelected("Только игроки") || base instanceof PlayerEntity) && base != mc.player && base != this.self && base.ticksExisted > 1 && base.isAlive() && base.getHealth() > 0 && base.getDistance(this.self) <= range && base.getDistance(this.self) >= minDistance && (this.tracks.get(base.getEntityId()) != null || this.hasEntityMove(base)) && (!this.rulesAllImpl.isSelected("Без элитр") || !base.isElytraFlying()) && (!this.rulesAllImpl.isSelected("Не в воде") || !(base.isInWater() || base.isInLava() || base.isMaterialInBB(Material.WEB) || mc.world.getBlockState(new BlockPos(base.getPosX(), base.getPosY() - .12F, base.getPosZ())).getMaterial().isLiquid())) && !Client.instance.friendManager.isFriend(base.getNotHidedName().getString())).map(Entity::getEntityId).toList());
        }
        //update trackers map
        for (final int entityId : this.trackableEntitiesId) {
            if (!this.tracks.containsKey(entityId))
                this.tracks.put(entityId, new PreviousTicksEntityTracker(this.trackTicksMax.getValue().intValue(), entityId));
        }
        List<Integer> rems = new ArrayList<>();
        if (!tracks.isEmpty()) for (final Integer entityId : tracks.keySet()) {
            final PreviousTicksEntityTracker tracker = tracks.get(entityId);
            if (tracker.removeIf()) {
                rems.add(entityId);
                continue;
            }
            tracker.setMemoryTicks(this.trackTicksMax.getValue().intValue());
            tracker.updatePrevs();
        }
        for (int rem : rems) tracks.remove(rem);
    }

    @Override
    public void onDisable() {
        this.tracks.clear();
    }

    @Override
    public void onEnable() {
        this.tracks.clear();
    }

    @EventHandler
    public void onRenderWorld(WorldRenderEvent event) {
        if (!this.tracks.isEmpty() && renderTracks.getValue()) {
            final float partialTicks = event.getTicks();
            for (final Integer entityId : trackableEntitiesId) {
                final PreviousTicksEntityTracker tracker = tracks.get(entityId);
                if (tracker == null || tracker.getAxises().isEmpty()) continue;
                Render3D.setup3dForBlockPos(event, () -> {
                    tracker.getAxises().forEach(axis -> {
                        final int color = ColorUtil.multAlpha(boxesColor.getValue().getRGB(), axis.getAlphaPC(partialTicks) / 1.5F);
                        if (ColorUtil.getAlphaFromColor(color) >= 1) {
                            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                            final AxisAlignedBB aabb = axis.getAabb().grow(-.02D);
                            GL11.glLineWidth(.01F);
                            Render3D.drawGradientAlphaBox(event.getStack(), BUFFER, TESSELLATOR, aabb, false, true, 0, ColorUtil.multDark(color, .081F));
                            GL11.glLineWidth(.01F);
                            Render3D.drawCanisterBox(event.getStack(), BUFFER, TESSELLATOR, aabb, true, false, false, ColorUtil.multDark(color, .25F), ColorUtil.multDark(color, .081F), 0);
                            GL11.glLineStipple(1, Short.reverseBytes((short) 16));
                            GL11.glEnable(GL11.GL_LINE_STIPPLE);
                            GL11.glEnable(GL11.GL_LINE_SMOOTH);
                            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                            GL11.glLineWidth(5.F);
                            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                            Render3D.drawCanisterBox(event.getStack(), BUFFER, TESSELLATOR, aabb, true, true, false, color, 0, 0);
                            GL11.glDisable(GL11.GL_LINE_STIPPLE);
                            GL11.glDisable(GL11.GL_LINE_SMOOTH);
                            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
                            GL11.glLineWidth(1.F);
                        }
                    });
                }, true, true);
            }
        }
    }

    public List<AxisAlignedBB> getTracksAsEntity(Entity entity, AxisAlignedBB defaultAxis, boolean sorted) {
        if (entity == null) return null;
        if (this == null) return Collections.singletonList(defaultAxis);
        final List<AxisAlignedBB> list = new ArrayList<>();
        list.add(defaultAxis);
        final PreviousTicksEntityTracker tracker = tracks.get(entity.getEntityId());
        if (tracker != null) list.addAll(tracker.getAxisesToBoxes());
        if (sorted) list.sort(Comparator.comparing(obj -> getVecAsAxis(obj).distanceTo(this.self.getPositionVec())));
        return list;
    }


    //UTILS//
    private boolean hasEntityMove(Entity entity) {
        double dx = Math.abs(entity.getPosX() - entity.lastTickPosX),
                dy = Math.abs(entity.getPosY() - entity.lastTickPosY),
                dz = Math.abs(entity.getPosZ() - entity.lastTickPosZ);
        double sqrt = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return sqrt >= this.minSpeedThreshold.getValue() && sqrt <= this.maxSpeedThreshold.getValue();
    }


    private class TickedAxis {
        private final AxisAlignedBB aabb;
        private int ticks;
        private final int ticksMax;

        public TickedAxis(AxisAlignedBB aabb, int ticksAlive) {
            this.aabb = aabb;
            this.ticksMax = this.ticks = ticksAlive;
        }

        public void update() {
            --this.ticks;
        }

        public float getAlphaPC(float partialTicks) {
            float value = Math.max((this.ticks + 1.F - partialTicks) / (float) this.ticksMax, 0.F);
            value = (value > .5F ? 1.F - value : value) * 2.F;
            return (float) Easings.CUBIC_OUT.ease(value);
        }

        public boolean removeIf() {
            return this.ticks <= 0;
        }

        public AxisAlignedBB getAabb() {
            return this.aabb;
        }
    }

    private Vector3d getVecAsAxis(AxisAlignedBB axis) {
        return new Vector3d(axis.minX + (axis.maxX - axis.minX) / 2.D, axis.minY, axis.minZ + (axis.maxZ - axis.minZ) / 2.D);
    }

    //Tracker
    private class PreviousTicksEntityTracker {
        private int memoryTicks;
        private final int entityId;
        private List<TickedAxis> axisList = new ArrayList<>();

        public PreviousTicksEntityTracker(int memoryTicks, int entityId) {
            this.memoryTicks = memoryTicks;
            this.entityId = entityId;
        }

        public void setMemoryTicks(int memoryTicks) {
            this.memoryTicks = memoryTicks;
        }

        private LivingEntity getEntity() {
            final Entity entity = mc.world.getEntityByID(this.entityId);
            if (entity instanceof LivingEntity base) return base;
            return null;
        }

        public void updatePrevs() {
            final LivingEntity trackableEntity = this.getEntity();
            if (trackableEntity != null && hasEntityMove(trackableEntity)) {
                if (axisList == null) axisList = new ArrayList<>();
                axisList.add(new TickedAxis(trackableEntity.getRenderBoundingBox(), this.memoryTicks));
            }
            axisList.removeIf(TickedAxis::removeIf);
            axisList.forEach(TickedAxis::update);
        }

        public List<TickedAxis> getAxises() {
            return this.axisList;
        }

        public List<AxisAlignedBB> getAxisesToBoxes() {
            return this.axisList.stream().map(TickedAxis::getAabb).toList();
        }

        public boolean removeIf() {
            final LivingEntity entity = this.getEntity();
            return entity == null || !entity.isAlive() || entity.getHealth() <= 0.F || axisList.size() < 2 && !hasEntityMove(entity) || Client.instance.friendManager.isFriend(entity.getNotHidedName().getString());
        }
    }
}