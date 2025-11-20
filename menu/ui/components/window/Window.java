package relake.menu.ui.components.window;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.util.MathUtil;
import relake.menu.ui.components.Component;

import java.time.Duration;

@Setter
@Getter
@RequiredArgsConstructor
public class Window extends Component {
    private final Animation animation = new Animation(1, Duration.ofMillis(150))
            .setDirection(Direction.BACKWARD);

    private final String name;

    public Window pos(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Window size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isHovered(mouseX, mouseY)) {
            getAnimation().switchDirection(false);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
