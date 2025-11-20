package relake.settings.implement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import relake.settings.Setting;

import java.util.regex.Pattern;

@Getter
@Setter
@Accessors(chain = true)
public class StringSetting extends Setting<String> {

    private Pattern pattern = Pattern.compile("[^а-яА-ЯёЁa-zA-Z0-9]");

    public StringSetting(String name) {
        super(name);
    }
}
