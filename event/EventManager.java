package relake.event;

import relake.Client;
import relake.common.InstanceAccess;
import relake.common.util.ColorUtil;
import relake.event.api.Invoker;
import relake.event.impl.render.RenderPre2DEvent;
import relake.event.impl.render.ScreenRenderEvent;
import relake.render.display.Render2D;
import relake.render.display.shape.Blur;
import relake.render.display.shape.ShapeRenderer;
import relake.shader.ShaderRegister;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static relake.render.display.font.FontRegister.Type.BOLD;

public class EventManager implements InstanceAccess {
    private final Map<Class<? extends Event>, List<RegisteredMethod>> eventListeners = new HashMap<>();

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (isEventHandlerMethod(method)) {
                Class<? extends Event> eventType = getEventType(method);
                eventListeners
                        .computeIfAbsent(eventType, k -> new ArrayList<>())
                        .add(new RegisteredMethod(listener, method));
            }
        }
    }

    public void unregister(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (isEventHandlerMethod(method)) {
                Class<? extends Event> eventType = getEventType(method);
                List<RegisteredMethod> registeredMethods = eventListeners.get(eventType);
                if (registeredMethods != null) {
                    registeredMethods.removeIf(registeredMethod -> registeredMethod.matches(listener, method));
                }
            }
        }
    }

    private List<Invoker> getListeners(Class<? extends Event> eventType) {
        List<Invoker> invokers = new ArrayList<>();
        List<RegisteredMethod> registeredMethods = eventListeners.get(eventType);
        if (registeredMethods != null) {
            for (RegisteredMethod registeredMethod : registeredMethods) {
                invokers.add(event -> {
                    try {
                        registeredMethod.method.invoke(registeredMethod.listener, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.printf("Error invoking method: %s in %s; Exception: \n%s\n", registeredMethod.listener.getClass().getSimpleName(), registeredMethod.method.getName(), e.getMessage());
                    }
                });
            }
        }
        return invokers;
    }

    private boolean isEventHandlerMethod(Method method) {
        return method.isAnnotationPresent(EventHandler.class) &&
                method.getParameterCount() == 1 &&
                Event.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    private Class<? extends Event> getEventType(Method method) {
        return method.getParameterTypes()[0].asSubclass(Event.class);
    }

    public <T extends Event> T fireEvent(T event) {
        if (mc.world == null
                || mc.player == null) {
            return event;
        }

        if (event instanceof ScreenRenderEvent e) {
            Blur.stencilFramebuffer = ShaderRegister.createFrameBuffer(Blur.stencilFramebuffer);

            Blur.stencilFramebuffer.framebufferClear();
            Blur.stencilFramebuffer.bindFramebuffer(false);

            for (ShapeRenderer.BlurData blurData : ShapeRenderer.BLUR_DATA_LIST) {
                blurData.shapeRenderer().quad(blurData.rounding(), ColorUtil.applyOpacity(-1, blurData.alpha()));
            }

            ShapeRenderer.BLUR_DATA_LIST.clear();

            Blur.stencilFramebuffer.unbindFramebuffer();
            Blur.renderBlur(e.getMatrixStack(), Blur.stencilFramebuffer.framebufferTexture, 3, 1);
        }

        for (Invoker invoker : getListeners(event.getClass())) {
            invoker.invoke(event);
        }

        return event;
    }

    private record RegisteredMethod(Object listener, Method method) {
        public boolean matches(Object listener, Method method) {
            return this.listener.equals(listener) && this.method.equals(method);
        }
    }
}