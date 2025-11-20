package relake.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModuleCategory {
    Combat("a"),
    Movement("b"),
    Render("c"),
    Player("d"),
    Misc("e");

    private final String textureID;
}

