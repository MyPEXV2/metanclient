package relake.module.implement.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.*;
import relake.Client;
import relake.event.EventHandler;
import relake.event.impl.player.PlayerEvent;
import relake.friend.Friend;
import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.point.PointTrace;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.MultiSelectSetting;
import relake.settings.implement.SelectSetting;
import relake.settings.implement.StringSetting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StreamerModeModule extends Module {


    public final MultiSelectSetting hideNames = new MultiSelectSetting("Кого скрывать")
            .setValue("Себя",
                    "Друзей");

    private Setting<String> replaceNameTo = new StringSetting("Заменять имена на:").setValue("Secret").setVisible(() -> hideNames.isSelected("Себя") || hideNames.isSelected("Друзей"));
    private final Setting<Boolean> hideCoords = new BooleanSetting("Скрыть координаты").setValue(false);
    private final Setting<Boolean> onlyHomePointNear = new BooleanSetting("Только возле поинта дома").setValue(false).setVisible(() -> hideCoords.getValue());
    private final Setting<Boolean> chatInfoHider = new BooleanSetting("Кнопка чат приватности").setValue(false);

    public StreamerModeModule() {
        super("Streamer Mode", "Скрывает приватную информацию для безопасности", "Hides private information for security", ModuleCategory.Misc);
        registerComponent(hideNames, replaceNameTo, hideCoords, onlyHomePointNear, chatInfoHider);

        hideNames.getSelected().add("Себя");
        hideNames.getSelected().add("Друзей");

    }

    private final String[] containsLower = new String[]{"дом", "хата", "бомжатник", "лагерь", "хом", "home", "dom", "hata", "Death", "база", "base", "казарма", "хуй", "пизда", "коробка", "сундуки"};

    private List<String> int010Collect() {
        return Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    }

    private boolean wasNearedHomePointUpdated;

    private boolean hasHomePointInNear(double distanceIn) {
        final List<PointTrace> pointsList = Client.instance.pointsManager.traces;
        return Arrays.stream(containsLower).anyMatch(contain -> pointsList.stream().filter(point -> mc.player.getDistanceToCoord(point.x, point.y, point.z) <= distanceIn).anyMatch(point -> point.name.toLowerCase().contains(contain)));
    }

    @Override
    public void onEnable() {
        wasNearedHomePointUpdated = false;
    }

    @Override
    public void onDisable() {
        wasNearedHomePointUpdated = false;
    }

    @EventHandler
    public void onUpdate(PlayerEvent playerEvent) {
        wasNearedHomePointUpdated = hideCoords.getValue() && onlyHomePointNear.getValue() && hasHomePointInNear(80.D);
    }

    private String getUnrecursedEntityNetName(PlayerEntity player) {
        return player == null || player.getGameProfile() == null ? "Unnamed" : player.getGameProfile().getName();
    }

    public String disguiseCoordsString(String coordsValuesNotAppend) {
        if (this.isEnabled() && this.hideCoords.getValue() && (!onlyHomePointNear.getValue() || wasNearedHomePointUpdated)) {
            coordsValuesNotAppend = coordsValuesNotAppend.replace("-", "").replace(".", "").replace(",", "");
            for (String numString : int010Collect()) coordsValuesNotAppend = coordsValuesNotAppend.replaceAll(numString, "#");
            while (coordsValuesNotAppend.contains("##")) coordsValuesNotAppend = coordsValuesNotAppend.replaceAll("##", "#");
        }
        return coordsValuesNotAppend;
    }

    public String disguiseNamesString(String name) {
        if (this.isEnabled() && name != null && !name.isEmpty()) {
            if (this.hideNames.isSelected("Себя") && mc.player != null) name = name.replace(getUnrecursedEntityNetName(mc.player), replaceNameTo.getValue());
            if (this.hideNames.isSelected("Друзей")) {
                for (String friendName : Client.instance.friendManager.friends.stream().map(Friend::getName).toList()) name = name.replace(friendName, replaceNameTo.getValue());
            }
        }
        return name;
    }

    public ITextComponent disguiseNamesTextComponent(ITextComponent textComponent) {
        if (this.isEnabled() && textComponent != null) {
            final List<String> stringsToReplace = new ArrayList<>();
            if (this.hideNames.isSelected("Себя") && mc.player != null)
                stringsToReplace.add(getUnrecursedEntityNetName(mc.player));
            if (this.hideNames.isSelected("Друзей"))
                stringsToReplace.addAll(Client.instance.friendManager.friends.stream().map(Friend::getName).toList());
            final String replaceTo = this.replaceNameTo.getValue();
            for (final String string : stringsToReplace) {
                ITextComponent textComponentReplace = new StringTextComponent("");
                if (textComponent.getSiblings().isEmpty()) {
                    Style styleComponent = textComponent.getStyle();
                    textComponentReplace = new StringTextComponent(textComponent.getString().replaceAll(string, replaceTo)).mergeStyle(styleComponent);
                } else {
                    for (ITextComponent symbling : textComponent.getSiblings()) {
                        if (symbling.getString().contains(string)) {
                            if (symbling.getSiblings().isEmpty()) textComponentReplace.getSiblings().add(new StringTextComponent(symbling.getString().replaceAll(string, replaceTo)).mergeStyle(symbling.getStyle()));
                            else {
                                for (ITextComponent parrentSymblingL1 : symbling.getSiblings()) {
                                    if (parrentSymblingL1.getSiblings().isEmpty()) textComponentReplace.getSiblings().add(new StringTextComponent(parrentSymblingL1.getString().replaceAll(string, replaceTo)).mergeStyle(parrentSymblingL1.getStyle()));
                                    else {
                                        for (ITextComponent parrentSymblingL2 : parrentSymblingL1.getSiblings()) {
                                            if (parrentSymblingL2.getSiblings().isEmpty()) textComponentReplace.getSiblings().add(parrentSymblingL2);
                                            else textComponentReplace.getSiblings().add(new StringTextComponent(parrentSymblingL2.getString().replaceAll(string, replaceTo)).mergeStyle(parrentSymblingL2.getStyle()));
                                        }
                                    }
                                }
                            }
                        } else textComponentReplace.getSiblings().add(symbling);
                    }
                }
                textComponent = textComponentReplace;
            }

        }
        return textComponent;
    }

    public boolean enableSecretHiderInChatInputField() {
        return this.isEnabled() && this.chatInfoHider.getValue();
    }
}