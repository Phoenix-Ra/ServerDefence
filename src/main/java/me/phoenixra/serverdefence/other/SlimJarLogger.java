package me.phoenixra.serverdefence.other;

import io.github.slimjar.logging.ProcessLogger;
import me.phoenixra.serverdefence.Main;

import java.text.MessageFormat;

public class SlimJarLogger implements ProcessLogger {
    private final Main plugin;

    public SlimJarLogger(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void log(String message, Object... args) {

        plugin.getLogger().info(MessageFormat.format(message, args));
    }

    @Override
    public void debug(String message, Object... args) {
        ProcessLogger.super.debug(message, args);
    }
}
