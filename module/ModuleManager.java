package relake.module;

import lombok.SneakyThrows;
import relake.module.implement.combat.*;
import relake.module.implement.misc.*;
import relake.module.implement.movement.*;
import relake.module.implement.player.*;
import relake.module.implement.render.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleManager {
    public final List<Module> modules = new ArrayList<>();

    //Combat
    public final AttackAuraModule attackAuraModule = new AttackAuraModule();
    public final EntityBoxModule entityBoxModule = new EntityBoxModule();
    public final BackTrackModule backTrackModule = new BackTrackModule();
    public final AutoExplosionModule autoExplosionModule = new AutoExplosionModule();
    public final BowSpamModule bowSpamModule = new BowSpamModule();
    public final TriggerBotModule triggerBotModule = new TriggerBotModule();
    public final ElytraTargetModule elytraTargetModule = new ElytraTargetModule();
    public final VelocityModule velocityModule = new VelocityModule();
    public final AutoTotemModule autoTotemModule = new AutoTotemModule();
    public final AutoPotionModule autoPotionModule = new AutoPotionModule();
    public final AutoGAppleModule autoGAppleModule = new AutoGAppleModule();
    public final AutoSwapModule autoSwapModule = new AutoSwapModule();
    public final NoPlayerTraceModule noPlayerTraceModule = new NoPlayerTraceModule();
    public final NoFriendDamageModule noFriendDamageModule = new NoFriendDamageModule();

    //Movement
    public final EagleModule eagleModule = new EagleModule();
    public final FlightModule flightModule = new FlightModule();
    public final JesusModule jesusModule = new JesusModule();
    public final StrafeModule strafeModule = new StrafeModule();
    public final NoJumpDelayModule noJumpDelayModule = new NoJumpDelayModule();
    public final NoSlowModule noSlowModule = new NoSlowModule();
    public final ScreenWalkModule screenWalkModule = new ScreenWalkModule();
    public final SneakModule sneakModule = new SneakModule();
    public final SpeedModule speedModule = new SpeedModule();
    public final TimerModule timerModule = new TimerModule();
    public final SpiderModule spiderModule = new SpiderModule();
    public final SprintModule sprintModule = new SprintModule();
    public final WaterSpeedModule waterSpeedModule = new WaterSpeedModule();
    public final AutoDodgeModule autoDodgeModule = new AutoDodgeModule();
    public final TargetStrafeModule targetStrafeModule = new TargetStrafeModule();


    //Render
    public final VulcanPredictionModule vulcanPredictionModule = new VulcanPredictionModule();
    public final AspectRatioModule aspectRatioModule = new AspectRatioModule();
    public final HitParticlesModule hitParticlesModule = new HitParticlesModule();
    public final HUDModule hudModule = new HUDModule();
    public final AmbienceModule ambienceModule = new AmbienceModule();
    public final ArrowsModule arrowsModule = new ArrowsModule();
    public final ChinaHatModule chinaHatModule = new ChinaHatModule();
    public final FullBrightModule fullBrightModule = new FullBrightModule();
    public final CrosshairModule crosshairModule = new CrosshairModule();
    public final JumpCircleModule jumpCircleModule = new JumpCircleModule();
    public final SwingAnimationModule swingAnimationModule = new SwingAnimationModule();
    public final NoRenderModule noRenderModule = new NoRenderModule();
    public final ParticlesModule particlesModule = new ParticlesModule();
    public final ParticleTrailModule particleTrailModule = new ParticleTrailModule();
    public final PearlPredictionModule pearlPredictionModule = new PearlPredictionModule();
    public final PlayerESPModule playerESPModule = new PlayerESPModule();
    public final ItemESPModule itemESPModule = new ItemESPModule();
    public final SeeInvisiblesModule seeInvisiblesModule = new SeeInvisiblesModule();
    public final TargetESPModule targetESPModule = new TargetESPModule();
    public final NotificationsModule notificationsModule = new NotificationsModule();
    public final ShaderHandsModule shaderHandsModule = new ShaderHandsModule();
    public final ClientSoundsModule clientSoundsModule = new ClientSoundsModule();
    public final TorusModule torusModule = new TorusModule();
    public final SantaHatModule santaHatModule = new SantaHatModule();
    public final SpeedGraphModule speedGraphModule = new SpeedGraphModule();
    public final TargetHUDModule targetHUDModule = new TargetHUDModule();
    public final GlowESPModule glowESPModule = new GlowESPModule();
    public final FragEffectsModule fragEffectsModule = new FragEffectsModule();
    public final ThrowDirsModule throwDirsModule = new ThrowDirsModule();
    public final BlockOverlayModule blockOverlayModule = new BlockOverlayModule();
    //Player
    public final FreeCamModule freeCamModule = new FreeCamModule();
    public final NoServerDesyncModule noServerDesyncModule = new NoServerDesyncModule();
    public final AutoAcceptModule autoAcceptModule = new AutoAcceptModule();
    public final AutoLeaveModule autoLeaveModule = new AutoLeaveModule();
    public final FastBreakModule fastBreakModule = new FastBreakModule();
    public final FastPlaceModule fastPlaceModule = new FastPlaceModule();
    public final AutoShiftTapModule autoShiftTapModule = new AutoShiftTapModule();
    public final NoInteractModule noInteractModule = new NoInteractModule();
    public final NoPushModule noPushModule = new NoPushModule();
    public final NukerModule nukerModule = new NukerModule();
    public final AutoToolModule autoToolModule = new AutoToolModule();
    public final StreamerModeModule streamerModeModule = new StreamerModeModule();

    //Misc
    public final AutoFishModule autoFishModule = new AutoFishModule();
    public final AutoEatModule autoEatModule = new AutoEatModule();
    public final BetterMinecraftModule betterMinecraftModule = new BetterMinecraftModule();
    public final ElytraHelperModule elytraHelperModule = new ElytraHelperModule();
    public final AutoTransferModule autoTransferModule = new AutoTransferModule();
    public final ChestStealerModule chestStealerModule = new ChestStealerModule();
    public final ClickPearlModule clickPearlModule = new ClickPearlModule();
    public final NoCommandsModule noCommandsModule = new NoCommandsModule();
    public final AutoAuthModule autoAuthModule = new AutoAuthModule();
    public final ItemScrollerModule itemScrollerModule = new ItemScrollerModule();
    public final RPSpooferModule rpSpooferModule = new RPSpooferModule();
    public final OptimizerModule optimizerModule = new OptimizerModule();
    public final FTHelperModule ftHelperModule = new FTHelperModule();
    public final AutoFTFarmModule autoFTFarmModule = new AutoFTFarmModule();
    public final ClickFriendModule clickFriendModule = new ClickFriendModule();
    public final RTXSoundsModule rtxSoundsModule = new RTXSoundsModule();
    public final BaseFinderModule baseFinderModule = new BaseFinderModule();
    public final EventTimerModule eventTimerModule = new EventTimerModule();
    public final AutoCashModule autoCashModule = new AutoCashModule();

    @SneakyThrows
    public ModuleManager() {
        for (Field field : ModuleManager.class.getFields()) {
            if (field.get(this) instanceof Module m) {
                this.modules.add(m);
            }
        }
    }

    private void registerModules(Module... modules) {
        this.modules.addAll(Arrays.asList(modules));
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }

        return null;
    }
}
