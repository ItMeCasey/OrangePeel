package com.itmecasey.orangepeel;

/**
 * The parts of the local player the overlays need.
 *
 * @param hudVisible true when the vanilla food bar is on screen (survival or adventure, not riding)
 */
public record PlayerState(int foodLevel, float saturation, float exhaustion, float health, float maxHealth,
                          float absorption, boolean hudVisible) {
}
