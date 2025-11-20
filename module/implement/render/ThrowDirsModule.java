package relake.module.implement.render;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.common.util.ChatUtil;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

import java.util.*;
import java.util.stream.Collectors;

public class ThrowDirsModule extends Module {
    public ThrowDirsModule() {
        super("Throw Dirs", "Рисует траекторию броска или выстрела текущего предмета в руках существ", "Draws the trajectory of a throw or shot of a current object in the hands of creatures", ModuleCategory.Render);
    }

    private final Random rand = new Random();

    private boolean itemIsCorrectToThrow(Item itemIn) {
        return itemIn instanceof BowItem || itemIn instanceof SnowballItem || itemIn instanceof EggItem || itemIn instanceof EnderPearlItem || itemIn instanceof SplashPotionItem || itemIn instanceof LingeringPotionItem || itemIn instanceof FishingRodItem;
    }

    private class ItemStackWithHand {
        private final ItemStack stack;
        private final Hand hand;

        private ItemStackWithHand(ItemStack stack, Hand hand) {
            this.stack = stack;
            this.hand = hand;
        }

        public ItemStack getItemStack() {
            return this.stack;
        }

        public Hand getHand() {
            return this.hand;
        }
    }

    private ItemStackWithHand getCorrectThrowStackOfEntity(LivingEntity entityOf) {
        return entityOf == null ? null : itemIsCorrectToThrow(entityOf.getHeldItemMainhand().getItem()) ? new ItemStackWithHand(entityOf.getHeldItemMainhand(), Hand.MAIN_HAND) : itemIsCorrectToThrow(entityOf.getHeldItemOffhand().getItem()) ? new ItemStackWithHand(entityOf.getHeldItemOffhand(), Hand.OFF_HAND) : null;
    }

    public RayTraceResult traceBlock(Vector3d startVec, Vector3d endVec, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode) {
        return mc.world.rayTraceBlocks(new RayTraceContext(
                startVec,
                endVec,
                blockMode,
                fluidMode,
                mc.player)
        );
    }

    private final double[] randoms = new double[]{0, 0, 0};
    private final double[] prevRandoms = new double[]{0, 0, 0};

    private List<Vector3d> getPointsOfThrowable(World worldIn, ItemStackWithHand handStack, float partialTicks, LivingEntity entityOf, int maxDensity) {
        List<Vector3d> vecs = new ArrayList();
        if (handStack != null && entityOf != null && worldIn != null && worldIn.isBlockLoaded(entityOf.getPosition().down((int) entityOf.getPosY() - 1))) {
            Item item = handStack.getItemStack().getItem();
            if (this.itemIsCorrectToThrow(item)) {
                boolean calcTight = item instanceof BowItem;
                if (!calcTight || entityOf.isHandActive() && entityOf.getActiveItemStack() != null && entityOf.getActiveItemStack().getItem() instanceof BowItem) {
                    float throwFactor = calcTight ? 1.F : .4F;
                    double[] selfHeadRotateWR = new double[]{entityOf.prevRotationYawHead + (entityOf.rotationYawHead - entityOf.prevRotationYawHead) * partialTicks,
                            entityOf.prevRotationPitchHead + (entityOf.rotationPitchHead - entityOf.prevRotationPitchHead) * partialTicks,
                            Math.toRadians(entityOf.prevRotationYawHead + (entityOf.rotationYawHead - entityOf.prevRotationYawHead) * partialTicks),
                            Math.toRadians(entityOf.prevRotationPitchHead + (entityOf.rotationPitchHead - entityOf.prevRotationPitchHead) * partialTicks)};
                    Vector3d playerVector = new Vector3d(
                            entityOf.lastTickPosX + (entityOf.getPosX() - entityOf.lastTickPosX) * partialTicks,
                            entityOf.lastTickPosY + (entityOf.getPosY() - entityOf.lastTickPosY) * partialTicks + entityOf.getEyeHeight(),
                            entityOf.lastTickPosZ + (entityOf.getPosZ() - entityOf.lastTickPosZ) * partialTicks);
                    double offsetStartDir = handStack.getHand() == Hand.MAIN_HAND ? 1.D : -1.D;
                    double throwOfX = playerVector.getX() - Math.cos(selfHeadRotateWR[2]) * .1D * offsetStartDir,
                            throwOfY = playerVector.getY(),
                            throwOfZ = playerVector.getZ() - Math.sin(selfHeadRotateWR[2]) * .1D * offsetStartDir;
                    double shiftX = (-Math.sin(selfHeadRotateWR[2]) * Math.cos(selfHeadRotateWR[3]) + (entityOf instanceof PlayerEntity player && player.abilities.isFlying ? 0 : entityOf.getPosX() - entityOf.lastTickPosX)) * throwFactor,
                            shiftY = (-Math.sin(selfHeadRotateWR[3]) + (entityOf instanceof PlayerEntity player && player.abilities.isFlying ? 0 : (entityOf == mc.player ? MathUtil.lerp(entityOf.getPosY() - entityOf.lastTickPosY, entityOf.getMotion().y, partialTicks) : .0D))) * throwFactor,
                            shiftZ = (Math.cos(selfHeadRotateWR[2]) * Math.cos(selfHeadRotateWR[3]) + (entityOf instanceof PlayerEntity player && player.abilities.isFlying ? 0 : entityOf.getPosZ() - entityOf.lastTickPosZ)) * throwFactor,
                            throwMotion = Math.sqrt(shiftX * shiftX + shiftY * shiftY + shiftZ * shiftZ);
                    if (entityOf == mc.player) {
                        shiftX += MathUtil.lerp(this.prevRandoms[0], this.randoms[0], partialTicks);
                        shiftY += MathUtil.lerp(this.prevRandoms[1], this.randoms[1], partialTicks);
                        shiftZ += MathUtil.lerp(this.prevRandoms[2], this.randoms[2], partialTicks);
                    }
                    shiftX /= throwMotion;
                    shiftY /= throwMotion;
                    shiftZ /= throwMotion;
                    if (calcTight) {
                        float tightPower = (72000.F - entityOf.getItemInUseCount() + partialTicks) / 20.F;
                        tightPower = (tightPower * tightPower + tightPower * 2.F) / 3.F;
                        tightPower = tightPower < .1F ? 1.F : tightPower > 1.F ? 1.F : tightPower;
                        tightPower *= 3.F;

                        shiftX *= tightPower;
                        shiftY *= tightPower;
                        shiftZ *= tightPower;
                    } else {
                        shiftX *= 1.5D;
                        shiftY *= 1.5D;
                        shiftZ *= 1.5D;
                    }
                    double gravityFactor = calcTight ? .005D : item instanceof PotionItem ? .04D : item instanceof FishingRodItem ? .015D : .003D;
                    while (maxDensity > 0) {
                        vecs.add(new Vector3d(throwOfX, throwOfY, throwOfZ));
                        throwOfX += shiftX * .1D;
                        throwOfY += shiftY * .1D;
                        throwOfZ += shiftZ * .1D;
                        double asellate = 1 - .001D;
                        shiftX *= asellate;
                        shiftY = shiftY * asellate - gravityFactor;
                        shiftZ *= asellate;
                        if (traceBlock(playerVector, new Vector3d(throwOfX, throwOfY, throwOfZ), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE).getType() != RayTraceResult.Type.MISS) break;
                        --maxDensity;
                    }
                }
            }
        }
        return vecs;
    }

    private List<Vector3d> getThowingVecsListOfEntity(LivingEntity entityFor) {
        return this.getPointsOfThrowable(mc.world, this.getCorrectThrowStackOfEntity(entityFor), mc.getRenderPartialTicks(), entityFor, 500);
    }

    private void start3DRendering(WorldRenderEvent event, boolean ignoreDepth) {
        event.getStack().push();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (ignoreDepth)
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        else GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glDepthMask(false);
        GL11.glPointSize(3.F);
        GL11.glLineWidth(.75F);
        //GL11.glDisable(GL11.GL_LIGHTING);
        event.getStack().translate(-mc.getRenderManager().renderPosX(), -mc.getRenderManager().renderPosY(), -mc.getRenderManager().renderPosZ());
    }

    private void end3DRendering(WorldRenderEvent event) {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.F);
        GL11.glPointSize(1.F);
        GL11.glDepthMask(true);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        event.getStack().pop();
    }

    private void renderLineBegin(WorldRenderEvent event, List<Vector3d> vecs, int color1, int color2, float alphaPC, float alphaPass) {
        final int max = vecs.size();
        alphaPC *= MathUtil.clamp(max / 50.F, 0, 1);
        if (alphaPC == 0 || max < 2) return;
        color1 = ColorUtil.multAlpha(color1, alphaPC);
        color2 = ColorUtil.multAlpha(color2, alphaPC);
        BUFFER.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        int index = 0, lastColor = 0;
        Vector3d last = null;
        for (final Vector3d vec : vecs) {
            final float pcOfStartVec = index / (float) max, pcOfMiddleVec = Math.abs(index - max / 2) / (max / 2.F);
            float darkPass = (1 - pcOfMiddleVec * alphaPass);
            final int currentColor = lastColor = ColorUtil.getOverallColorFrom(color1, color2, pcOfStartVec), finalColor = ColorUtil.multAlpha(currentColor, darkPass);
            BUFFER.pos(event.getStack().getLast().getMatrix(), (float) vec.getX(), (float) vec.getY(), (float) vec.getZ()).color(ColorUtil.multDark(finalColor, darkPass)).endVertex();
            ++index;
            last = vec;
        }
        TESSELLATOR.draw();
        if (lastColor != 0 && last != null) {
            if (ColorUtil.getAlphaFromColor(lastColor) < 60) lastColor = ColorUtil.replAlpha(lastColor, 60);
            BUFFER.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
            BUFFER.pos(event.getStack().getLast().getMatrix(), (float) last.getX(), (float) last.getY(), (float) last.getZ()).color(lastColor).endVertex();
            TESSELLATOR.draw();
        }
    }

    private List<LivingEntity> toRenderEntities = new ArrayList();

    @EventHandler
    public void onTickEvent(TickEvent event) {
        if (!this.toRenderEntities.isEmpty() && mc.player != null && !this.toRenderEntities.isEmpty()) {
            this.prevRandoms[0] = this.randoms[0];
            this.prevRandoms[1] = this.randoms[1];
            this.prevRandoms[2] = this.randoms[2];
            this.randoms[0] = this.rand.nextGaussian() * .007499999832361937D;
            this.randoms[1] = this.rand.nextGaussian() * .007499999832361937D;
            this.randoms[2] = this.rand.nextGaussian() * .007499999832361937D;
        }
    }

    @EventHandler
    public void onRender3WorldD(WorldRenderEvent event) {
        if ( mc.world == null) return;
        this.toRenderEntities = mc.world.loadedLivingEntityList().stream().filter(living -> living.isAlive()).collect(Collectors.toList());
        if (this.toRenderEntities.isEmpty()) return;
        final List<List<Vector3d>> pageVecLists = new ArrayList();
        for (final LivingEntity entity : this.toRenderEntities) {
            final List<Vector3d> vecsList = this.getThowingVecsListOfEntity(entity);
            if (!vecsList.isEmpty()) pageVecLists.add(vecsList);
        }
        if (!pageVecLists.isEmpty()) {
            final int color1 = -1, color2 = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
            final float alphaPass = 0.9F;
            this.start3DRendering(event, false);
            pageVecLists.forEach(list -> {
                if (list.size() > 1 && list.get(0).distanceTo(list.get(list.size() - 1)) > 1.D)
                    this.renderLineBegin(event, list, color1, color2, 1.F, alphaPass);
            });
            this.end3DRendering(event);
        }
    }
}
