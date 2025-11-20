package relake.command.implement;

import net.minecraft.util.text.TextFormatting;
import relake.command.Command;
import relake.common.util.ChatUtil;

public class ClipCommand extends Command {
    public ClipCommand() {
        super("clip", "clip command");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ChatUtil.send(TextFormatting.RED + "Использование: .clip <h | v> [аргумент]");
            return;
        }

        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "h":
                handleHorizontal(args);
                break;
            case "v":
                handleVertical(args);
                break;
            default:
                ChatUtil.send(TextFormatting.RED + "Неизвестная подкоманда. Используйте: h, v.");
        }
    }

    private void handleHorizontal(String[] args) {
        if (args.length < 3) {
            ChatUtil.send(TextFormatting.RED + "Использование: .clip h <arg>");
            return;
        }

        final double amount = Double.parseDouble(args[2]);

        final double yaw = Math.toRadians(mc.player.rotationYaw);
        final double x = Math.sin(yaw) * amount;
        final double z = Math.cos(yaw) * amount;

        mc.player.setPosition(mc.player.getPositionVec().x - x, mc.player.getPositionVec().y, mc.player.getPositionVec().z + z);
        ChatUtil.send("Переместил вас на " + Math.abs(amount) + " блоков " + (amount > 0 ? "вперед" : "назад"));
    }

    private void handleVertical(String[] args) {
        if (args.length < 3) {
            ChatUtil.send(TextFormatting.RED + "Использование: .clip v <arg>");
            return;
        }

        final double amount = Double.parseDouble(args[2]);

        mc.player.setPosition(mc.player.getPositionVec().x, mc.player.getPositionVec().y + amount, mc.player.getPositionVec().z);
        ChatUtil.send("Переместил вас на " + Math.abs(amount) + " блоков " + (amount > 0 ? "вверх" : "вниз"));
    }
}
