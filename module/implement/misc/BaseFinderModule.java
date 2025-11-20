package relake.module.implement.misc;

import net.minecraft.entity.player.PlayerEntity;
import relake.Client;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class BaseFinderModule extends Module {
    
    private final SecureRandom random = new SecureRandom();
    private final Set<String> recordedPlayers = new HashSet<>();
    private int tickCounter = 0;
    private static final int CHECK_INTERVAL_MIN = 50;
    private static final int CHECK_INTERVAL_MAX = 120;
    private int nextCheckTick = 0;
    private static final double SEARCH_RANGE = 900.0;
    private int playersCheckedThisTick = 0;
    private static final int MAX_PLAYERS_PER_TICK = 2;
    
    public BaseFinderModule() {
        super("Base Finder", "Находит и записывает координаты игроков в радиусе 900 блоков", "Finds and records player coordinates within 900 block radius", ModuleCategory.Misc);
    }
    
    @EventHandler
    public void tick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        tickCounter++;
        if (tickCounter < nextCheckTick) return;
        
        nextCheckTick = tickCounter + CHECK_INTERVAL_MIN + random.nextInt(CHECK_INTERVAL_MAX - CHECK_INTERVAL_MIN);
        playersCheckedThisTick = 0;
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (playersCheckedThisTick >= MAX_PLAYERS_PER_TICK) break;
            
            if (player == mc.player) continue;
            
            if (random.nextFloat() < 0.15F) continue;
            
            double distance = mc.player.getDistance(player);
            if (distance > SEARCH_RANGE) continue;
            
            String playerName = player.getNotHidedName().getString();
            if (Client.instance.friendManager.isFriend(playerName)) continue;
            
            playersCheckedThisTick++;
            
            int roundedX = (int) Math.floor(player.getPosX());
            int roundedY = (int) Math.floor(player.getPosY());
            int roundedZ = (int) Math.floor(player.getPosZ());
            String playerKey = playerName + "_" + roundedX + "_" + roundedY + "_" + roundedZ;
            
            if (recordedPlayers.contains(playerKey)) continue;
            
            recordPlayerCoordinates(playerName, player.getPosX(), player.getPosY(), player.getPosZ());
            
            recordedPlayers.add(playerKey);
            
            if (random.nextFloat() < 0.3F) break;
        }
    }
    
    private void recordPlayerCoordinates(String playerName, double x, double y, double z) {
        try {
            String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
            File desktopDir = new File(desktopPath);
            if (!desktopDir.exists()) {
                desktopDir.mkdirs();
            }
            
            File file = new File(desktopDir, "BaseFinder.txt");
            
            try (FileWriter writer = new FileWriter(file, true)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(new Date());
                String line = String.format("[%s] %s: X=%.6f Y=%.6f Z=%.6f%n", timestamp, playerName, x, y, z);
                writer.write(line);
                writer.flush();
            }
        } catch (IOException e) {
        }
    }
    
    @Override
    public void disable() {
        super.disable();
        recordedPlayers.clear();
        tickCounter = 0;
        nextCheckTick = 0;
        playersCheckedThisTick = 0;
    }
}

