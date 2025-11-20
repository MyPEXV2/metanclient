package relake.event.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import relake.event.Event;

@Getter
@AllArgsConstructor
public class PostRenderEntityOverlayEvent extends Event {
    private final Entity entity;
    private final EntityModel<?> entityModel;
    private final MatrixStack matrixStack;
    private final float partialTicks;
}
