package me.phoenixra.serverdefence;

import org.bukkit.configuration.file.FileConfiguration;

public class ChannelControl {

    public static void registerChannels(ByteMessageListener bml) {
        FileConfiguration config = Main.getInstance().fileM.getConfig("config");
        ChannelControl.register(bml, "MC|Brand");
        ChannelControl.register(bml, "minecraft:brand");
        if (config.getBoolean("modsBlocker.5zig.block")) {
            ChannelControl.register(bml, "5zig_Set");
            ChannelControl.register(bml, "the5zigmod:5zig_set");
        }
        if (config.getBoolean("modsBlocker.betterSprinting.block")) {
            ChannelControl.register(bml, "BSM");
            ChannelControl.register(bml, "BSprint");
            ChannelControl.register(bml, "bsm:settings");
        }
        if (config.getBoolean("modsBlocker.schematica.block")) {
            ChannelControl.register(bml, "schematica");
        }
        if (config.getBoolean("modsBlocker.worldDownloader.block")) {
            ChannelControl.register(bml, "WDL|INIT");
            ChannelControl.register(bml, "WDL|CONTROL");
            ChannelControl.register(bml, "WDL|REQUEST");
            ChannelControl.register(bml, "WorldDownloader");
            ChannelControl.register(bml, "wdl");
        }
        if (config.getBoolean("modsBlocker.vape.block")) {
            ChannelControl.register(bml, "LOLIMAHCKER");
        }
        if (config.getBoolean("modsBlocker.forge.block")) {
            ChannelControl.register(bml, "FML|HS");
            ChannelControl.register(bml, "FML|MP");
            ChannelControl.register(bml, "FML");
        }
        if (config.getBoolean("modsBlocker.hyperium.block")) {
            ChannelControl.register(bml, "hyperium");
        }
        if (config.getBoolean("modsBlocker.pixelClient.block")) {
            ChannelControl.register(bml, "MC|Pixel");
        }
        if (config.getBoolean("modsBlocker.winterWare.block")) {
            ChannelControl.register(bml, "LC|Brand");
        }
        if (config.getBoolean("modsBlocker.lunarClient.block")) {
            ChannelControl.register(bml, "Lunar-Client");
            ChannelControl.register(bml, "LunarClient");
        }
        if (config.getBoolean("modsBlocker.EMC.block")) {
            ChannelControl.register(bml, "Subsystem");
        }
        Main.getInstance().getConsole().sendMessage("§7Channels listener loaded successfully.");
    }

    protected static String translateChannel(String Channel) {
        if (Main.getInstance().getServerVersion()>12) {
            return Channel.replace("|", ":").toLowerCase();
        }
        return Channel;
    }

    private static void register(ByteMessageListener bml, String Channel) {
        Main.getInstance().getServer().getMessenger().registerIncomingPluginChannel(Main.getInstance(), ChannelControl.translateChannel(Channel),bml);
        Main.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(Main.getInstance(), ChannelControl.translateChannel(Channel));
    }
}
