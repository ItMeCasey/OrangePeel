package com.itmecasey.orangepeel;

import java.util.List;

/**
 * What a food gives, read from the game's own food data so modded foods need no support.
 *
 * @param id         registry id, used to tell foods apart ("minecraft:apple")
 * @param name       display name
 * @param nutrition  hunger points restored (2 points fill one shank)
 * @param saturation saturation points added (absolute, not the old 1.21.1 modifier)
 * @param eatTicks   ticks the food takes to eat
 * @param effects    status effects the food can apply
 */
public record FoodInfo(String id, String name, int nutrition, float saturation, int eatTicks, List<EffectInfo> effects) {
    public FoodInfo {
        effects = List.copyOf(effects);
    }

    /** Fullness the food delivers in total: hunger points plus saturation points. */
    public float score() {
        return nutrition + saturation;
    }
}
