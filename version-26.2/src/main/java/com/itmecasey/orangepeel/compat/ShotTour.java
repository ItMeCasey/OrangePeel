package com.itmecasey.orangepeel.compat;

import com.itmecasey.orangepeel.OrangePeel;
import com.itmecasey.orangepeel.client.ClientHooks;
import java.io.IOException;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;

/**
 * A development-only camera tour: it opens a fresh creative superflat world, builds a few things
 * to look at (a chest, a furnace, a crop, water, a cow), points the camera at each and saves a
 * screenshot into {@code run/screenshots} as {@code orangepeel_<name>.png}, then quits. It does nothing
 * unless the environment variable {@code ORANGEPEEL_SHOTS} is set.
 *
 * <p>It runs on a desktop someone may be using, so it stays out of the way: it lets go of the mouse
 * every tick, keeps rendering while unfocused, and the script that starts it mutes the game.
 * Progress goes to the log as {@code ORANGEPEEL-SHOTS} lines.
 */
public final class ShotTour {
    private static final boolean ENABLED = System.getenv("ORANGEPEEL_SHOTS") != null;
    private static final String WORLD = "orangepeel_shots";
    /** Ticks for the overlay to ask the server and get its answer, and for the chunk renderer to catch up. */
    private static final int SETTLE = 40;

    private enum Stage { WAITING_FOR_MENU, LOADING_WORLD, TOURING, DONE }

    /** One thing to do, after waiting the given number of ticks. */
    private record Step(int delay, Runnable action) {
    }

    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static Stage stage = Stage.WAITING_FOR_MENU;
    private static int idle;
    private static int wait;
    /** The first air block above the superflat ground. */
    private static int ground;
    private static boolean pauseOnLostFocus;

    private ShotTour() {
    }

    public static void tick() {
        if (!ENABLED || stage == Stage.DONE) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.mouseHandler.isMouseGrabbed()) {
            minecraft.mouseHandler.releaseMouse();
        }
        switch (stage) {
            case WAITING_FOR_MENU -> waitForMenu(minecraft);
            case LOADING_WORLD -> waitForWorld(minecraft);
            case TOURING -> runSteps();
            default -> {
            }
        }
    }

    /** Once loading has finished and a menu is up, creates the world (replacing an old one). */
    private static void waitForMenu(Minecraft minecraft) {
        if (minecraft.gui.overlay() != null || minecraft.level != null || minecraft.gui.screen() == null || ++idle < 20) {
            return;
        }
        pauseOnLostFocus = minecraft.options.pauseOnLostFocus;
        minecraft.options.pauseOnLostFocus = false;

        LevelStorageSource source = minecraft.getLevelSource();
        if (source.levelExists(WORLD)) {
            try (LevelStorageSource.LevelStorageAccess access = source.createAccess(WORLD)) {
                access.deleteLevel();
            } catch (IOException e) {
                OrangePeel.LOGGER.warn("ORANGEPEEL-SHOTS could not delete the old world", e);
            }
        }
        LevelSettings settings = new LevelSettings(WORLD, GameType.SURVIVAL,
                new LevelSettings.DifficultySettings(Difficulty.NORMAL, false, false), true, WorldDataConfiguration.DEFAULT);
        stage = Stage.LOADING_WORLD;
        idle = 0;
        OrangePeel.LOGGER.info("ORANGEPEEL-SHOTS creating world");
        minecraft.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
                registries -> registries.lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
    }

    /** Waits for the player to stand in the world, then plans the tour. */
    private static void waitForWorld(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || minecraft.gui.screen() != null
                || minecraft.getSingleplayerServer() == null || ++idle < 40) {
            return;
        }
        ground = minecraft.level.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0);
        OrangePeel.LOGGER.info("ORANGEPEEL-SHOTS in the world, ground at y={}", ground);
        planTour();
        stage = Stage.TOURING;
    }

    private static void runSteps() {
        if (wait > 0) {
            wait--;
            return;
        }
        Step step = STEPS.poll();
        if (step == null) {
            return;
        }
        step.action().run();
        Step next = STEPS.peek();
        wait = next == null ? 0 : next.delay();
    }

    // --- The tour ----------------------------------------------------------------------------

    private static void planTour() {
        then(0, () -> {
            server("gamerule doDaylightCycle false", "gamerule doWeatherCycle false", "gamerule doMobSpawning false",
                    "time set noon", "weather clear", "kill @e[type=!player]", "clear @p",
                    "give @p minecraft:cooked_beef 8", "give @p minecraft:bread 12", "give @p minecraft:apple 5",
                    "give @p minecraft:golden_apple 2", "give @p minecraft:rotten_flesh 9",
                    "give @p minecraft:cooked_chicken 4", "give @p minecraft:sweet_berries 20");
            LocalPlayer player = player();
            player.getAbilities().flying = false;
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        });
        then(10, () -> setHunger(8, 1.0F, 12.0F));
        then(2, () -> selectSlot(0));
        shoot("hud_steak_hungry", SETTLE);
        then(2, () -> ClientHooks.tourTooltip = player().getInventory().getItem(0));
        shoot("tooltip_steak_hungry", 10);
        then(2, () -> ClientHooks.tourTooltip = player().getInventory().getItem(1));
        shoot("tooltip_bread_hungry", 10);
        then(2, () -> ClientHooks.tourTooltip = ItemStack.EMPTY);

        then(2, () -> selectSlot(4));
        shoot("hud_rotten_flesh", SETTLE);
        then(2, () -> ClientHooks.tourTooltip = player().getInventory().getItem(4));
        shoot("tooltip_rotten_flesh", 10);
        then(2, () -> ClientHooks.tourTooltip = ItemStack.EMPTY);

        then(2, () -> setHunger(19, 5.0F, 20.0F));
        then(2, () -> selectSlot(3));
        shoot("hud_golden_apple_nearly_full", SETTLE);
        then(2, () -> ClientHooks.tourTooltip = player().getInventory().getItem(1));
        shoot("tooltip_bread_nearly_full", 10);
        then(2, () -> ClientHooks.tourTooltip = ItemStack.EMPTY);

        then(2, () -> setHunger(10, 2.0F, 20.0F));
        then(2, () -> selectSlot(0));
        then(2, () -> Minecraft.getInstance().gui.setScreen(new InventoryScreen(player())));
        shoot("inventory_foods", 20);
        then(2, () -> Minecraft.getInstance().gui.setScreen(null));

        then(20, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.options.pauseOnLostFocus = pauseOnLostFocus;
            OrangePeel.LOGGER.info("ORANGEPEEL-SHOTS DONE");
            stage = Stage.DONE;
            minecraft.stop();
        });
    }

    private static void setHunger(int food, float saturation, float health) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> {
                for (var p : server.getPlayerList().getPlayers()) {
                    p.getFoodData().setFoodLevel(food);
                    p.getFoodData().setSaturation(saturation);
                    p.setHealth(health);
                }
            });
        }
    }

    /** Puts the camera two blocks south of the thing at this x and a little above, looking at it. */
    private static void lookAt(int x) {
        then(4, () -> {
            Minecraft.getInstance().gui.setScreen(null);
            moveTo(new Vec3(x + 0.5, ground + 1.3, -1.6), new Vec3(x + 0.5, ground + 0.4, 0.5));
        });
    }

    /** Straight down onto the top of the block at this x, so the crosshair lands on that block. */
    private static void lookDownAt(int x) {
        then(4, () -> moveTo(new Vec3(x + 0.5, ground + 4.0, 0.5), new Vec3(x + 0.5, ground + 0.5, 0.5)));
    }

    /** Side-on at a height above the ground line, so the crosshair lands on a body at that height. */
    private static void lookBodyAt(int x, double height) {
        then(4, () -> moveTo(new Vec3(x + 0.5, ground + 1.5, -2.0), new Vec3(x + 0.5, ground + height, 0.5)));
    }

    /** Like {@link #lookAt} but from higher up, for something below the ground line. */
    private static void lookAtDown(int x) {
        then(4, () -> moveTo(new Vec3(x + 0.5, ground + 1.8, -1.2), new Vec3(x + 0.5, ground - 0.5, 0.5)));
    }

    private static void moveTo(Vec3 eye, Vec3 target) {
        Vec3 d = target.subtract(eye);
        float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float) -(Mth.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * Mth.RAD_TO_DEG);
        server(String.format(Locale.ROOT, "tp @a %.3f %.3f %.3f %.2f %.2f", eye.x, eye.y - 1.62, eye.z, yaw, pitch));
        LocalPlayer player = player();
        player.snapTo(eye.x, eye.y - 1.62, eye.z, yaw, pitch);
        player.setYHeadRot(yaw);
        player.setDeltaMovement(Vec3.ZERO);
        // Fly, so the camera does not sink onto the ground after the teleport and miss its target.
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
    }

    /** A command with the position of a block on the ground line at this x filled in; {@code up} raises it. */
    private static String at(String command, int x, int up) {
        return String.format(Locale.ROOT, command, x, ground + up, 0);
    }

    /** What the crosshair is on, as a block position or an entity id and its position. */
    private static String describeHit(net.minecraft.world.phys.HitResult hit) {
        if (hit instanceof net.minecraft.world.phys.BlockHitResult block) {
            return "block " + block.getBlockPos();
        }
        if (hit instanceof net.minecraft.world.phys.EntityHitResult entity) {
            return "entity " + entity.getEntity().getType() + " at " + entity.getEntity().position();
        }
        return String.valueOf(hit);
    }

    private static void shoot(String name, int delay) {
        then(delay, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            // Command echoes and recipe toasts would only clutter the shot.
            minecraft.gui.hud.getChat().clearMessages(false);
            minecraft.gui.toastManager().clear();
        });
        then(2, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer self = minecraft.player;
            OrangePeel.LOGGER.info("ORANGEPEEL-SHOTS {} eye={} feet={},{},{} pitch={} yaw={} hit={}", name, self.getEyePosition(),
                    self.getX(), self.getY(), self.getZ(), self.getXRot(), self.getYRot(), describeHit(minecraft.hitResult));
            Screenshot.grab(minecraft.gameDirectory, "orangepeel_" + name + ".png", minecraft.gameRenderer.mainRenderTarget(), 1,
                    message -> OrangePeel.LOGGER.info("ORANGEPEEL-SHOTS saved {}", name));
        });
    }

    private static void then(int delay, Runnable action) {
        STEPS.add(new Step(delay, action));
    }

    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    private static void server(String... commands) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> {
                for (String command : commands) {
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
                }
            });
        }
    }

    private static void selectSlot(int slot) {
        player().getInventory().setSelectedSlot(slot);
    }
}
