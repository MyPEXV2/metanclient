package relake.command.implement;


import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.command.Command;
import relake.point.PointTraceManager;
import relake.common.util.ChatUtil;

public class PointsCommand extends Command {
    public PointsCommand() {
        super("points", "points command", "p", "point", "points");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1)
            ChatUtil.send(TextFormatting.RED + """
                                        
                    .point add <name> <xyz/xz>
                    .point remove <last/name>
                    .point clear
                    .point list               \s""");

        PointTraceManager pointsManager = Client.instance.pointsManager;

        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (String arg : args) {
            if (i >= 4) {
                sb.append(arg);
                sb.append(" ");
            }
            i++;
        }
        // add
        if (args[1].equalsIgnoreCase("add") && (args.length == 3 || args.length == 5 || args.length == 6)) {
            String name = args[2];
            Vector3f pos = null;
            try {
                pos = args.length == 3 ? new Vector3f((float) Minecraft.getInstance().player.getPosX(), (float)  Minecraft.getInstance().player.getPosY(), (float)  Minecraft.getInstance().player.getPosZ()) : args.length == 5 ? new Vector3f(Float.parseFloat(args[3]), (int) Minecraft.getInstance().player.getPosY(), Float.parseFloat(args[4])) : new Vector3f(Float.parseFloat(args[3]), Float.parseFloat(args[4]), Float.parseFloat(args[5]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pos == null) {
                ChatUtil.send(TextFormatting.RED + "Впиши блядь нормально корды а.");
            } else {
                if (pointsManager.addPoint(pos.getX(), pos.getY(), pos.getZ(), name)) {
                    ChatUtil.send(TextFormatting.GREEN + "Поинт \"" + args[2] + "\" был добавлен.");
                } else {
                    ChatUtil.send(TextFormatting.GREEN + "Поинт \"" + args[2] + "\" уже существует.");
                }
            }
        }
        // remove
        else if (args[1].equalsIgnoreCase("remove") && args.length == 3) {
            if (args[2].equalsIgnoreCase("last") && pointsManager.removeLastPoint()) {
                ChatUtil.send(TextFormatting.GREEN + "Самый новый поинт \"" + args[2] + "\" был удалён.");
            } else if (pointsManager.removePoint(args[2]))
                ChatUtil.send(TextFormatting.GREEN + "Поинт \"" + args[2] + "\" был удалён.");
            else ChatUtil.send(TextFormatting.RED + "Поинта с названием \"" + args[2] + "\" не существует.");
        }
        // clear
        else if (args[1].equalsIgnoreCase("clear") && args.length == 2) {
            int count = pointsManager.traces.size();
            if (pointsManager.clearPoints()) {
                ChatUtil.send(TextFormatting.GREEN + "Очищено поинтов: " + count);
            } else {
                ChatUtil.send(TextFormatting.GREEN + "Поинтов и так нету.");
            }
        }
        // list
        else if (args[1].equalsIgnoreCase("list") && args.length == 2) {
            if (pointsManager.traces.isEmpty()) {
                ChatUtil.send(TextFormatting.RED + "Список поинтов пуст.");
            } else {
                ChatUtil.send(TextFormatting.GRAY + "Количество поинтов: " + pointsManager.traces.size() + ".");

                pointsManager.traces.forEach(pointTrace -> {
                    ChatUtil.send(TextFormatting.AQUA + pointTrace.name + " " + TextFormatting.DARK_AQUA + "x" + (int) pointTrace.x + ", y" + (int) pointTrace.y + ", z" + (int) pointTrace.z);
                });
            }
        } else ChatUtil.send(TextFormatting.RED + """
                                
                .point add <name> <xyz/xz>
                .point remove <last/name>
                .point clear
                .point list               \s""");

    }
}
