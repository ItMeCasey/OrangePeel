package com.itmecasey.orangepeel;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the settings file that versions 2026.11.1 to 2026.11.4 wrote ({@code config/orangepeel-client.properties}, the same
 * name and keys on every loader and Minecraft version) so a player's choices carry over to the .toml files.
 *
 * <p>{@link #read} never changes the old file. Only {@link #retire}, after the .toml files are written and checked, moves it to
 * a {@code .back} copy (with a header line) and removes the original; any failed check leaves the original where it is. The
 * loader layers call this only when the new .toml does not exist yet, so a .toml the player has edited is never overwritten.
 * Keys that were never renamed need no mapping; {@link #RENAMED} is where a future rename goes.
 */
public final class ConfigMigration {
    /** The old file's name, in the config folder. */
    public static final String OLD_FILE = "orangepeel-client.properties";
    /** Old key to new key. No key was ever renamed. */
    private static final Map<String, String> RENAMED = Map.of();

    /**
     * What the old file held.
     *
     * @param found    true when the old file exists (even if it could not be used)
     * @param clean    true when every line of the file was understood as it stands: no unreadable file, nothing unknown,
     *                 invalid or out of range
     * @param values   the values to import, by new key: Integer or Boolean, already within the range; keys that were
     *                 missing or invalid are left out so the default stays
     * @param messages one plain line for every key that was imported, clamped, invalid or ignored, and for a file that could
     *                 not be read
     */
    public record Result(boolean found, boolean clean, Map<String, Object> values, List<String> messages) {
    }

    private ConfigMigration() {
    }

    public static Result read(Path file) {
        List<String> messages = new ArrayList<>();
        Map<String, Object> values = new LinkedHashMap<>();
        if (file == null || !Files.isRegularFile(file)) {
            return new Result(false, false, values, messages);
        }
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException | IllegalArgumentException e) {
            // Not UTF-8 text or a broken escape: Properties.load may have filled some keys before it gave up; use none of them.
            messages.add("the old settings file " + file.getFileName() + " could not be read (" + e.getClass().getSimpleName()
                    + "); every setting keeps its default");
            return new Result(true, false, values, messages);
        }
        if (p.isEmpty()) {
            messages.add("the old settings file " + file.getFileName() + " has no settings; every setting keeps its default");
            return new Result(true, false, values, messages);
        }
        boolean clean = true;
        for (String name : p.stringPropertyNames()) {
            String key = RENAMED.getOrDefault(name, name);
            Settings.Entry entry = Settings.entry(key);
            if (entry == null) {
                clean = false;
                messages.add("ignored the unknown old setting '" + name + "'");
                continue;
            }
            String raw = p.getProperty(name).trim();
            if (entry.isNumber()) {
                try {
                    int v = Integer.parseInt(raw);
                    int clamped = Math.max(entry.min(), Math.min(entry.max(), v));
                    if (clamped != v) {
                        clean = false;
                        messages.add(key + " was " + v + ", outside " + entry.min() + " to " + entry.max() + "; set to "
                                + clamped);
                    } else {
                        messages.add(key + " = " + clamped + " kept");
                    }
                    values.put(key, clamped);
                } catch (NumberFormatException e) {
                    clean = false;
                    messages.add(key + " was '" + raw + "', not a whole number; it keeps the default " + entry.def());
                }
            } else if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false")) {
                values.put(key, Boolean.parseBoolean(raw));
                messages.add(key + " = " + raw.toLowerCase(java.util.Locale.ROOT) + " kept");
            } else {
                clean = false;
                messages.add(key + " was '" + raw + "', not true or false; it keeps the default " + entry.def());
            }
        }
        return new Result(true, clean, values, messages);
    }

    /**
     * After the .toml files are written: checks them, then moves the old file to a backup. The steps, in order: the written
     * .toml files are read back and must hold every imported value; the old file must have parsed cleanly (
     * nothing invalid, clamped, unknown or unreadable); the backup (a header comment line, then the exact original bytes) is
     * written to a name that does not exist yet ({@code .back}, {@code .back2}, ...); the backup is read back and compared; only
     * then is the original removed. Any failure leaves the original untouched, removes only a backup this call made, logs why
     * and returns empty.
     *
     * @param imported the .toml file of every side that was imported from the old file
     * @return the backup file, or empty when nothing was moved
     */
    public static Optional<Path> retire(Path old, Result r, Map<Settings.Side, Path> imported, String version, String date,
            Consumer<String> log) {
        if (old == null || !r.found() || !Files.isRegularFile(old)) {
            return Optional.empty();
        }
        if (!r.clean()) {
            log.accept("the old file " + old + " is left where it is: it was not read completely (see the notes above)");
            return Optional.empty();
        }
        for (Map.Entry<Settings.Side, Path> e : imported.entrySet()) {
            Map<String, Object> expected = new LinkedHashMap<>();
            for (Map.Entry<String, Object> v : r.values().entrySet()) {
                Settings.Entry entry = Settings.entry(v.getKey());
                if (entry != null && entry.side() == e.getKey()) {
                    expected.put(v.getKey(), v.getValue());
                }
            }
            String problem = verifyToml(e.getValue(), expected);
            if (problem != null) {
                log.accept("the old file " + old + " is left where it is: " + problem);
                return Optional.empty();
            }
        }
        Path backup = null;
        try {
            byte[] original = Files.readAllBytes(old);
            StringBuilder names = new StringBuilder();
            for (Path t : imported.values()) {
                names.append(names.length() == 0 ? "" : " and ").append("config/").append(t.getFileName());
            }
            byte[] header = ("# Migrated by OrangePeel " + version + " on " + date + " to " + names
                    + "; this file is no longer read. Kept as a backup.\n").getBytes(StandardCharsets.UTF_8);
            byte[] whole = new byte[header.length + original.length];
            System.arraycopy(header, 0, whole, 0, header.length);
            System.arraycopy(original, 0, whole, header.length, original.length);
            for (int n = 1; backup == null; n++) {
                Path candidate = old.resolveSibling(old.getFileName() + (n == 1 ? ".back" : ".back" + n));
                try {
                    Files.write(candidate, whole, java.nio.file.StandardOpenOption.CREATE_NEW,
                            java.nio.file.StandardOpenOption.WRITE);
                    backup = candidate;
                } catch (java.nio.file.FileAlreadyExistsException exists) {
                    // never overwrite an existing backup: try the next name
                }
            }
            if (!java.util.Arrays.equals(Files.readAllBytes(backup), whole)) {
                Files.deleteIfExists(backup);
                log.accept("the old file " + old + " is left where it is: the backup did not read back the same");
                return Optional.empty();
            }
            Files.delete(old);
            log.accept("migrated " + old + " to " + names + "; the old file is now " + backup);
            return Optional.of(backup);
        } catch (IOException | RuntimeException ex) {
            if (backup != null && Files.exists(old)) {
                try {
                    Files.deleteIfExists(backup);
                } catch (IOException ignored) {
                    // the extra backup stays; the original is still in place
                }
            }
            log.accept("the old file " + old + " is left where it is: " + ex);
            return Optional.empty();
        }
    }

    /** Reads a .toml back and returns null when it holds every expected value, else what is wrong. */
    static String verifyToml(Path toml, Map<String, Object> expected) {
        String text;
        try {
            text = Files.readString(toml, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "the new settings file " + toml.getFileName() + " could not be read back (" + e.getClass().getSimpleName() + ")";
        }
        for (Map.Entry<String, Object> e : expected.entrySet()) {
            Matcher m = Pattern.compile("(?m)^\\s*" + Pattern.quote(e.getKey()) + "\\s*=\\s*(\\S+)\\s*$").matcher(text);
            if (!m.find() || !m.group(1).equals(String.valueOf(e.getValue()))) {
                return "the new settings file " + toml.getFileName() + " does not hold " + e.getKey() + " = " + e.getValue();
            }
        }
        return null;
    }

    /** The mod's version, from the jar's own mods.toml; "unknown version" when it cannot be found. */
    public static String modVersion() {
        for (String name : new String[] {"META-INF/neoforge.mods.toml", "META-INF/mods.toml"}) {
            try (java.io.InputStream in = ConfigMigration.class.getClassLoader().getResourceAsStream(name)) {
                if (in != null) {
                    Matcher m = Pattern.compile("(?m)^\\s*version\\s*=\\s*\"([^\"]+)\"")
                            .matcher(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                    if (m.find()) {
                        return m.group(1);
                    }
                }
            } catch (IOException ignored) {
                // fall through to the next name
            }
        }
        return "unknown version";
    }
}
