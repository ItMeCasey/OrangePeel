package com.itmecasey.orangepeel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ranks one food against the other foods the player carries. Foods are compared by {@link FoodInfo#score()}
 * (hunger plus saturation); equal scores tie.
 */
public final class MealComparator {
    private MealComparator() {
    }

    /**
     * @param others         how many other distinct foods the player carries
     * @param betterThan     how many of those the held food beats (strictly higher score)
     * @param best           true when no carried food scores higher
     * @param bestFood       the highest-scoring carried food (the held food itself when it is the best)
     * @param fractionOfBest held score divided by the best score, 0..1
     * @param deltaVsBest    held score minus best score, 0 or negative
     */
    public record Result(int others, int betterThan, boolean best, FoodInfo bestFood, float fractionOfBest,
                         float deltaVsBest) {
    }

    /** Distinct foods by id, first occurrence wins, with the held food included once. */
    public static List<FoodInfo> distinct(FoodInfo held, List<FoodInfo> carried) {
        Map<String, FoodInfo> byId = new LinkedHashMap<>();
        byId.put(held.id(), held);
        for (FoodInfo f : carried) {
            byId.putIfAbsent(f.id(), f);
        }
        return new ArrayList<>(byId.values());
    }

    public static Result compare(FoodInfo held, List<FoodInfo> carried) {
        List<FoodInfo> all = distinct(held, carried);
        FoodInfo best = held;
        int better = 0;
        for (FoodInfo f : all) {
            if (f.id().equals(held.id())) {
                continue;
            }
            if (held.score() > f.score()) {
                better++;
            }
            if (f.score() > best.score()) {
                best = f;
            }
        }
        float bestScore = best.score();
        float fraction = bestScore <= 0f ? 1f : Math.min(1f, held.score() / bestScore);
        return new Result(all.size() - 1, better, held.score() >= bestScore, best, fraction,
                Math.min(0f, held.score() - bestScore));
    }

    /** What a food would actually add right now: hunger points that fit, plus the saturation that fits. */
    public static float effectiveValue(PlayerState p, FoodInfo f) {
        int fitting = Math.min(f.nutrition(), FoodMath.MAX_FOOD - p.foodLevel());
        float satGain = FoodMath.saturationAfter(p.foodLevel(), p.saturation(), f.nutrition(), f.saturation())
                - Math.min(p.saturation(), FoodMath.foodAfter(p.foodLevel(), f.nutrition()));
        return Math.max(0, fitting) + Math.max(0f, satGain);
    }

    /**
     * The food that fits the player's hunger best right now: the highest effective value, and among equals the one that
     * wastes the least (lowest raw score). Null when the hunger bar is already full, so nothing helps.
     */
    public static FoodInfo bestForHunger(PlayerState p, List<FoodInfo> foods) {
        if (p.foodLevel() >= FoodMath.MAX_FOOD) {
            return null;
        }
        FoodInfo best = null;
        float bestValue = 0f;
        for (FoodInfo f : foods) {
            float v = effectiveValue(p, f);
            if (v > bestValue + 1e-4f || (best != null && Math.abs(v - bestValue) <= 1e-4f && v > 0f
                    && f.score() < best.score())) {
                best = f;
                bestValue = v;
            }
        }
        return best;
    }
}
