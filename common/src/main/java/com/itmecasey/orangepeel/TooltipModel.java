package com.itmecasey.orangepeel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds the food tooltip as plain lines with a tone, so the wording and numbers are unit tested without the game.
 * The client layer turns each tone into a text color.
 */
public final class TooltipModel {
    public static final int BAR_SEGMENTS = 10;
    public static final char BAR_FULL = '█';
    public static final char BAR_EMPTY = '░';

    public enum Tone { INFO, HUNGER, SATURATION, GOOD, BEST, WARN, BAD, DIM }

    /**
     * @param graphic true for lines the drawn tooltip block repeats (hunger and saturation numbers, the text bar); they
     *                are removed when the block is drawn and stay as the plain-text fallback otherwise
     */
    public record Line(String text, Tone tone, boolean graphic) {
        public Line(String text, Tone tone) {
            this(text, tone, false);
        }
    }

    private TooltipModel() {
    }

    /**
     * @param player   the local player, or null when unknown (then waste and fullness lines are skipped)
     * @param carried  foods in the player's inventory (the held food may be among them)
     */
    public static List<Line> build(FoodInfo food, PlayerState player, List<FoodInfo> carried) {
        List<Line> lines = new ArrayList<>();
        lines.add(new Line("Hunger +" + trim(food.nutrition() / 2f) + "  Saturation +" + trim(food.saturation()),
                Tone.HUNGER, true));

        if (Config.showComparator) {
            MealComparator.Result r = MealComparator.compare(food, carried);
            if (r.others() > 0) {
                if (r.best()) {
                    lines.add(new Line("Your best food (beats " + r.betterThan() + " of your other " + r.others()
                            + ")", Tone.BEST));
                } else {
                    lines.add(new Line("Better than " + r.betterThan() + " of your other " + r.others()
                            + (r.others() == 1 ? " food" : " foods"), Tone.INFO));
                    int filled = Math.round(r.fractionOfBest() * BAR_SEGMENTS);
                    lines.add(new Line(bar(filled) + " " + Math.round(r.fractionOfBest() * 100) + "% of "
                            + r.bestFood().name() + " (" + trim(r.deltaVsBest()) + ")", Tone.DIM, true));
                }
            }
        }

        if (player != null && Config.showWaste) {
            int waste = FoodMath.wastedHunger(player.foodLevel(), food.nutrition());
            if (waste > 0) {
                lines.add(new Line("Wastes " + trim(waste / 2f) + (waste == 2 ? " shank" : " shanks")
                        + " of hunger if eaten now", Tone.WARN));
            }
        }

        if (Config.showEffects) {
            for (EffectInfo e : food.effects()) {
                lines.add(new Line(effectText(e), e.beneficial() ? Tone.GOOD : Tone.BAD));
            }
        }

        if (player != null && Config.showFullness) {
            float seconds = FoodMath.fullnessSeconds(player, food, FoodMath.AVERAGE_EXHAUSTION_PER_SECOND);
            if (seconds >= 30f) {
                lines.add(new Line("Keeps you full for about " + minutes(seconds), Tone.SATURATION));
            }
        }
        return lines;
    }

    public static String bar(int filled) {
        int f = Math.max(0, Math.min(BAR_SEGMENTS, filled));
        return String.valueOf(BAR_FULL).repeat(f) + String.valueOf(BAR_EMPTY).repeat(BAR_SEGMENTS - f);
    }

    public static String effectText(EffectInfo e) {
        StringBuilder sb = new StringBuilder(e.name());
        if (e.amplifier() > 0) {
            sb.append(' ').append(roman(e.amplifier() + 1));
        }
        sb.append(" (").append(clock(e.durationTicks())).append(')');
        if (e.chance() < 0.999f) {
            sb.append(' ').append(Math.round(e.chance() * 100)).append("%");
        }
        return sb.toString();
    }

    /** m:ss for a tick count. */
    public static String clock(int ticks) {
        int total = Math.max(0, ticks) / 20;
        return (total / 60) + ":" + String.format(Locale.ROOT, "%02d", total % 60);
    }

    public static String minutes(float seconds) {
        int m = Math.max(1, Math.round(seconds / 60f));
        return m + (m == 1 ? " minute" : " minutes");
    }

    public static String roman(int n) {
        String[] r = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n >= 1 && n <= r.length ? r[n - 1] : Integer.toString(n);
    }

    /** One decimal place, no trailing ".0". */
    public static String trim(float v) {
        float rounded = Math.round(v * 10f) / 10f;
        if (rounded == (long) rounded) {
            return Long.toString((long) rounded);
        }
        return String.format(Locale.ROOT, "%.1f", rounded);
    }
}
