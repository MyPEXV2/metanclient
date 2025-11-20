package relake.event.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import relake.event.Event;

@Getter
@RequiredArgsConstructor
public class ScreenRenderEvent extends Event {
    private final MatrixStack matrixStack;
    private final float partialTicks;
}

