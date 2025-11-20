package relake.command.implement;

import relake.command.Command;
import relake.common.util.ChatUtil;

import java.util.Arrays;

public class TestCommand extends Command {

    public TestCommand() {
        super("Test", "test command");
    }

    @Override
    public void execute(String[] args) {
        ChatUtil.send("Test - " + Arrays.toString(args));
    }
}
