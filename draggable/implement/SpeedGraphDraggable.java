package relake.draggable.implement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import relake.Client;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.draggable.Draggable;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.font.TextRenderer;
import relake.render.display.shape.ShapeRenderer;
import relake.util.ScaleUtil;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.mojang.blaze3d.systems.RenderSystem.enableBlend;

public class SpeedGraphDraggable extends Draggable {
    public SpeedGraphDraggable() {
        super("SpeedGraph", 100, 170, 150, 60);
    }

    private void setScaledSize() {
        if (Client.instance.moduleManager.speedGraphModule.graphSize.isSelected("Меньше")) {
            width = 145;
            height = 60;
        } else if (Client.instance.moduleManager.speedGraphModule.graphSize.isSelected("Стандартно")) {
            width = 170;
            height = 75;
        } else {
            width = 190;
            height = 105;
        }
    }

    private float pixelDensity() {
        if (Client.instance.moduleManager.speedGraphModule.graphSize.isSelected("Меньше")) return 7.5F;
        else if (Client.instance.moduleManager.speedGraphModule.optimizeLevel.isSelected("Сбалансированно")) return 3.F;
        return 1.F;
    }

    private final Animation speedLimitAnim = new Animation();

    private float speedLimit(boolean update) {
        if (Client.instance.moduleManager.speedGraphModule.adaptiveSensitivity.getValue()) {
            if (update) {
                speedLimitAnim.update();
                speedLimitAnim.run(MathUtil.clamp(maxBps, .7F, 10.F), .03F, Easings.QUAD_IN_OUT);
            }
            return speedLimitAnim.get();
        }
        return .8F;
    }

    private float speedMoveTrigger() {
        return .0F;
    }

    private int getColorBloom(float aPC) {
        return ColorUtil.multAlpha(Client.instance.moduleManager.hudModule.color.getValue().getRGB(), aPC);
    }

    private int maxCountCalc() {
        return (int) ((width - renderOffsetsGraph() * 2.F) / pixelDensity());
    }

    private float renderOffsetsGraph() {
        return 3.F;
    }

    @Override
    public boolean visible() {
        return Client.instance.moduleManager.speedGraphModule.isEnabled();
    }

    @Override
    public void tick() {
    }

    private float getSelfSpeed() {
        double mx = mc.player.getPosX() - mc.player.lastTickPosX;
        double mz = mc.player.getPosZ() - mc.player.lastTickPosZ;
        return (float) Math.sqrt(mx * mx + mz * mz) * mc.timer.speed;
    }

    private float bps = 0, minBps = 150, maxBps = 0;

    @Override
    public void update() {
        //update fields
        GraphPoint graphPoint;
        minBps = 150;
        bps = maxBps = 0;
        for (int graphIndex = 0; graphIndex < graphPoints.size(); graphIndex++) {
            if ((graphPoint = graphPoints.get(graphIndex)) == null) continue;
            float motion = graphPoint.getMotion();
            if (motion < minBps) minBps = motion;
            if (motion > maxBps) maxBps = motion;
            bps = motion;
        }

        final int maxCount = maxCountCalc();
        float motionSpeed = getSelfSpeed() * mc.timer.speed;
        if (motionSpeed < speedMoveTrigger()) motionSpeed = 0.F;

        //removing
        if (mc.player.ticksExisted == 1) graphPoints.clear();
        else {
            GraphPoint first;
            while (graphPoints.size() > maxCount) {
                if ((first = graphPoints.stream().findFirst().orElse(null)) != null) graphPoints.remove(first);
                else graphPoints.clear();
            }
        }

        //adds
        if (motionSpeed >= speedMoveTrigger()) graphPoints.add(new GraphPoint(motionSpeed));
    }

    @Override
    public void render(MatrixStack matrixStack, float partialTicks) {
        if (Client.instance.moduleManager.speedGraphModule.isEnabled()) {
            this.setScaledSize();
            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
            int round = 10;
            //bg
            ShapeRenderer box = Render2D.box(matrixStack, x, y, width, Math.max(60, height));
            box.quad(round, 0xB70E0E0F);
            box.quad(round, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));
            Render2D.size(FontRegister.Type.BOLD, 14).string(matrixStack, name, x + 5, y + 5, ColorUtil.applyOpacity(ColorUtil.getColor(235, 235, 235), 255));
            Render2D.size(FontRegister.Type.ICONS, 20).string(matrixStack, "w", x + width - round - Render2D.size(FontRegister.Type.ICONS, 20).getWidth("w"), y + 5.5F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.35f));
            ShapeRenderer box1 = Render2D.box(matrixStack, x, y + 32.f, width, Math.max(60, height) - 33);
            box1.corner(new float[]{0, round, 0, round}, 0x590E0E0F);
            ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - 2, y - 2, width + 4, Math.max(60, height) + 4);
            boxOutLine.outlineHud(round, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

            float offsets = renderOffsetsGraph();
            float graphMaxHeight = height - 32 - offsets * 2.F;
            float x = this.x + offsets, x2 = this.x + width - offsets;
            float glowing = Client.instance.moduleManager.speedGraphModule.optimizeLevel.isSelected("Качество") ? 1.F : Client.instance.moduleManager.speedGraphModule.optimizeLevel.isSelected("Сбалансированно") ? .6F : .25F;
            if (!graphPoints.isEmpty()) {
                float pixelDensity = pixelDensity();
                float leftMovePartial = getSelfSpeed() >= speedMoveTrigger() ? (partialTicks - .5F) * pixelDensity : 0.F;
                ScaleUtil.scale_pre();
                int[] begins = glowing == 0 ? new int[]{GL11.GL_LINE_STRIP} : new int[]{GL11.GL_TRIANGLE_STRIP, GL11.GL_QUAD_STRIP, GL11.GL_LINE_STRIP, GL11.GL_LINE_STRIP};
                if (Client.instance.moduleManager.speedGraphModule.optimizeLevel.isSelected("Производительность")) {
                    begins = new int[]{GL11.GL_TRIANGLE_STRIP, GL11.GL_QUAD_STRIP, GL11.GL_LINE_STRIP};
                }
                float speedLimit = speedLimit(true);
                //pre
                RenderSystem.disableTexture();
                enableBlend();
                RenderSystem.disableAlphaTest();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
                RenderSystem.shadeModel(GL11.GL_SMOOTH);
                RenderSystem.depthMask(false);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.disableAlphaTest();
                GL12.glHint(GL12.GL_POLYGON_SMOOTH_HINT, GL12.GL_NICEST);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_POINT_SMOOTH);
                GL11.glLineWidth(1.5F);
                GL11.glPointSize(.5F);
                //render & calc
                for (final Integer begin : begins) {
                    BUFFER.begin(begin, DefaultVertexFormats.POSITION_COLOR);
                    boolean addDown = begin == GL11.GL_QUAD_STRIP || begin == GL11.GL_TRIANGLE_STRIP;
                    for (int graphIndex = 0; graphIndex < graphPoints.size(); graphIndex++) {
                        final GraphPoint graphPoint = graphPoints.get(graphIndex);
                        float iteratorProgPC = graphIndex / (float) graphPoints.size(), graphPointX = (x + (x2 - x) * iteratorProgPC - leftMovePartial) / 2.F;
                        float graphPointY = (y + height - offsets - Math.min(graphPoint.getMotionClamped(speedLimit) * graphMaxHeight / speedLimit, graphMaxHeight)) / 2.F;
                        float aPC = (float) Easings.QUAD_IN_OUT.ease((iteratorProgPC > .5F ? 1.F - iteratorProgPC : iteratorProgPC) * 2.F);
                        int color = getColorBloom(begin == GL11.GL_TRIANGLE_STRIP || begin == GL11.GL_QUAD_STRIP ? aPC * glowing / 2.25F : aPC);
                        BUFFER.pos(matrixStack.getLast().getMatrix(), graphPointX, graphPointY).color(color).endVertex();
                        if (addDown)
                            BUFFER.pos(matrixStack.getLast().getMatrix(), graphPointX, begin == GL11.GL_TRIANGLE_STRIP ? (y + height - graphMaxHeight) / 2.F : (y + height - offsets) / 2.F).color(0).endVertex();
                    }
                    //draw
                    TESSELLATOR.draw();
                }
                //post
                GL11.glPointSize(1.F);
                GL11.glLineWidth(1.F);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_POINT_SMOOTH);
                RenderSystem.depthMask(true);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.shadeModel(GL11.GL_FLAT);
                RenderSystem.enableCull();
                RenderSystem.disableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
                RenderSystem.clearCurrentColor();
                ScaleUtil.scale_post();
                //post unset graph draw texts
                if (Client.instance.moduleManager.speedGraphModule.graphData.getValue()) {
                    TextRenderer textDrawer = Render2D.size(FontRegister.Type.BOLD, 7);
                    String bpsStr = motionAsBPSString("Bps", bps), minBPSStr = motionAsBPSString("Min", minBps), maxBPSStr = motionAsBPSString("Max", maxBps);
                    int textColor = ColorUtil.getColor(190, 190, 210, 80);
                    textDrawer.string(matrixStack, bpsStr, x + width - offsets - 6.F - textDrawer.getWidth(bpsStr), y + 33, textColor);
                    textDrawer.string(matrixStack, minBPSStr, x + width - offsets - 6.F - textDrawer.getWidth(minBPSStr), y + 40.5F, textColor);
                    textDrawer.string(matrixStack, maxBPSStr, x + width - offsets - 6.F - textDrawer.getWidth(maxBPSStr), y + 48, textColor);
                }
            }
        }
    }


    private final CopyOnWriteArrayList<GraphPoint> graphPoints = new CopyOnWriteArrayList<>();

    private class GraphPoint {
        private final long hash = hashCode();
        private final float motion;

        public float getMotion() {
            return motion;
        }

        public float getMotionClamped(float maxValue) {
            return Math.min(motion, maxValue);
        }

        public GraphPoint(float motion) {
            this.motion = motion;
        }

        public boolean isMaxSpeedAsList(List<GraphPoint> graphPointsList) {
            final GraphPoint record = graphPointsList.stream().filter(obj -> obj.hash != this.hash).max(Comparator.comparing(point -> point.getMotion())).orElse(null);
            return record != null && this.getMotion() > record.getMotion();
        }

        public boolean isMinSpeedAsList(List<GraphPoint> graphPointsList) {
            final GraphPoint record = graphPointsList.stream().filter(obj -> obj.hash != this.hash).min(Comparator.comparing(point -> point.getMotion())).orElse(null);
            return record != null && this.getMotion() < record.getMotion();
        }

        public boolean isEmptySpeed() {
            return this.getMotion() == 0.F;
        }
    }

    public String motionAsBPSString(String append, float motion) {
        return append + " " + MathUtil.roundPROBLYA(motion * 15.3571428571F, .1F);
    }

}