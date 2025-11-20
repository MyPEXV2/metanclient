package relake.render.display.font;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.util.Map;

public class FontRegister {
    private static final Map<FontKey, FontEngine> fontCache = new HashMap<>();

    public static void init() {
        for (Type type : Type.values()) {
            for (int size = 4; size <= 32; size++) {
                fontCache.put(new FontKey(size, type), new FontEngine(type.getName(), size));
            }
        }
    }

    public static FontEngine getSize(int size) {
        return getSize(Type.DEFAULT, size);
    }

    public static FontEngine getSize(Type type, int size) {
        return fontCache.computeIfAbsent(new FontKey(size, type), k -> new FontEngine(type.getName(), size));
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        DEFAULT("font.ttf"),
        ICONS("relake.ttf"),
        BOLD("bold.ttf"),
        LOGO("logo.ttf");

        private final String name;
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class FontKey {
        private final int size;
        private final Type type;
    }
}