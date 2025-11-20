package relake.render.display.shape;

public enum Side {
    LEFT {
        @Override
        public void expand(ShapeRenderer renderer, float amount) {
            renderer.x -= amount;
            renderer.width += amount;
        }
    },
    RIGHT {
        @Override
        public void expand(ShapeRenderer renderer, float amount) {
            renderer.width += amount;
        }
    },
    TOP {
        @Override
        public void expand(ShapeRenderer renderer, float amount) {
            renderer.y -= amount;
            renderer.height += amount;
        }
    },
    BOTTOM {
        @Override
        public void expand(ShapeRenderer renderer, float amount) {
            renderer.height += amount;
        }
    },
    ALL {
        @Override
        public void expand(ShapeRenderer renderer, float amount) {
            renderer.x -= amount;
            renderer.y -= amount;
            renderer.width += 2 * amount;
            renderer.height += 2 * amount;
        }
    };

    public abstract void expand(ShapeRenderer renderer, float amount);
}