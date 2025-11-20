package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.player.AttackEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Glass;

import java.time.Duration;
import java.util.ArrayList;

public class TorusModule extends Module {

	public TorusModule() {
		super("Torus", "Добавляет невероятно красивые стекловидные пузыри от ударов", "Adds incredibly beautiful glassy bubbles from impacts", ModuleCategory.Render);
	}
	
	ArrayList<TorusObj> toruses = new ArrayList<>();

	@EventHandler
	public void attack(AttackEvent event) {
		Vector3d particlePos = event.getEntity().getPositionVec().add(0, event.getEntity().getHeight()/2F, 0);
		TorusObj particle = new TorusObj(particlePos);

		toruses.add(particle);
	}

	@EventHandler
	public void worldRender(WorldRenderEvent worldRenderEvent) {
		toruses.removeIf(torus -> torus.shouldRemove());

		if (toruses.isEmpty()) return;

		BufferBuilder builder = Tessellator.getInstance().getBuffer();

		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();

		Glass.draw();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		for (TorusObj particle : toruses) {
			particle.render(worldRenderEvent.getStack(), builder);
		}

		tessellator.draw();
		Glass.end();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}
	
	class TorusObj {

		final Vector2f rotation;
		Animation sizing = new Animation(1, Duration.ofMillis(1000)).setDirection(Direction.BACKWARD);
		StopWatch timer = new StopWatch();
		Vector3d pos;
		float size = 1.0F;
		long lifeTime;
		
		public TorusObj(Vector3d pos) {
			this.pos = pos;
			rotation = get(pos);
			lifeTime = 1000;
			sizing.switchDirection(true);
		}
		
		public void render(MatrixStack matrices, BufferBuilder builder) {
		    matrices.push();

			float sizing = (float) Math.sqrt(1 - Math.pow(this.sizing.get() - 1, 2));
		    float size = 1.f;
		    float radius = sizing*size;
		    float innerRadius = (size-sizing*size) * radius;
		    float outerRadius = (sizing) * radius;

		    Matrix4f matrix = matrices.getLast().getMatrix();
		       
		    Vector3d camera = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
		    double x = pos.getX() - camera.x;
		    double y = pos.getY() - camera.y;
		    double z = pos.getZ() - camera.z;

		    matrices.translate(x, y, z);

		    matrices.rotate(Vector3f.YN.rotation((float) Math.toRadians(rotation.x)));
		    matrices.rotate(Vector3f.XN.rotation((float) Math.toRadians(-rotation.y + 90)));

		    int segments = 30;

		    for (int i = 0; i < segments; i++) {
		        double theta1 = 2 * Math.PI * i / segments;
		        double theta2 = 2 * Math.PI * (i + 1) / segments;

		        for (int j = 0; j < segments; j++) {
		            double phi1 = 2 * Math.PI * j / segments;
		            double phi2 = 2 * Math.PI * (j + 1) / segments;

		            float x1 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.cos(theta1));
		            float y1 = (float) (innerRadius * Math.sin(phi1));
		            float z1 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.sin(theta1));

		            float x2 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.cos(theta1));
		            float y2 = (float) (innerRadius * Math.sin(phi2));
		            float z2 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.sin(theta1));

		            float x3 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.cos(theta2));
		            float y3 = (float) (innerRadius * Math.sin(phi2));
		            float z3 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.sin(theta2));

		            float x4 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.cos(theta2));
		            float y4 = (float) (innerRadius * Math.sin(phi1));
		            float z4 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.sin(theta2));

		            builder.pos(matrix, x1, y1, z1).color(-1).endVertex();
		            builder.pos(matrix, x2, y2, z2).color(-1).endVertex();
		            builder.pos(matrix, x3, y3, z3).color(-1).endVertex();
		            builder.pos(matrix, x4, y4, z4).color(-1).endVertex();
		        }
		    }

		    matrices.pop();
		}


		public boolean shouldRemove() {
			return timer.finished(lifeTime);
		}
		
	}

	private Vector2f get(Vector3d target) {
		ClientPlayerEntity e = mc.player;
		double x = e.getPosX();
		double y = e.getPosY();
		double z = e.getPosZ();

		Vector3d vec = target;
		double posX = vec.getX() - x;
		double posY = vec.getY() - (y + (double) mc.player.getEyeHeight());
		double posZ = vec.getZ() - z;
		double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
		float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
		float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
		float sens = (float) (Math.pow(mc.gameSettings.mouseSensitivity, 1.5) * 0.05f + 0.1f);
		float pow = sens * sens * sens * 1.2F;
		yaw -= yaw % pow;
		pitch -= pitch % (pow * sens);
		return new Vector2f(yaw, pitch);
	}
}
