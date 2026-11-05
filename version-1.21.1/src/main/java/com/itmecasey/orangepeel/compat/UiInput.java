package com.itmecasey.orangepeel.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;

/** Development only: feeds the game's own mouse and keyboard handlers (reflection, so it is the same on every version). */
final class UiInput {
    private UiInput() {
    }

    private static long window() {
        Object window = Minecraft.getInstance().getWindow();
        for (String name : new String[] {"handle", "getWindow"}) {
            try {
                Method m = window.getClass().getMethod(name);
                if (m.getReturnType() == long.class) {
                    return (long) m.invoke(window);
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return 0L;
    }

    private static Method find(Class<?> type, String name) {
        for (Method m : type.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }

    private static Object make(Class<?> type, int primary) throws ReflectiveOperationException {
        Constructor<?> c = type.getDeclaredConstructors()[0];
        c.setAccessible(true);
        Class<?>[] types = c.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            args[i] = i == 0 ? primary : 0;
        }
        return c.newInstance(args);
    }

    private static void call(Object target, String name, int[] ints, int primary) {
        try {
            Method m = find(target.getClass(), name);
            Class<?>[] types = m.getParameterTypes();
            Object[] args = new Object[types.length];
            int next = 0;
            for (int i = 0; i < types.length; i++) {
                if (types[i] == long.class) {
                    args[i] = window();
                } else if (types[i] == int.class) {
                    args[i] = ints[next++];
                } else {
                    args[i] = make(types[i], primary);
                }
            }
            m.invoke(target, args);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalStateException("UiInput " + name, e);
        }
    }

    /** Puts the cursor here (screenshot pixels) and lets the open screen see the move. */
    static void move(double x, double y) {
        Object mouse = Minecraft.getInstance().mouseHandler;
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                Field f = mouse.getClass().getDeclaredField(name);
                f.setAccessible(true);
                f.setDouble(mouse, name.equals("xpos") ? x : y);
            }
            Method m = find(mouse.getClass(), "onMove");
            if (m != null && m.getParameterCount() == 3 && m.getParameterTypes()[1] == double.class) {
                m.invoke(mouse, window(), x, y);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("UiInput move", e);
        }
    }

    static void click(double x, double y) {
        move(x, y);
        Object mouse = Minecraft.getInstance().mouseHandler;
        String name = find(mouse.getClass(), "onPress") != null ? "onPress" : "onButton";
        boolean legacy = find(mouse.getClass(), name).getParameterCount() == 4;
        call(mouse, name, legacy ? new int[] {0, 1, 0} : new int[] {1}, 0);
        call(mouse, name, legacy ? new int[] {0, 0, 0} : new int[] {0}, 0);
    }

    private static void key(Object keyboard, int key) {
        boolean legacy = find(keyboard.getClass(), "keyPress").getParameterCount() == 5;
        call(keyboard, "keyPress", legacy ? new int[] {key, 0, 1, 0} : new int[] {1}, key);
        call(keyboard, "keyPress", legacy ? new int[] {key, 0, 0, 0} : new int[] {0}, key);
    }

    /** Empties the focused text box (End, then Backspace many times), so a leftover search cannot spoil the typed one. */
    static void clear() {
        Object keyboard = Minecraft.getInstance().keyboardHandler;
        key(keyboard, 269);
        for (int i = 0; i < 40; i++) {
            key(keyboard, 259);
        }
    }

    static void type(String text) {
        Object keyboard = Minecraft.getInstance().keyboardHandler;
        boolean legacy = find(keyboard.getClass(), "charTyped").getParameterCount() == 3;
        text.codePoints().forEach(cp -> call(keyboard, "charTyped", legacy ? new int[] {cp, 0} : new int[] {0}, cp));
    }
}
