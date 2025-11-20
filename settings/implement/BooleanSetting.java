package relake.settings.implement;

import relake.settings.Setting;

public class BooleanSetting extends Setting<Boolean> {
    private Runnable onAction;

    public BooleanSetting(String name) {
        super(name);
    }

    @Override
    public Boolean getValue() {
        return super.getValue() != null && super.getValue();
    }

    @Override
    public BooleanSetting setValue(Boolean value) {
        super.setValue(value);
        
        if (onAction != null) {
            onAction.run();
        }
        return this;
    }

    public BooleanSetting onAction(Runnable action) {
        this.onAction = action;
        return this;
    }
}
