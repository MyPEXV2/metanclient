package relake.draggable.implement;

import com.mojang.blaze3d.matrix.MatrixStack;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.draggable.Draggable;
import relake.module.implement.misc.EventTimerModule;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;

public class EventTimerDraggable extends Draggable {
    
    public EventTimerDraggable() {
        super("EventTimer", 100, 200, 200, 30);
    }
    
    @Override
    public boolean visible() {
        EventTimerModule eventTimerModule = Client.instance.moduleManager.eventTimerModule;
        return eventTimerModule != null && eventTimerModule.isEnabled() 
            && Client.instance.moduleManager.hudModule.selectComponent.isSelected("EventTimer") 
            && Client.instance.moduleManager.hudModule.isEnabled();
    }
    
    @Override
    public void tick() {
        EventTimerModule eventTimerModule = Client.instance.moduleManager.eventTimerModule;
        if (eventTimerModule != null) {
            String text = eventTimerModule.getFormattedTime();
            float textWidth = Render2D.size(FontRegister.Type.BOLD, 13).getWidth(text);
            this.width = Math.max(this.defaultWidth, textWidth + 20);
        }
    }
    
    @Override
    public void update() {}
    
    @Override
    public void render(MatrixStack matrixStack, float partialTicks) {
        EventTimerModule eventTimerModule = Client.instance.moduleManager.eventTimerModule;
        if (eventTimerModule == null) return;
        
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        int padding = 7;
        int borderRadius = 10;
        int outlineOffset = 8;
        float fontSize = 2f;
        
        String text = eventTimerModule.getFormattedTime();
        
        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);
        box.expand(Side.ALL, padding);
        box.quad(borderRadius, 0xB70E0E0F);
        box.quad(borderRadius, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));
        
        ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - outlineOffset, y - outlineOffset, width + outlineOffset * 2, height + outlineOffset * 2);
        boxOutLine.outlineHud(borderRadius, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));
        
        Render2D.size(FontRegister.Type.BOLD, 13).string(matrixStack, text, box.x + padding, box.y + padding + fontSize, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 255));
    }
}

