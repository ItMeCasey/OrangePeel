package com.itmecasey.modjs.script;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/** Test stand-in with the same shape as ModJS's ExtraBindings: providers register, a script scope is installed from them. */
public final class ExtraBindings {
    private static final List<BindingProvider> PROVIDERS = new ArrayList<>();

    private ExtraBindings() {
    }

    public static void register(BindingProvider provider) {
        PROVIDERS.add(provider);
    }

    public static void clear() {
        PROVIDERS.clear();
    }

    /** What ModJS does for each script: put the providers' globals into a fresh scope. */
    public static void install(ScriptType type, Context cx, ScriptableObject scope) {
        for (BindingProvider provider : PROVIDERS) {
            provider.install(type, cx, scope);
        }
    }
}
