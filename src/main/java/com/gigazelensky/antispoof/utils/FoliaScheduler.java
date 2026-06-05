package com.gigazelensky.antispoof.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Utility class to support both Paper and Folia schedulers.
 * Folia uses region-based threading — tasks must be dispatched
 * to the correct scheduler depending on context.
 */
public class FoliaScheduler {

    private static final boolean IS_FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Run a task on the main/global thread (next tick).
     * Use for non-entity, non-location operations (e.g. plugin-wide state).
     */
    public static void runTask(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a delayed task on the main/global thread.
     */
    public static void runTaskLater(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    /**
     * Run a task asynchronously (off the main thread).
     * Safe for network/IO operations like Discord webhooks.
     */
    public static void runTaskAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Run a delayed task asynchronously.
     */
    public static void runTaskLaterAsync(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            // Folia async scheduler uses milliseconds, convert ticks → ms (1 tick = 50ms)
            long delayMs = delayTicks * 50L;
            Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(),
                    delayMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        }
    }

    /**
     * Run a task on the entity's scheduler (follows entity across regions).
     * Falls back to global scheduler on Paper.
     */
    public static void runTaskForEntity(Plugin plugin, Player player, Runnable task, Runnable retired) {
        if (IS_FOLIA) {
            player.getScheduler().run(plugin, scheduledTask -> task.run(), retired);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a delayed task on the entity's scheduler.
     */
    public static void runTaskLaterForEntity(Plugin plugin, Player player, Runnable task, Runnable retired, long delayTicks) {
        if (IS_FOLIA) {
            player.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), retired, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }
}
