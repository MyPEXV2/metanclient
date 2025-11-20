package relake.settings.implement;

import lombok.Getter;
import relake.settings.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class MultiSelectSetting extends Setting<List<String>> {
    private List<String> list, selected = new ArrayList<>();

    public MultiSelectSetting(String name) {
        super(name);
    }

    @Override
    public List<String> getValue() {
        return selected;
    }

    public MultiSelectSetting setValue(String... settings) {
        list = Arrays.asList(settings);
        return this;
    }

    public boolean isSelected(String name) {
        return selected.contains(name);
    }
}
