package com.itmecasey.orangepeel;

/**
 * One status effect a food can apply.
 *
 * @param name          display name without a level, for example "Hunger"
 * @param amplifier     0 for level I
 * @param durationTicks effect length in ticks
 * @param chance        0..1 probability that the effect is applied
 * @param beneficial    true for good effects, false for harmful ones
 */
public record EffectInfo(String name, int amplifier, int durationTicks, float chance, boolean beneficial) {
}
