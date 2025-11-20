package relake.command;

import relake.command.implement.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {
    public final List<Command> commands = new ArrayList<>();
    public final String prefix = ".";

    public final HelpCommand helpCommand = new HelpCommand();
    public final TestCommand testCommand = new TestCommand();
    public final NukerCommand nukerCommand = new NukerCommand();
    public final ConfigCommand configCommand = new ConfigCommand();
    public final FriendCommand friendCommand = new FriendCommand();
    public final LoginCommand loginCommand = new LoginCommand();
    public final TransferCommand transferCommand = new TransferCommand();
    public final ClipCommand clipCommand = new ClipCommand();
    public final MacrosCommand macrosCommand = new MacrosCommand();
    public final IgnoreCommand ignoreCommand = new IgnoreCommand();
    public final BindCommand bindCommand = new BindCommand();
    public final PointsCommand pointsCommand = new PointsCommand();
    public final CacaoCommand cacaoCommand = new CacaoCommand();

    public CommandManager() {
        registerCommands(
                helpCommand,
                testCommand,
                nukerCommand,
                configCommand,
                friendCommand,
                loginCommand,
                transferCommand,
                clipCommand,
                macrosCommand,
                ignoreCommand,
                pointsCommand,
                bindCommand,
                cacaoCommand
        );
    }

    private void registerCommands(Command... commands) {
        this.commands.addAll(Arrays.asList(commands));
    }
}
