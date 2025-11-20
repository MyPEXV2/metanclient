package relake.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.util.InputMappings;
import relake.common.InstanceAccess;

import static net.minecraft.client.util.InputMappings.Type.*;

@UtilityClass
public class StringUtil implements InstanceAccess {
    public String getKeyName(int key) {
        InputMappings.Input isMouse = key < 8 ? MOUSE.getOrMakeInput(key) : KEYSYM.getOrMakeInput(key);

        InputMappings.Input code = key == -1
                ? SCANCODE.getOrMakeInput(key)
                : isMouse;

        return key == -1 ? "None" : code
                .getTranslationKey()
                .replace("key.keyboard.", "")
                .replace("key.mouse.", "mouse ")
                .replace(".", " ")
                .toUpperCase();
    }
}
