package relake.common.component.distant_charm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class UVec3dColored {
    private final double x, y, z;
    private final int color;
}
