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
}
