package relake.module.implement.misc;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPickItemPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import relake.common.util.ChatUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.KeyboardEvent;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.KeySetting;
import relake.settings.implement.SelectSetting;

import java.util.List;
import java.util.stream.IntStream;

public class FTHelperModule extends Module {

    public final SelectSetting mode = new SelectSetting("Мод")
            .setValue("NBT",
                    "Предмет");

    public Setting<Integer> desorientationKey = new KeySetting("Дезориентация").setValue(-1);
    public Setting<Integer> trapKey = new KeySetting("Трапка").setValue(-1);
    public BooleanSetting trapDelay = new BooleanSetting("Рядом с игроком");
    public Setting<Integer> plastKey = new KeySetting("Пласт").setValue(-1);
    public Setting<Integer> sheerdustKey = new KeySetting("Явная пыль").setValue(-1);
    public Setting<Integer> godsauraKey = new KeySetting("Божья аура").setValue(-1);
    public BooleanSetting autoGodsaura = new BooleanSetting("Авто использование");
    public Setting<Integer> freezeballKey = new KeySetting("Снежок заморозки").setValue(-1);
    public Setting<Integer> acidKey = new KeySetting("Серная кислота").setValue(-1);
    public Setting<Integer> burpKey = new KeySetting("Отрыжка").setValue(-1);
    public Setting<Integer> flashKey = new KeySetting("Флеш").setValue(-1);
    public Setting<Integer> flareKey = new KeySetting("Вспышка").setValue(-1);

    private final List<KeyAction> keyActions = List.of(
            new KeyAction(desorientationKey, "desorientation", Items.ENDER_EYE),
            new KeyAction(plastKey, "stratum", Items.DRIED_KELP),
            new KeyAction(sheerdustKey, "sheerdust", Items.SUGAR),
            new KeyAction(godsauraKey, "godsaura", Items.PHANTOM_MEMBRANE),
            new KeyAction(freezeballKey, "freezeball", Items.SNOWBALL),
            new KeyAction(acidKey, "potion-acid", Items.SPLASH_POTION),
            new KeyAction(burpKey, "potion-burp", Items.SPLASH_POTION),
            new KeyAction(flashKey, "potion-flash", Items.SPLASH_POTION),
            new KeyAction(flareKey, "potion-flare", Items.SPLASH_POTION),
            new KeyAction(trapKey, "trap", Items.NETHERITE_SCRAP)
    );


    private boolean using;
    private int index = 0;

    private boolean waitingForTrap = false;
    private final StopWatch stopWatch = new StopWatch();

    public FTHelperModule() {
        super("FT Helper", "Всячески упрощает игру на сервере FunTime путём упрощения действий", "Simplifies the game on the Fun Time server in every possible way by simplifying the actions", ModuleCategory.Misc);
        registerComponent(mode, desorientationKey, trapKey, trapDelay, plastKey, sheerdustKey, autoGodsaura, freezeballKey, acidKey, burpKey, flareKey, flashKey);
        mode.setSelected("NBT");
    }

    @EventHandler
    public void onUpdate(TickEvent event) {
        if (autoGodsaura.getValue()) {
            EffectInstance weakness = mc.player.getActivePotionEffect(Effects.WEAKNESS);
            if (weakness != null && weakness.getAmplifier() == 2) {
                boolean onCooldown = keyActions.stream()
                        .filter(action -> action.getTag().equals("godsaura"))
                        .findFirst()
                        .map(action -> mc.player.getCooldownTracker().hasCooldown(action.getItem()))
                        .orElse(false);
                if (!onCooldown) {
                    KeyAction godsauraAction = keyActions.stream()
                            .filter(action -> action.getTag().equals("godsaura"))
                            .findFirst()
                            .orElse(null);
                    if (godsauraAction != null) {
                        useSilent(godsauraAction);
                    }
                }
            }
        }

        if (waitingForTrap) {
            if (stopWatch.finished(5000)) {
                waitingForTrap = false;
                ChatUtil.send("Трапка отменена из-за отсутствия цели.");
                return;
            }

            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof PlayerEntity && entity != mc.player) {
                    double distance = mc.player.getDistance(entity);
                    if (distance <= 1.0) {
                        KeyAction trapAction = keyActions.stream()
                                .filter(action -> action.getTag().equals("trap"))
                                .findFirst()
                                .orElse(null);
                        if (trapAction != null) {
                            useSilent(trapAction);
                            waitingForTrap = false;
                            ChatUtil.send("Трапка использована, цель найдена.");
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChange(PacketEvent e) {
        if (mc.player == null || mc.world == null) return;

        if (e.getPacket() instanceof SHeldItemChangePacket) {
            if (using) {
                index++;
                e.cancel();
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.playerController.syncCurrentPlayItem();
                if (index >= 2) {
                    index = 0;
                    using = false;
                }
            }
        }
    }

    @EventHandler
    public void onKey(KeyboardEvent event) {
        int pressedKey = event.getKey();

        if (pressedKey == trapKey.getValue()) {
            if (trapDelay.getValue()) {
                waitingForTrap = true;
                stopWatch.reset();
                ChatUtil.send("Подойдите к игроку.");
            } else {
                KeyAction trapAction = keyActions.stream()
                        .filter(action -> action.getTag().equals("trap"))
                        .findFirst()
                        .orElse(null);
                if (trapAction != null) {
                    useSilent(trapAction);
                }
            }
        } else {
            keyActions.stream()
                    .filter(action -> pressedKey == action.getKeySetting().getValue())
                    .findFirst()
                    .ifPresent(this::useSilent);
        }
    }

    private void useSilent(KeyAction action) {
        IntStream.range(0, mc.player.inventory.getSizeInventory())
                .filter(i -> {
                    ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
                    if (mode.isSelected("NBT")) {
                        return itemStack.hasTag()
                                && !mc.player.getCooldownTracker().hasCooldown(itemStack.getItem())
                                && action.getTag().equals(itemStack.getTag().getString("don-item"));
                    } else {
                        Item item = action.getItem();
                        return itemStack.getItem() == item
                                && !mc.player.getCooldownTracker().hasCooldown(item);
                    }
                })
                .findFirst()
                .ifPresent(this::silentUse);
    }

    private void silentUse(int slot) {
        using = true;
        int currentSlot = mc.player.inventory.currentItem;
        mc.playerController.onStoppedUsingItem(mc.player);
        if (slot < 9) {
            mc.player.inventory.currentItem = slot;
            mc.playerController.syncCurrentPlayItem();
            mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
            mc.player.swingArm(Hand.MAIN_HAND);
        } else {
            mc.player.connection.sendPacket(new CPickItemPacket(slot));
            mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
            mc.player.swingArm(Hand.MAIN_HAND);
            mc.player.connection.sendPacket(new CPickItemPacket(slot));
        }
        mc.player.inventory.currentItem = currentSlot;
        mc.playerController.syncCurrentPlayItem();
    }

    @Getter
    private static class KeyAction {
        private final Setting<Integer> keySetting;
        private final String tag;
        private final Item item;

        public KeyAction(Setting<Integer> keySetting, String tag, Item item) {
            this.keySetting = keySetting;
            this.tag = tag;
            this.item = item;
        }
    }
}

