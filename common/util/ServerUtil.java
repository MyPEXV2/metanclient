package relake.common.util;

import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.ClientBossInfo;
import net.minecraft.client.gui.overlay.BossOverlayGui;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import relake.common.InstanceAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class ServerUtil implements InstanceAccess {
    public int FT_ANARCHY = -1, RW_GRIEF = -1;

    private String lastIP = "mc.funtime.su";

    public String getIP() {
        if (!isInGame()) return "mainmenu";
        if (mc.isSingleplayer()) return "local";
        if (mc.getCurrentServerData() != null) return lastIP = mc.getCurrentServerData().serverIP.toLowerCase();

        return lastIP;
    }

    public boolean isSunWay() {
        String ip = getIP();
        return ip.contains("sunw");
    }

    public boolean isSaturn() {
        String ip = getIP();
        return ip.contains("saturn-x");
    }

    public boolean isSR() {
        String ip = getIP();
        return ip.contains("sunmc");
    }

    public boolean isFS() {
        String ip = getIP();
        return ip.contains("funsky");
    }

    public boolean isRW() {
        String ip = getIP();
        return ip.contains("reallyworld") || ip.contains("playrw");
    }

    public boolean isInGame() {
        return mc.world != null && mc.player != null;
    }

    public boolean isBedWars() {
        return mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.contains("mineblaze") || mc.getCurrentServerData().serverIP.contains("dexland") || mc.getCurrentServerData().serverIP.contains("masedworld") || mc.getCurrentServerData().serverIP.contains("cheatmine"));
    }

    public String getServerName(boolean shortName) {
        String ip = getIP();
        String[] parts = ip.split("\\.");

        if (mc.isSingleplayer())
            return applyCase(ip, shortName);

        if (parts.length == 3)
            return applyCase(parts[1], shortName);

        if (parts.length == 2)
            return applyCase(parts[0], shortName);

        if (ip.contains(":"))
            return ip.split(":")[0];

        return ip;
    }

    private String applyCase(String server, boolean shortName) {
        server = server.replace("-", "");

        ArrayList<Data> datas = new ArrayList<>();
        String[] suffixes = { "legacy", "bars", "world", "best", "times", "time", "shine", "sky", "lands", "land", "trainer", "server", "blaze", "mine", "lord", "cube" , "grief", "craft", "rise", "force", "project" };

        Arrays.stream(suffixes).forEach(suffix -> datas.add(genData(suffix)));

        Arrays.stream(new Data[] {
                new Data("mc", "MC", "-MC"),
                new Data("hvh", "HVH", "-HVH"),
                new Data("pvp", "PVP", "PVP")
        }).forEach(datas::add);

        if (mc.isSingleplayer() && !shortName)
            server = "LocalHost";

        if (isSR())
            server = shortName ? "SR" : "SunRise";

        if (isSaturn())
            server = shortName ? "S-X" : "SaturnX";

        if (isSunWay())
            server = shortName ? "SW" : "SunWay";

        for (Data data : datas) {
            if (server.contains(data.orig)) {
                if (shortName) {
                    String rightPart = server.replace(data.orig, "");
                    server = server.substring(0, 1).toUpperCase() + data.small;
                } else {
                    server = server.replace(data.orig, data.big);
                    server = server.substring(0, 1).toUpperCase() + server.substring(1);
                }

                return server;
            }
        }

        server = server.substring(0, 1).toUpperCase() + server.substring(1);

        return server;
    }

    public boolean isFT() {
        String ip = getIP();
        return ip.contains("funtime") || ip.contains("ft") || ip.contains("FunTime");
    }

    public boolean isHW() {
        String ip = getIP();
        return ip.contains("holyworld") || ip.contains("hollyworld");
    }

    public boolean is(String str) {
        return getIP().contains(str);
    }

    public boolean hasCT() {
        return BossOverlayGui.CT;
    }

    public int getTimeCT() {
        return BossOverlayGui.timeCT;
    }

    public int ping() {
        return PlayerTabOverlayGui.getPlayerPings() == null || !PlayerTabOverlayGui.getPlayerPings().containsKey(mc.player.getName().getString()) ? 0 : PlayerTabOverlayGui.getPlayerPings().get(mc.player.getName().getString());
    }


    private Data genData(String full) {
        return new Data(full, full.substring(0, 1).toUpperCase() + full.substring(1), full.substring(0, 1).toUpperCase());
    }

    @AllArgsConstructor
    class Data {
        private String orig, big, small;
    }
}