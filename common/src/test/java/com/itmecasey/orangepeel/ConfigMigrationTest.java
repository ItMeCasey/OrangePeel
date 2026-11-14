package com.itmecasey.orangepeel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The old settings file ({@code orangepeel-client.properties}) of every released version, copied from what that version's own
 * code wrote and then edited like a player would, must land in the new settings value for value; a missing, empty, corrupt or
 * partial file must be survivable; and reading must never change the old file.
 */
class ConfigMigrationTest {
    @TempDir
    Path dir;

    private Path fixture(String tag) throws IOException {
        Path file = dir.resolve(ConfigMigration.OLD_FILE);
        try (InputStream in = getClass().getResourceAsStream("/old-config/" + tag + ".properties")) {
            Files.copy(in, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return file;
    }

    /** What a fake loader config would hold after the import: the setter of every side, into one map. */
    private static Map<String, Object> landed(ConfigMigration.Result r) {
        Map<String, Object> spec = new HashMap<>(Settings.defaults());
        Settings.apply(r, Settings.Side.CLIENT, spec::put);
        Settings.apply(r, Settings.Side.COMMON, spec::put);
        return spec;
    }

    private static void assertEdits(Map<String, Object> spec, Object... keyValues) {
        for (int i = 0; i < keyValues.length; i += 2) {
            assertEquals(keyValues[i + 1], spec.get(keyValues[i]), String.valueOf(keyValues[i]));
        }
    }

    @Test
    void release20261101LandsValueForValue() throws IOException {
        ConfigMigration.Result r = ConfigMigration.read(fixture("v2026.11.1"));
        assertTrue(r.found());
        Map<String, Object> spec = landed(r);
        assertEdits(spec, "hudOpacity", 60, "hudOffsetX", -12, "hudOffsetY", 30, "showTooltip", false, "steadyPreview", true,
                "showWaste", false, "graphicalTooltip", false);
        // untouched keys keep the value the file had (the defaults)
        assertEdits(spec, "showHungerPreview", true, "showComparator", true, "sendExhaustion", true);
        assertEquals(14, r.values().size(), "that release wrote 14 keys, no sendExhaustion");
    }

    @Test
    void release20261102LandsValueForValue() throws IOException {
        Map<String, Object> spec = landed(ConfigMigration.read(fixture("v2026.11.2")));
        assertEdits(spec, "hudOpacity", 35, "hudOffsetX", 8, "hudOffsetY", -4, "showComparator", false, "showFullness", false,
                "showHealthPreview", false, "steadyPreview", true);
    }

    @Test
    void release20261103ImportsTheServerSwitchToo() throws IOException {
        ConfigMigration.Result r = ConfigMigration.read(fixture("v2026.11.3"));
        Map<String, Object> spec = landed(r);
        assertEdits(spec, "hudOpacity", 80, "hudOffsetX", -200, "hudOffsetY", 55, "sendExhaustion", false,
                "showExhaustionUnderlay", false, "showEffects", false, "graphicalTooltip", false);
        assertEquals(15, r.values().size());
        // sendExhaustion belongs to the COMMON side: only that side's setter gets it
        Map<String, Object> client = new HashMap<>();
        Settings.apply(r, Settings.Side.CLIENT, client::put);
        assertFalse(client.containsKey("sendExhaustion"));
        Map<String, Object> common = new HashMap<>();
        Settings.apply(r, Settings.Side.COMMON, common::put);
        assertEquals(Map.of("sendExhaustion", false), common);
    }

    @Test
    void release20261104KeepsTheEdgesOfTheRanges() throws IOException {
        Map<String, Object> spec = landed(ConfigMigration.read(fixture("v2026.11.4")));
        assertEdits(spec, "hudOpacity", 0, "hudOffsetX", 400, "hudOffsetY", -400, "sendExhaustion", false,
                "showHungerPreview", false, "showSaturationOverlay", false, "steadyPreview", true);
    }

    @Test
    void missingFileIsNotAMigration() {
        ConfigMigration.Result r = ConfigMigration.read(dir.resolve("nothing.properties"));
        assertFalse(r.found());
        assertTrue(r.values().isEmpty());
        assertTrue(r.messages().isEmpty());
    }

    @Test
    void emptyFileKeepsEveryDefault() throws IOException {
        Path file = dir.resolve(ConfigMigration.OLD_FILE);
        Files.writeString(file, "");
        ConfigMigration.Result r = ConfigMigration.read(file);
        assertTrue(r.found());
        assertTrue(r.values().isEmpty());
        assertEquals(1, r.messages().size());
        assertEquals(Settings.defaults(), landed(r));
    }

    @Test
    void corruptFileKeepsEveryDefaultAndSaysSo() throws IOException {
        Path file = dir.resolve(ConfigMigration.OLD_FILE);
        Files.writeString(file, "hudOpacity=40\nshowTooltip=false\nbroken=\\u12\n");
        ConfigMigration.Result r = ConfigMigration.read(file);
        assertTrue(r.found());
        assertTrue(r.values().isEmpty(), "a file that cannot be parsed gives nothing, not half of it");
        assertTrue(r.messages().get(0).contains("could not be read"), r.messages().toString());
        assertEquals(Settings.defaults(), landed(r));
    }

    @Test
    void binaryGarbageIsIgnoredKeyByKey() throws IOException {
        Path file = dir.resolve(ConfigMigration.OLD_FILE);
        Files.write(file, new byte[] {0, (byte) 0xFF, (byte) 0xFE, 13, 10, 0, 0, 1, 2, 3});
        ConfigMigration.Result r = ConfigMigration.read(file);
        assertTrue(r.values().isEmpty());
        assertEquals(Settings.defaults(), landed(r));
    }

    @Test
    void partialFileOnlyChangesWhatItHolds() throws IOException {
        Path file = dir.resolve(ConfigMigration.OLD_FILE);
        Files.writeString(file, "# written by hand\nhudOpacity = 70\nshowTooltip=false\n");
        Map<String, Object> spec = landed(ConfigMigration.read(file));
        assertEdits(spec, "hudOpacity", 70, "showTooltip", false, "hudOffsetX", 0, "showWaste", true, "sendExhaustion", true);
    }

    @Test
    void invalidAndOutOfRangeValuesFallBackOrClampAndAreLogged() throws IOException {
        Path file = dir.resolve(ConfigMigration.OLD_FILE);
        Files.writeString(file, "hudOpacity=150\nhudOffsetX=-9999\nhudOffsetY=abc\nshowTooltip=yes\nshowWaste=TRUE\n"
                + "steadyPreview= false \nsomeOldThing=1\n");
        ConfigMigration.Result r = ConfigMigration.read(file);
        Map<String, Object> spec = landed(r);
        assertEdits(spec, "hudOpacity", 100, "hudOffsetX", -400, "hudOffsetY", 0, "showTooltip", true, "showWaste", true,
                "steadyPreview", false);
        String log = String.join("\n", r.messages());
        assertTrue(log.contains("hudOpacity was 150, outside 0 to 100; set to 100"), log);
        assertTrue(log.contains("hudOffsetX was -9999, outside -400 to 400; set to -400"), log);
        assertTrue(log.contains("hudOffsetY was 'abc'"), log);
        assertTrue(log.contains("showTooltip was 'yes'"), log);
        assertTrue(log.contains("ignored the unknown old setting 'someOldThing'"), log);
    }

    @Test
    void readingNeverChangesTheOldFile() throws IOException {
        Path file = fixture("v2026.11.3");
        byte[] before = Files.readAllBytes(file);
        long modified = Files.getLastModifiedTime(file).toMillis();
        ConfigMigration.read(file);
        ConfigMigration.read(file);
        assertArrayEquals(before, Files.readAllBytes(file));
        assertEquals(modified, Files.getLastModifiedTime(file).toMillis());
        assertTrue(Files.exists(file));
        assertEquals(StandardCharsets.UTF_8, StandardCharsets.UTF_8);
    }

    @Test
    void everyReleasedFileKeyIsASettingOrIgnoredNeverLost() throws IOException {
        for (String tag : new String[] {"v2026.11.1", "v2026.11.2", "v2026.11.3", "v2026.11.4"}) {
            ConfigMigration.Result r = ConfigMigration.read(fixture(tag));
            assertFalse(String.join("\n", r.messages()).contains("unknown"), tag + ": " + r.messages());
            for (String key : r.values().keySet()) {
                assertTrue(Settings.entry(key) != null, key);
            }
        }
    }
    /** What the loader leaves behind: both .toml files, each holding the landed values as the loader writes them. */
    private Map<Settings.Side, Path> writeTomls(Map<String, Object> spec, Object... breakKeyValues) throws IOException {
        Map<Settings.Side, Path> tomls = new EnumMap<>(Settings.Side.class);
        for (Settings.Side side : Settings.Side.values()) {
            StringBuilder sb = new StringBuilder("# settings\n");
            for (Settings.Entry e : Settings.ENTRIES) {
                if (e.side() == side) {
                    Object v = spec.get(e.key());
                    for (int i = 0; i < breakKeyValues.length; i += 2) {
                        if (e.key().equals(breakKeyValues[i])) {
                            v = breakKeyValues[i + 1];
                        }
                    }
                    sb.append("\t# ").append(e.comment()).append("\n\t").append(e.key()).append(" = ").append(v).append("\n");
                }
            }
            Path toml = dir.resolve("orangepeel-" + side.name().toLowerCase() + ".toml");
            Files.writeString(toml, sb.toString());
            tomls.put(side, toml);
        }
        return tomls;
    }

    private static final String HEADER_START = "# Migrated by OrangePeel 2026.11.5 on 2026-11-14 to config/orangepeel-client.toml";

    @Test
    void successfulMigrationLeavesTomlAndBackupWithHeaderAndNoOriginal() throws IOException {
        Path old = fixture("v2026.11.3");
        byte[] original = Files.readAllBytes(old);
        ConfigMigration.Result r = ConfigMigration.read(old);
        assertTrue(r.clean(), r.messages().toString());
        Map<Settings.Side, Path> tomls = writeTomls(landed(r));
        List<String> log = new ArrayList<>();
        Path back = ConfigMigration.retire(old, r, tomls, "2026.11.5", "2026-11-14", log::add).orElseThrow();
        assertEquals(dir.resolve(ConfigMigration.OLD_FILE + ".back"), back);
        assertFalse(Files.exists(old), "the original is gone");
        assertTrue(Files.exists(tomls.get(Settings.Side.CLIENT)) && Files.exists(tomls.get(Settings.Side.COMMON)));
        byte[] whole = Files.readAllBytes(back);
        String text = new String(whole, StandardCharsets.UTF_8);
        assertTrue(text.startsWith(HEADER_START), text);
        int nl = text.indexOf('\n');
        assertTrue(text.substring(0, nl).contains("this file is no longer read. Kept as a backup."));
        assertArrayEquals(original, java.util.Arrays.copyOfRange(whole, nl + 1, whole.length));
        assertEquals(1, log.size());
        assertTrue(log.get(0).contains(old.toString()) && log.get(0).contains(back.toString())
                && log.get(0).contains("orangepeel-client.toml"), log.get(0));
    }

    @Test
    void secondLaunchFindsNothingToDo() throws IOException {
        Path old = fixture("v2026.11.4");
        ConfigMigration.Result r = ConfigMigration.read(old);
        Map<Settings.Side, Path> tomls = writeTomls(landed(r));
        ConfigMigration.retire(old, r, tomls, "2026.11.5", "2026-11-14", m -> { });
        byte[] back = Files.readAllBytes(dir.resolve(ConfigMigration.OLD_FILE + ".back"));
        ConfigMigration.Result again = ConfigMigration.read(old);
        assertFalse(again.found());
        assertTrue(ConfigMigration.retire(old, again, tomls, "2026.11.5", "2026-11-14", m -> { }).isEmpty());
        assertArrayEquals(back, Files.readAllBytes(dir.resolve(ConfigMigration.OLD_FILE + ".back")));
        assertFalse(Files.exists(dir.resolve(ConfigMigration.OLD_FILE + ".back2")));
    }

    @Test
    void anExistingBackupIsNeverOverwritten() throws IOException {
        Path old = fixture("v2026.11.2");
        Path taken = dir.resolve(ConfigMigration.OLD_FILE + ".back");
        Files.writeString(taken, "an older backup");
        Path taken2 = dir.resolve(ConfigMigration.OLD_FILE + ".back2");
        Files.writeString(taken2, "another one");
        ConfigMigration.Result r = ConfigMigration.read(old);
        Path back = ConfigMigration.retire(old, r, writeTomls(landed(r)), "2026.11.5", "2026-11-14", m -> { }).orElseThrow();
        assertEquals(dir.resolve(ConfigMigration.OLD_FILE + ".back3"), back);
        assertEquals("an older backup", Files.readString(taken));
        assertEquals("another one", Files.readString(taken2));
        assertFalse(Files.exists(old));
    }

    @Test
    void aTomlThatDoesNotHoldTheImportedValuesKeepsTheOriginal() throws IOException {
        Path old = fixture("v2026.11.3");
        byte[] original = Files.readAllBytes(old);
        ConfigMigration.Result r = ConfigMigration.read(old);
        Map<Settings.Side, Path> tomls = writeTomls(landed(r), "hudOpacity", 99);
        List<String> log = new ArrayList<>();
        assertTrue(ConfigMigration.retire(old, r, tomls, "2026.11.5", "2026-11-14", log::add).isEmpty());
        assertArrayEquals(original, Files.readAllBytes(old));
        assertFalse(Files.exists(dir.resolve(ConfigMigration.OLD_FILE + ".back")));
        assertTrue(log.get(0).contains("does not hold hudOpacity"), log.get(0));
    }

    @Test
    void aMissingTomlKeepsTheOriginal() throws IOException {
        Path old = fixture("v2026.11.3");
        byte[] original = Files.readAllBytes(old);
        ConfigMigration.Result r = ConfigMigration.read(old);
        Map<Settings.Side, Path> tomls = writeTomls(landed(r));
        Files.delete(tomls.get(Settings.Side.CLIENT));
        assertTrue(ConfigMigration.retire(old, r, tomls, "2026.11.5", "2026-11-14", m -> { }).isEmpty());
        assertArrayEquals(original, Files.readAllBytes(old));
        assertFalse(Files.exists(dir.resolve(ConfigMigration.OLD_FILE + ".back")));
    }

    @Test
    void corruptOrPartlyReadableOldFilesStayWhereTheyAreWithNoBackup() throws IOException {
        String[] bodies = {"", "hudOpacity=40\nbroken=\\u12\n", "hudOpacity=150\nshowTooltip=false\n",
            "hudOffsetY=abc\nshowWaste=false\n", "showWaste=false\nsomeOldThing=1\n"};
        for (String body : bodies) {
            Path old = dir.resolve(ConfigMigration.OLD_FILE);
            Files.writeString(old, body);
            byte[] original = Files.readAllBytes(old);
            ConfigMigration.Result r = ConfigMigration.read(old);
            Map<Settings.Side, Path> tomls = writeTomls(landed(r));
            List<String> log = new ArrayList<>();
            assertTrue(ConfigMigration.retire(old, r, tomls, "2026.11.5", "2026-11-14", log::add).isEmpty(), body);
            assertArrayEquals(original, Files.readAllBytes(old), body);
            assertFalse(Files.exists(dir.resolve(ConfigMigration.OLD_FILE + ".back")), body);
            assertEquals(1, log.size(), body);
        }
    }

    @Test
    void modVersionIsNeverBlank() {
        assertFalse(ConfigMigration.modVersion().isBlank());
    }
}
