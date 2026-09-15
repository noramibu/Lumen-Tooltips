package me.noramibu.lumentooltips.config;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.sdl.SDLMouse;

public final class LumenInputBinding {
    public static final String UNBOUND = InputConstants.UNKNOWN.getName();
    public static final String LEFT_ALT = "key.keyboard.left.alt";
    public static final String LEFT_CONTROL = "key.keyboard.left.control";
    public static final String LEFT_SHIFT = "key.keyboard.left.shift";
    public static final String CONTROL_S = LEFT_CONTROL + "+key.keyboard.s";
    public static final String CONTROL_SPACE = LEFT_CONTROL + "+key.keyboard.space";

    private LumenInputBinding() {}

    public static String normalize(String keyName, String fallback) {
        if (keyName == null || keyName.isBlank()) {
            return fallback;
        }
        if (UNBOUND.equals(keyName)) {
            return UNBOUND;
        }
        List<String> keys = new ArrayList<>();
        for (String name : keyName.split("\\+")) {
            InputConstants.Key key = parse(name);
            if (key == InputConstants.UNKNOWN) {
                return fallback;
            }
            keys.add(key.getName());
        }
        return String.join("+", keys);
    }

    public static boolean isDown(String keyName) {
        String[] keyNames = keyName.split("\\+");
        for (String name : keyNames) {
            if (!isDown(parse(name))) {
                return false;
            }
        }
        return keyNames.length > 0;
    }

    private static boolean isDown(InputConstants.Key key) {
        if (key == InputConstants.UNKNOWN) {
            return false;
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            return (SDLMouse.SDL_GetMouseState(null, null) & (1 << (key.getValue() - 1))) != 0;
        }
        return InputConstants.isKeyDown(key.getValue());
    }

    public static MutableComponent displayName(String keyName) {
        if (UNBOUND.equals(keyName)) {
            return Component.translatable("key.keyboard.unknown");
        }
        MutableComponent name = Component.empty();
        String[] keys = keyName.split("\\+");
        for (int index = 0; index < keys.length; index++) {
            if (index > 0) {
                name.append(" + ");
            }
            name.append(parse(keys[index]).getDisplayName());
        }
        return name;
    }

    public static boolean matches(String keyName, KeyEvent event) {
        String[] keys = keyName.split("\\+");
        InputConstants.Key pressed = InputConstants.getKey(event);
        if (keys.length == 0 || !parse(keys[keys.length - 1]).getName().equals(pressed.getName())) {
            return false;
        }
        for (int index = 0; index < keys.length - 1; index++) {
            if (!modifierDown(parse(keys[index]), event.modifiers())) {
                return false;
            }
        }
        return true;
    }

    public static boolean includes(String keyName, KeyEvent event) {
        String released = InputConstants.getKey(event).getName();
        for (String name : keyName.split("\\+")) {
            if (parse(name).getName().equals(released)) {
                return true;
            }
        }
        return false;
    }

    public static String fromEvent(KeyEvent event) {
        return withModifiers(InputConstants.getKey(event), event.modifiers());
    }

    public static String fromEvent(MouseButtonEvent event) {
        return withModifiers(InputConstants.Type.MOUSE.getOrCreate(event.button()), event.modifiers());
    }

    public static boolean isModifier(KeyEvent event) {
        return modifierMask(InputConstants.getKey(event)) != 0;
    }

    private static String withModifiers(InputConstants.Key key, int modifiers) {
        if (modifierMask(key) != 0) {
            return key.getName();
        }
        List<String> keys = new ArrayList<>();
        addModifier(keys, modifiers, InputConstants.MOD_CONTROL, LEFT_CONTROL);
        addModifier(keys, modifiers, InputConstants.MOD_SHIFT, LEFT_SHIFT);
        addModifier(keys, modifiers, InputConstants.MOD_ALT, LEFT_ALT);
        keys.add(key.getName());
        return String.join("+", keys);
    }

    private static void addModifier(List<String> keys, int modifiers, int modifier, String keyName) {
        if ((modifiers & modifier) != 0) {
            keys.add(keyName);
        }
    }

    private static boolean modifierDown(InputConstants.Key key, int modifiers) {
        int mask = modifierMask(key);
        return mask == 0 ? isDown(key) : (modifiers & mask) != 0;
    }

    private static int modifierMask(InputConstants.Key key) {
        return switch (key.getValue()) {
            case InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL -> InputConstants.MOD_CONTROL;
            case InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT -> InputConstants.MOD_SHIFT;
            case InputConstants.KEY_LALT, InputConstants.KEY_RALT -> InputConstants.MOD_ALT;
            case InputConstants.KEY_LGUI, InputConstants.KEY_RGUI -> InputConstants.MOD_SUPER;
            default -> 0;
        };
    }

    private static InputConstants.Key parse(String keyName) {
        try {
            return InputConstants.getKey(keyName);
        } catch (IllegalArgumentException ignored) {
            return InputConstants.UNKNOWN;
        }
    }
}
