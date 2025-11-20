package relake.event.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.vector.Matrix4f;
import relake.event.Event;

@Getter
@RequiredArgsConstructor
public class WorldRenderEvent extends Event {
    private final MatrixStack stack;
    private final Matrix4f matrix;
    private final float ticks;
}
