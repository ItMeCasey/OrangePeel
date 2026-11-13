package com.itmecasey.modjs.script;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/** Test stand-in with the same shape as ModJS's BindingProvider. */
@FunctionalInterface
public interface BindingProvider {
    void install(ScriptType type, Context cx, ScriptableObject scope);
}
