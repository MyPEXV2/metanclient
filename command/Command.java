package relake.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import relake.common.InstanceAccess;

@Getter
@RequiredArgsConstructor
public abstract class Command implements InstanceAccess {
    private final String name;
    private final String[] aliases;
    private final String description;

    public Command(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
    }

    public abstract void execute(String[] args);
}
