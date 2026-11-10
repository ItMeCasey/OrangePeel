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

    private static String buttonMethod(Object mouse) {
        return find(mouse.getClass(), "onPress") != null ? "onPress" : "onButton";
    }

    private static void button(double x, double y, boolean down) {
        move(x, y);
        Object mouse = Minecraft.getInstance().mouseHandler;
        String name = buttonMethod(mouse);
        boolean legacy = find(mouse.getClass(), name).getParameterCount() == 4;
        int action = down ? 1 : 0;
        call(mouse, name, legacy ? new int[] {0, action, 0} : new int[] {action}, 0);
    }

    /** The mouse button goes down here. Release it a few ticks later with {@link #release}, like a real click. */
    static void press(double x, double y) {
        button(x, y, true);
    }

    static void release(double x, double y) {
        button(x, y, false);
    }

    static void click(double x, double y) {
        press(x, y);
        release(x, y);
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

    /** One key with modifier bits held (2 = control), pressed and released, for chords such as Ctrl+F. */
    static void chord(int key, int modifiers) {
        Object keyboard = Minecraft.getInstance().keyboardHandler;
        boolean legacy = find(keyboard.getClass(), "keyPress").getParameterCount() == 5;
        try {
            for (int action = 1; action >= 0; action--) {
                Method m = find(keyboard.getClass(), "keyPress");
                Class<?>[] types = m.getParameterTypes();
                Object[] args = new Object[types.length];
                int next = 0;
                for (int i = 0; i < types.length; i++) {
                    if (types[i] == long.class) {
                        args[i] = window();
                    } else if (types[i] == int.class) {
                        args[i] = legacy ? new int[] {key, 0, action, modifiers}[next++] : action;
                    } else {
                        Constructor<?> c = types[i].getDeclaredConstructors()[0];
                        c.setAccessible(true);
                        Object[] cargs = new Object[c.getParameterCount()];
                        for (int k = 0; k < cargs.length; k++) {
                            cargs[k] = k == 0 ? key : (k == cargs.length - 1 ? modifiers : 0);
                        }
                        args[i] = c.newInstance(cargs);
                    }
                }
                m.invoke(keyboard, args);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalStateException("UiInput chord", e);
        }
    }

    /** Types like a keyboard does: for each character a key press, the character, and the key release. */
    static void type(String text) {
        Object keyboard = Minecraft.getInstance().keyboardHandler;
        boolean legacy = find(keyboard.getClass(), "charTyped").getParameterCount() == 3;
        text.codePoints().forEach(cp -> {
            int key = cp == ' ' ? 32 : Character.toUpperCase(cp);
            // On 26.3 the key D toggles fullscreen (key.fullscreen defaults to 68), which resizes the window and rebuilds the
            // open screen; the character is still typed, only its key press is left out.
            boolean press = key != 68;
            if (press) {
                call(keyboard, "keyPress", find(keyboard.getClass(), "keyPress").getParameterCount() == 5
                        ? new int[] {key, 0, 1, 0} : new int[] {1}, key);
            }
            call(keyboard, "charTyped", legacy ? new int[] {cp, 0} : new int[] {0}, cp);
            if (press) {
                call(keyboard, "keyPress", find(keyboard.getClass(), "keyPress").getParameterCount() == 5
                        ? new int[] {key, 0, 0, 0} : new int[] {0}, key);
            }
        });
    }
}
