package relake.settings;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

@Getter
@Setter
@Accessors(chain = true)
public class Setting<T> {
    private String name;
    private T value;
    private Supplier <Boolean> visible = () -> true;

    public Setting(String name) {
        this.name = name;
    }
}
