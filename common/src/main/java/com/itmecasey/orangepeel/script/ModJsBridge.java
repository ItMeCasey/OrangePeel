package com.itmecasey.orangepeel.script;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

/**
 * Gives ModJS client scripts a global {@code OrangePeel} (the {@link ScriptApi}). ModJS is optional, so everything here goes
 * through reflection: ModJS offers {@code ExtraBindings.register(BindingProvider)}, and the provider's
 * {@code install(type, cx, scope)} adds globals to every script scope. Without ModJS nothing is loaded and nothing fails.
 * OrangePeel is a client mod, so the global exists in client scripts only.
 */
public final class ModJsBridge {
    private static final Logger LOG = Logger.getLogger("orangepeel");

    private ModJsBridge() {
    }

    /** Installs the binding when ModJS is on the classpath; returns whether it did. */
    public static boolean install(ScriptApi api) {
        return install(api, "com.itmecasey.modjs.script.BindingProvider", "com.itmecasey.modjs.script.ExtraBindings");
    }

    /** As {@link #install(ScriptApi)} with the ModJS class names given, so a test can point at a class that is not there. */
    public static boolean install(ScriptApi api, String providerName, String extraName) {
        try {
            Class<?> provider = Class.forName(providerName);
            Class<?> extra = Class.forName(extraName);
            Class<?> context = Class.forName("org.mozilla.javascript.Context");
            Class<?> scriptable = Class.forName("org.mozilla.javascript.Scriptable");
            Class<?> scriptableObject = Class.forName("org.mozilla.javascript.ScriptableObject");
            Method javaToJs = context.getMethod("javaToJS", Object.class, scriptable);
            Method put = scriptableObject.getMethod("putProperty", scriptable, String.class, Object.class);
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "install" -> {
                        if (args != null && args.length == 3 && "CLIENT".equalsIgnoreCase(String.valueOf(args[0]))) {
                            put.invoke(null, args[2], "OrangePeel", javaToJs.invoke(null, api, args[2]));
                        }
                        return null;
                    }
                    case "toString" -> {
                        return "OrangePeel binding";
                    }
                    case "hashCode" -> {
                        return System.identityHashCode(proxy);
                    }
                    case "equals" -> {
                        return proxy == args[0];
                    }
                    default -> {
                        return null;
                    }
                }
            };
            Object binding = Proxy.newProxyInstance(provider.getClassLoader(), new Class<?>[] {provider}, handler);
            extra.getMethod("register", provider).invoke(null, binding);
            LOG.info("OrangePeel: ModJS client scripts can use the OrangePeel global");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            LOG.warning("OrangePeel: could not add the OrangePeel global to ModJS: " + e);
            return false;
        }
    }
}
