package relake.module.implement.movement;

import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.MathHelper;
import relake.common.util.MathUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.SelectSetting;

public class TimerModule extends Module {

    public final Setting<Float> timer = new FloatSetting("Базовая скорость").range(.1F, 5.F).setIncrement(.1F).setValue(2.F);
    public final Setting<Float> randomize = new FloatSetting("Фактор случайности").range(.0F, 3.F).setIncrement(.1F).setValue(.0F);
    public final Setting<Boolean> timeOut = new BooleanSetting("Выкл. через время").setValue(false);
    public final Setting<Float> timeOutMS = new FloatSetting("Таймаут MS").range(10L, 1000L).setIncrement(10L).setValue(350.F).setVisible(() -> this.timeOut.getValue());
    public final Setting<Boolean> smart = new BooleanSetting("Режим выносливости").setValue(true);
    public final Setting<Boolean> smoothWastage = new BooleanSetting("Ослаблять под лимит").setValue(false).setVisible(() -> this.smart.getValue());
    public final Setting<Float> boundUp = new FloatSetting("Ограничить запас").range(0.F, .9F).setIncrement(.05F).setValue(.0F).setVisible(() -> this.smart.getValue());
    public final Setting<Boolean> ncpBypass = new BooleanSetting("Дисаблер Old-NCP").setValue(false).setVisible(() -> !this.smart.getValue());

    public final SelectSetting mode = new SelectSetting("Обход")
            .setValue("Matrix",
                    "Matrix/NCP",
                    "Vulcan/Grim/Intave",
                    "AAC/Другое");


    public TimerModule() {
        super("Timer", "Изменяет субъективное течение времени в игре", "Changes the subjective flow of time in game", ModuleCategory.Movement);
        registerComponent(timer, randomize, timeOut, timeOutMS, smart, smoothWastage, boundUp, ncpBypass, mode);
        mode.setSelected("Matrix");
    }

    private static StopWatch afkWait = new StopWatch();
    private static StopWatch timeOutWait = new StopWatch();
    private boolean afk = true;
    private float yaw,pitch;
    private static float forceTimer = 1;
    private boolean smartGo,critical,panicRegen;
    public double percent = 1.F, prevPercent = 1.F;
    private boolean isRegening = false;
    public static boolean forceWastage = false;
    private static void forceWastage() {
        forceWastage = true;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (this.forceWastage && this.smartGo && event.getPacket() instanceof SPlayerPositionLookPacket TP) {
            if (mc.player.getDistanceToCoord(TP.getX(), TP.getY(), TP.getZ()) > 20 || this.isNcpTimerDisabler()) return;
            this.panicRegen = true;
            this.smartGo = false;
            this.percent /= 1.5f;
            this.critical = true;
        }
    }

    private double[] timerArgs(final String mode, final boolean flaged, final double tpsPC20, final double timerSpeed) {
        double chargeSP = 1.D, dropSP = 0.D, regenSP = 0.D;
        switch (mode) {
            case "Matrix" -> {
                chargeSP = .035D * tpsPC20;
                dropSP = .0222D * timerSpeed / tpsPC20;
                regenSP = .5D * tpsPC20;
            }
            case "Matrix/NCP" -> {
                chargeSP = .06D * tpsPC20;
                dropSP = .046D * timerSpeed / tpsPC20;
                regenSP = .75D * tpsPC20;
            }
            case "Vulcan/Grim/Intave" -> {
                chargeSP = .45D * tpsPC20;
                dropSP = .11D * timerSpeed / tpsPC20;//055
                regenSP = 1.D * tpsPC20;
            }
            case "AAC/Другое" -> {
                chargeSP = 1.D * tpsPC20;
                dropSP = .046D * timerSpeed / tpsPC20;
                regenSP = .85D * tpsPC20;
            }
        }
        if (flaged) {
            chargeSP /= 1.425D;
            regenSP /= 3.5D;
        }
        return new double[] {chargeSP,dropSP,regenSP};
    }

    private boolean updateAfkStatus(final StopWatch timer) {
        if (!timer.finished(100L)) {
            this.yaw = mc.player.lastReportedYaw;
            this.pitch = mc.player.lastReportedPitch;
        }
        final double player3DSpeed = Math.sqrt(mc.player.motion.x * mc.player.motion.x + mc.player.motion.y * mc.player.motion.y + mc.player.motion.z * mc.player.motion.z);
        boolean FORCE_RECHARGE = mc.player.ticksExisted == 1 || !mc.player.isAlive();
        if (FORCE_RECHARGE || this.yaw == mc.player.lastReportedYaw && this.pitch == (float)mc.player.lastReportedPitch && (player3DSpeed == 0.0784000015258789 || player3DSpeed == 0) && !this.forceWastage) {
            if (timer.finished(150L)) {
                timer.reset();
                this.afk = true;
            }
        } else {
            this.afk = false;
            timer.reset();
        }
        if (mc.player.ticksExisted == 1 || !mc.player.isAlive()) {
            this.afk = true;
            this.percent = 1;
            this.critical = false;
        }
        return this.afk;
    }
    private double updateTimerPercent(final double[] args,final boolean isAfk,float boundUp) {
        if (!isAfk && this.percent < 1.F && !this.isEnabled()) {
            final double upped = (float) (args[0] * .2D) / 7.D;
            if ((args[2] / (1.D - boundUp)) > upped + this.percent - boundUp && !this.forceWastage)
                this.percent += upped;
            this.critical = false;
        }
        if (this.panicRegen && this.percent == 1.F) {
            this.panicRegen = false;
            this.critical = false;
        }
        if (this.percent < 1.F && isAfk) {
            this.percent += (args[0] / (1-boundUp));
            this.isRegening = true;
            this.critical = false;
        }
        if (!isAfk && percent > boundUp && (this.smartGo || this.forceWastage)) {
            this.percent = Math.max(this.percent - args[1], boundUp);
        }
        this.percent = MathUtil.clamp(0.F, 1.F, this.percent);
        return this.percent;
    }

    private boolean canDisableByTimeOut(final boolean timeOutEnabled,final int timeOutMS) {
        return this.isEnabled() && timeOutEnabled && this.timeOutWait.finished(timeOutMS);
    }

    private boolean canAbuseTimerSpeed(final boolean isSmart) {
        boolean FORCE_STOP = false;
        return this.forceWastage && this.smartGo || this.isEnabled() && (this.smartGo && !this.critical || !isSmart) && !FORCE_STOP;
    }

    private double getTimerBoostSpeed(boolean can,final boolean smart,final float boundUp) {
        double speed = 1.D;
        if (can) {
            final float timer = this.timer.getValue();
            speed = smart && this.smoothWastage.getValue() ? 1.D + (timer - 1.D) * MathUtil.clamp(this.percent - boundUp, 0.D, 1.D) : timer;
            final float randomVal = this.randomize.getValue();
            final double randomize = -randomVal + randomVal * Math.random() * 2.D;
            if (speed + randomize > 1 || speed < 1)
                speed += randomize;
            if (this.forceWastage) speed = this.forceTimer;
        }
        return can ? MathUtil.clamp(speed, .1D, 20.D) : 1.D;
    }

    private boolean isNcpTimerDisabler() {
        return ncpBypass.getValue() && !smart.getValue();
    }

    public void onAlwaysUpdate() {
        if (this.afkWait == null || mc.player == null) return;
        final boolean smartTimer = this.smart.getValue();
        final float boundUp = this.boundUp.getValue();
        final boolean canABB = this.canAbuseTimerSpeed(smartTimer);
        double speed = this.getTimerBoostSpeed(canABB, smartTimer, boundUp);
        this.prevPercent = this.percent;
        if (smartTimer) {
            final double[] ARGS = this.timerArgs(mode.getSelected(),this.panicRegen, /*Client.instance.getTpsDetect().getTPSServer() / 20.0F*/1.F,speed - 1);
            this.smartGo = this.updateTimerPercent(ARGS,this.updateAfkStatus(this.afkWait),boundUp) > boundUp && !this.afk && !this.critical;
        } else if (this.percent != 1.F) {
            this.smartGo = false;
            this.percent = 1.F;
        }
        if (this.prevPercent != this.percent && this.percent <= this.boundUp.getValue()) this.onUpdatedTriggerMinPercent();
        else if (this.prevPercent != this.percent && this.percent == 1.F) this.onUpdatedTriggerMaxPercent();
        if (this.canDisableByTimeOut(this.timeOut.getValue(), this.timeOutMS.getValue().intValue())) {
            this.switchState(false, true);
            return;
        }
        mc.timer.speed = (float) speed;
        this.forceWastage = false;
    }

    private void onUpdatedTriggerMinPercent() {

    }

    private void onUpdatedTriggerMaxPercent() {

    }

    @Override
    public void onEnable() {
        this.timeOutWait.reset();
    }

    @Override
    public void onDisable() {
        mc.timer.speed = 1.F;
    }
}
