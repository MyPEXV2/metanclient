package relake;

import relake.account.AccountManager;
import relake.command.CommandManager;
import relake.common.component.ClickPearlComponent;
import relake.common.component.IgnoreComponent;
import relake.common.component.PointComponent;
import relake.common.component.TargetComponent;

import relake.common.component.rotation.FreeLookComponent;
import relake.common.component.rotation.RotationComponent;
import relake.common.component.rtxsoundengine.SoundMixFilter;
import relake.config.Config;
import relake.config.ConfigManager;
import relake.draggable.DraggableManager;
import relake.event.EventManager;
import relake.friend.FriendManager;
import relake.macros.MacrosManager;
import relake.menu.ui.MenuScreen;
import relake.menu.ui.components.window.WindowManager;
import relake.module.ModuleManager;
import relake.point.PointTraceManager;
import relake.render.display.bqrender.Shaders;
import relake.render.display.font.FontRegister;
import relake.shader.ShaderRegister;

public class Client {
    public static final Client instance = new Client();

    private DraggableManager draggableManager;
    public final EventManager eventManager = new EventManager();
    public final ModuleManager moduleManager = new ModuleManager();
    public final CommandManager commandManager = new CommandManager();
    public final ConfigManager configManager = new ConfigManager();
    public final FriendManager friendManager = new FriendManager();
    public final AccountManager accountManager = new AccountManager();
    public final PointTraceManager pointsManager = new PointTraceManager();
    public final MacrosManager macrosManager = new MacrosManager();
    public final WindowManager windowManager = new WindowManager();

    private final SoundMixFilter rtxEngine = SoundMixFilter.makeDistorterMixer();
    private MenuScreen menuScreen;

    public void start() {


        for (Config config : Client.instance.configManager.configs) {
            config.load();
        }

        new FreeLookComponent();
        new RotationComponent();
        new TargetComponent();
        new PointComponent();
        new IgnoreComponent();
        new ClickPearlComponent();

        ShaderRegister.init();
        Shaders.loadShaders();
        FontRegister.init();
    }

    public MenuScreen getMenu() {
        if (menuScreen == null) {
            menuScreen = new MenuScreen();
        }

        return menuScreen;
    }

    public DraggableManager getDraggableManager() {
        if (draggableManager == null) {
            draggableManager = new DraggableManager();
        }
        return draggableManager;
    }

    public SoundMixFilter getRtxEngine() {
        return rtxEngine;
    }
}