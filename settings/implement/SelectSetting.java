package relake.settings.implement;

import lombok.Getter;
import lombok.Setter;
import relake.settings.Setting;

import java.util.Arrays;
import java.util.List;
@Getter
@Setter
public class SelectSetting extends Setting<String> {
    private String selected;
    private List<String> list;

    public SelectSetting(String name) {
        super(name);
    }

    @Override
    public String getValue() {
        return selected;
    }

    public SelectSetting setValue(String... values) {
        List<String> list = Arrays.asList(values);

        selected = list.get(0);
        this.list = list;
        return this;
    }

    public boolean isSelected(String name) {
        return selected.equalsIgnoreCase(name);
    }
}
