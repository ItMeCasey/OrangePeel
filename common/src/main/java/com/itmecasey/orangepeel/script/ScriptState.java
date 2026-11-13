package com.itmecasey.orangepeel.script;

import com.itmecasey.orangepeel.FoodInfo;
import com.itmecasey.orangepeel.PlayerState;
import java.util.List;

/** What scripts can read from the client right now. The client wiring implements it; tests use a fake. */
public interface ScriptState {
    /** The local player's hunger, saturation, exhaustion and health, or null outside a world. */
    PlayerState player();

    /** The food in the player's hands, or null. */
    FoodInfo held();

    /** One entry per food stack in the player's inventory. */
    List<FoodInfo> carried();
}
