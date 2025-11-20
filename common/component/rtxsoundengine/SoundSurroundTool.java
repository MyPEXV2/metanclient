package relake.common.component.rtxsoundengine;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.optifine.BlockPosM;
import org.lwjgl.opengl.GL11;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;

import java.util.*;

public class SoundSurroundTool {
    private final Minecraft mc;
    private ClientPlayerEntity player;
    public ClientPlayerEntity getPlayer() {
        return player;
    }
    public void setPlayer(ClientPlayerEntity player) {
        this.player = player;
    }
    private boolean tooPerfomance;
    public boolean isTooPerfomance() {
        return tooPerfomance;
    }
    public void setTooPerfomance(boolean tooPerfomance) {
        this.tooPerfomance = tooPerfomance;
    }
    private boolean rtxDebug = true;
    public boolean isRtxDebug() {
        return rtxDebug;
    }
    public void setRtxDebug(boolean rtxDebug) {
        this.rtxDebug = rtxDebug;
    }
    private Vector3d headPosition;
    public Vector3d getHeadPosition() {
        return headPosition;
    }
    public void setHeadPosition(Vector3d headPosition) {
        this.headPosition = headPosition;
    }
    private Vector3d lastHeadPosition;
    public Vector3d getLastHeadPosition() {
        return lastHeadPosition;
    }
    public void setLastHeadPosition(Vector3d lastHeadPosition) {
        this.lastHeadPosition = lastHeadPosition;
    }
    protected SoundSurroundTool() {
        this.mc = Minecraft.getInstance();
    }

    public static SoundSurroundTool build() {
        return new SoundSurroundTool();
    }

    public float[] getGainArgsFromWorld() {
        return this.getGainArgsFromWorld(this.getPlayer());
    }

    public float[] getGainArgsFromWorld(ClientPlayerEntity player) {
        if (player != null) {
            final boolean isPerfomance = this.isTooPerfomance();
            final double xPos = player.getPosX(), yPos = player.getPosY(), eyeHeight = player.getEyeHeight(), zPos = player.getPosZ();
            this.setHeadPosition(new Vector3d(xPos, yPos + eyeHeight, zPos));
            this.setLastHeadPosition(new Vector3d(player.prevPosX, player.prevPosY + eyeHeight, player.prevPosZ));
            final int floorX = (int) xPos, floorY = (int) yPos, floorEyeY = floorY + (int) eyeHeight, floorZ = (int) zPos;
            final int worldMaxY = getWorldYLevel(floorX, floorZ);
            final Vector2f rayResults = overallRandomRaySignValues(xPos, yPos + eyeHeight, zPos, isPerfomance);
            double liquidDepthPC = getLiquidDepth(xPos, yPos + eyeHeight, zPos, isPerfomance),
                    atmospheric = getAtmosphericPressure(xPos, yPos + eyeHeight, zPos, worldMaxY, isPerfomance),
                    skyFactor = rayResults.y / 2.26D,
                    isolationAndSize = rayResults.x;
            final boolean isOpaquePlayer = hasOpaquePushing(player, xPos, yPos, zPos, isPerfomance);
            float echoPC = 2.F;
            float echo = (float) (isolationAndSize - (isolationAndSize * skyFactor)) * echoPC;
            echo = echo > 1.F ? 1.F : Math.max(echo, 0.F);
            float rev = echo / (float) (.01F + Math.min(skyFactor / (.01F + isolationAndSize), 1.F));
            rev = rev > 1.F ? 1.F : Math.max(rev, 0.F);
            float vol = (float) (1.F - atmospheric - liquidDepthPC / 2.4F);
            vol = Math.max(vol, .5F);
            float push = 1.F - (float) Math.min(liquidDepthPC * 1.7F + atmospheric * (1.F - echo), 1.F) / 1.108F;
            push = Math.max(push, 0.F);
            echo *= .35F + .65F * push * push;
            if (isOpaquePlayer) {
                echo /= 6.F;
                rev *= 1.5F;
                push -= isPerfomance ? .35F : .8F;
                push = Math.max(push, 0.F);
            }
            return new float[]{echo, rev, vol, push};
        }
        return new float[]{0.F, 0.F, 1.F, 1.F};
    }

    //utils
    private int getWorldYLevel(int x, int z) {
        if (mc.world == null) return 0;
        final Chunk chunk = mc.world.getChunk(x >> 4, z >> 4);
        if (chunk.isLoaded()) return chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).getHeight(x & 15, z & 15);
        else return 0;
    }

    private double getLiquidDepth(double x, double y, double z, boolean perfomance) {
        return getLiquidDepth(x, y, z, perfomance ? 9 : 18, perfomance ? 3 : 1, perfomance);
    }
    public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
        int j2 = MathHelper.floor(bb.minX);
        int k2 = MathHelper.ceil(bb.maxX);
        int l2 = MathHelper.floor(bb.minY);
        int i3 = MathHelper.ceil(bb.maxY);
        int j3 = MathHelper.floor(bb.minZ);
        int k3 = MathHelper.ceil(bb.maxZ);
        BlockPosM blockpos$pooledmutableblockpos = new BlockPosM();
        for (int l3 = j2; l3 < k2; ++l3) {
            for (int i4 = l2; i4 < i3; ++i4) {
                for (int j4 = j3; j4 < k3; ++j4) {
                    blockpos$pooledmutableblockpos.setXyz(l3, i4, j4);
                    if (mc.world.getBlockState(blockpos$pooledmutableblockpos).getMaterial() == materialIn) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private double getLiquidDepth(double x, double y, double z, int maxDepth, int blockStep, boolean perfomance) {
        if (mc.world == null) return 0.D;
        double value = 0.D;
        if (perfomance) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = mc.world.getBlockState(pos);
            if (state.getMaterial().isLiquid()) {
                double factorY = 1.D + y - (int) y;
                for (int y1 = (int) y; y1 < y + maxDepth; y1 += blockStep) {
                    state = mc.world.getBlockState(pos = pos.up());
                    if (state.getMaterial().isLiquid()) ++factorY;
                }
                value = factorY / (float) maxDepth;
                return value;
            }
        } else {
            AxisAlignedBB aabb = new AxisAlignedBB(x - .3D, y, z - .3D, x + .3D, y + 1E-14D, z + .3D);
            if (isMaterialInBB(aabb, Material.WATER)) {
                double factorY = 1.D + y - (int) y;
                for (int y1 = (int) y; y1 < y + maxDepth; y1 += blockStep) {
                    aabb = aabb.offset(0.D, 1.D, 0.D);
                    if (isMaterialInBB(aabb, Material.WATER) || isMaterialInBB(aabb, Material.LAVA))
                        ++factorY;
                    else if (isMaterialInBB(aabb, Material.WEB)) factorY += .15D;
                }
                value = factorY / (float) maxDepth;
            }
            final double editValue = 1.D - Math.pow(1.D - Math.min(value, 1.D), 3.D);
            if (!Double.isNaN(editValue)) value = editValue;
        }
        return value;
    }

    private double getAtmosphericPressure(double x, double y, double z, int heightMapMaxY, boolean perfomance) {
        if (mc.world == null) return 0.D;
        double factor = 0.D;
        if (!perfomance) {
            if (mc.world.isRaining()) factor += mc.world.getRainStrength(mc.getRenderPartialTicks()) * .2F;
            final Biome.Category biome = mc.world.getBiome(new BlockPos(x, y, z)).getCategory();
            if (biome == Biome.Category.OCEAN || biome == Biome.Category.ICY || biome == Biome.Category.BEACH || biome == Biome.Category.TAIGA)
                factor += .2F;
            else if (biome == Biome.Category.NETHER) factor += .3D;
            factor += .17F * MathHelper.clamp((MathHelper.clamp(heightMapMaxY, 56, 72) - y) / 48.D, 0.D, 1.D);
        } else if (y < 48) factor += (y / 30.D) * .15D;
        return Math.min(factor, 1.D);
    }

    private final Random RAND = new Random();
    private final List<Vector2f> listOfChangesRays = new ArrayList<>();
    private final List<RTVec> listOfTestVecs = new ArrayList<>();
    public List<RTVec> getListOfTestVecs() {
        return this.listOfTestVecs;
    }
    private static class RTVec {
        private Vector3d pos;
        private float dstPC;
        public RTVec(Vector3d pos, float dstPC) {
            this.pos = pos;
            this.dstPC = dstPC;
        }
        public Vector3d getPos() {
            return pos;
        }
        public float getDstPC() {
            return dstPC;
        }
        public double getSortValue() {
            return -getDstPC();
        }
    }

    private Vector2f overallRandomRaySignValues(double x, double y, double z, boolean perfomance) {
        return overallRandomRaySignValues(x, y, z, perfomance ? 90 : 250, 56, perfomance ? 400 : 700, perfomance);
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
    private Vector2f overallRandomRaySignValues(double x, double y, double z, int raysCount, int maxRayLength, long changeTimeWithAnoise, boolean perfomance) {
        if (mc.world == null) return new Vector2f(0.F, 0.F);
        Vector3d pos = new Vector3d(x, y, z), randRay;
        int misses = 0;
        double value = 0.D;
        RayTraceResult ray;
        float a = 0.F, b = 0.F;
        for (int rayIndex = 0; rayIndex < raysCount; rayIndex++) {
            randRay = new Vector3d(-1.F + RAND.nextDouble(2.F), Math.min(-.5F + RAND.nextFloat(1.5F), 1.F), -1.F + RAND.nextFloat(2.F)).scale(maxRayLength);
            ray = traceBlock(pos, pos.add(randRay), RayTraceContext.BlockMode.COLLIDER, perfomance ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
            if (ray == null || ray.getType() == RayTraceResult.Type.MISS)
                ++misses;
            else if (ray.getType() == RayTraceResult.Type.BLOCK) {
                value += ray.getHitVec().distanceTo(pos) * 2.666666666666F * 2.F;
                if (isRtxDebug())
                    listOfTestVecs.add(new RTVec(ray.getHitVec(), (float) Math.min(ray.getHitVec().distanceTo(pos) / (float) maxRayLength, 1.F)));
            }
        }
        Vector2f result = new Vector2f((float) value / (float) raysCount, misses);
        listOfChangesRays.add(result);
        while (listOfChangesRays.size() > changeTimeWithAnoise / 50.F) listOfChangesRays.remove(0);
        while (isRtxDebug() && listOfTestVecs.size() > changeTimeWithAnoise / 50.F * maxRayLength)
            listOfTestVecs.remove(0);
        for (final Vector2f vec : listOfChangesRays) {
            a += vec.x;
            b += vec.y;
        }
        if (a != 0.F || b != 0.F) {
            a = a / (float) listOfChangesRays.size();
            b = b / (float) listOfChangesRays.size();
        }
        return new Vector2f(a / (float) raysCount, b / (float) raysCount);
    }

    private boolean hasOpaquePushing(ClientPlayerEntity player, double x, double y, double z, boolean perfomance) {
        if (mc.world == null) return false;
        else {
            if (perfomance) {
                return mc.world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement();
            } else {
                final double eyeHeight = player.getEyeHeight(), expandY = .075D, expandXZ = .165D;
                return !mc.world.getCollisionShapes(player, new AxisAlignedBB(x - expandXZ, y + eyeHeight - expandY, z - expandXZ, x + expandXZ, y + eyeHeight + expandY, z + expandXZ)).toList().isEmpty();
            }
        }
    }

    private boolean hasLight(int x, int y, int z) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(new BlockPos(x, y, z)).getLightValue() != 0;
    }
    public void draw3dTest(MatrixStack matrix, double transX, double transY, double transZ) {
        if (!isRtxDebug() || getHeadPosition() == null && getLastHeadPosition() == null) return;
        matrix.push();
        matrix.translate(-transX, -transY, -transZ);
        Tessellator tessellator = Tessellator.getInstance();
        float pTicks = mc.getRenderPartialTicks();
        Vector3d headRenderPos = new Vector3d(
            MathUtil.lerp(getLastHeadPosition().x, getHeadPosition().x, pTicks),
            MathUtil.lerp(getLastHeadPosition().y, getHeadPosition().y, pTicks),
            MathUtil.lerp(getLastHeadPosition().z, getHeadPosition().z, pTicks)
        );
        GlStateManager.clearCurrentColor();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glLineWidth(.25F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        tessellator.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        listOfTestVecs.forEach(vec -> {
            final int c = ColorUtil.getOverallColorFrom(ColorUtil.getColor(255, 70, 70), ColorUtil.getColor(70, 255, 70), vec.getDstPC());
            tessellator.getBuffer().pos(matrix.getLast().getMatrix(), (float) headRenderPos.x, (float) headRenderPos.y, (float) headRenderPos.z).color(ColorUtil.replAlpha(ColorUtil.multDark(c, .8F), 3)).endVertex();
            tessellator.getBuffer().pos(matrix.getLast().getMatrix(), (float) vec.getPos().x, (float) vec.getPos().y, (float) vec.getPos().z).color(ColorUtil.replAlpha(c, (int) (26F + 80F * vec.getDstPC()))).endVertex();
        });
        tessellator.draw();
        if (!this.isTooPerfomance()) {
            final List<TripleRTVector3d> tripleRTVector3ds = getSortLimitedToTriangles(listOfTestVecs);
            if (!tripleRTVector3ds.isEmpty()) {
                tripleRTVector3ds.forEach(tripleRTVec -> {
                    tessellator.getBuffer().begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                    tripleRTVec.getRTVecs().forEach(rtVec -> tessellator.getBuffer().pos(matrix.getLast().getMatrix(), (float) rtVec.getPos().x, (float) rtVec.getPos().y, (float) rtVec.getPos().z).color(ColorUtil.replAlpha(ColorUtil.getColor(30, 30, 255), (int) (30.F + 40.F * rtVec.getDstPC()))).endVertex());
                    tessellator.draw();
                });
            }
        }
        GL11.glLineWidth(1.F);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glPointSize(.25F);
        tessellator.getBuffer().begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
        tessellator.getBuffer().pos(matrix.getLast().getMatrix(), (float) headRenderPos.x, (float) headRenderPos.y, (float) headRenderPos.z).color(-1).endVertex();
        listOfTestVecs.forEach(vec -> {
            final int c = ColorUtil.getOverallColorFrom(ColorUtil.getColor(255, 70, 70), ColorUtil.getColor(70, 255, 70), vec.getDstPC());
            tessellator.getBuffer().pos(matrix.getLast().getMatrix(), (float) vec.getPos().x, (float) vec.getPos().y, (float) vec.getPos().z).color(ColorUtil.replAlpha(c, 255)).endVertex();
        });
        tessellator.draw();
        GL11.glPointSize(27.F);
        tessellator.getBuffer().begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
        tessellator.getBuffer().pos(matrix.getLast().getMatrix(), (float) headRenderPos.x, (float) headRenderPos.y, (float) headRenderPos.z).color(ColorUtil.getColor(60, 60, 255, 20)).endVertex();
        listOfTestVecs.forEach(vec -> {
            final int c = ColorUtil.getOverallColorFrom(ColorUtil.getColor(255, 70, 70), ColorUtil.getColor(70, 255, 70), vec.getDstPC());
            tessellator.getBuffer().pos(matrix.getLast().getMatrix(), (float) vec.getPos().x, (float) vec.getPos().y, (float) vec.getPos().z).color(ColorUtil.replAlpha(c, 2)).endVertex();
        });
        tessellator.draw();
        GL11.glPointSize(1.F);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.clearCurrentColor();
        GlStateManager.enableDepthTest();
        matrix.pop();
    }

    private static class TripleRTVector3d {
        private final RTVec rt0, rt1, rt2;
        public TripleRTVector3d(RTVec rt0, RTVec rt1, RTVec rt2) {
            this.rt0 = rt0;
            this.rt1 = rt1;
            this.rt2 = rt2;
        }
        public RTVec getRt0() {
            return this.rt0;
        }
        public RTVec getRt1() {
            return this.rt1;
        }
        public RTVec getRt2() {
            return this.rt2;
        }
        public double getSortValue() {
            return -(rt0.getDstPC() + rt1.getDstPC() + rt2.getDstPC());
        }
        public List<RTVec> getRTVecs() {
            return Arrays.stream(new RTVec[]{getRt0(), getRt1(), getRt2()}).toList();
        }
    }

    private List<TripleRTVector3d> getSortLimitedToTriangles(List<RTVec> rayVecs) {
        List<TripleRTVector3d> tempRTGroups = new ArrayList<>();
        rayVecs = rayVecs.stream().sorted(Comparator.comparingDouble(RTVec::getSortValue)).toList();
        int indexOfRt = 0;
        for (final RTVec rtVec : rayVecs) {
            if (indexOfRt % 3 == 2)
                tempRTGroups.add(new TripleRTVector3d(rtVec, rayVecs.get(indexOfRt - 1), rayVecs.get(indexOfRt - 2)));
            ++indexOfRt;
        }
        tempRTGroups = tempRTGroups.stream().sorted(Comparator.comparingDouble(TripleRTVector3d::getSortValue)).toList();
        return tempRTGroups;
    }

}
