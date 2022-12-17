package me.phoenixra.serverdefence;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;

public class ByteMessageListener implements PluginMessageListener {

    public void onPluginMessageReceived(String channel, Player player, byte[] value) {
        FileConfiguration config = Main.getInstance().getFileM().getConfig("config");
        if (config.getBoolean("modsBlocker.5zig.enabled")) {
            this.block5Zig(player, channel);
        }
        if (config.getBoolean("modsBlocker.betterSprinting.enabled")) {
            this.blockBSM(player, channel);
        }
        if (channel.equalsIgnoreCase("minecraft:brand") || channel.equalsIgnoreCase("MC|BRAND")) {
            String brand = new String(value, StandardCharsets.UTF_8);
            if (config.getBoolean("modsBlocker.fabric.enabled")) {
                this.blockFabric(player, brand);
            }
            if (config.getBoolean("modsBlocker.lunarClient.enabled")) {
                this.blockLunarClient(player, brand);
            }
            if (config.getBoolean("modsBlocker.emc.enabled")) {
                this.blockEMC(player, brand);
            }
            if (config.getBoolean("modsBlocker.liteLoader.enabled")) {
                this.blockLiteLoader(player, brand);
            }
            if (config.getBoolean("modsBlocker.forge.enabled")) {
                this.blockForge(player, brand);
            }
            if (config.getBoolean("modsBlocker.rift.enabled")) {
                this.blockRift(player, brand);
            }
        } else if (channel.equalsIgnoreCase("minecraft:register") || channel.equalsIgnoreCase("register") || channel.equalsIgnoreCase("REGISTER")) {
            String register = new String(value, StandardCharsets.UTF_8);
            if (config.getBoolean("modsBlocker.schematica.enabled")) {
                this.blockSchematica(player, register);
            }
        }
        if (config.getBoolean("modsBlocker.winterWare.enabled")) {
            this.blockWinterware(player, channel);
        }
        if (config.getBoolean("modsBlocker.vape.enabled")) {
            this.blockVape(player, channel);
        }
        if (config.getBoolean("modsBlocker.pixelClient.enabled")) {
            this.blockPixelClient(player, channel);
        }
        if (config.getBoolean("modsBlocker.worldDownloader.enabled")) {
            this.blockWDL(player, channel);
        }
        if (config.getBoolean("modsBlocker.hyperium.enabled")) {
            this.blockHyperium(player, channel);
        }
    }

    private void blockSchematica(Player player, String value) {
        if (value.contains("schematica") &&!player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.schematica")) {
            player.sendPluginMessage(Main.getInstance(), ChannelControl.translateChannel("schematica"), Utils.getSchematicaPayload());
        }
    }

    private void block5Zig(Player player, String value) {
        if (value.contains("5zig") &&!player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.5zig")) {
            player.sendPluginMessage(Main.getInstance(), value, new byte[]{63});
        }
    }

    private void blockBSM(Player player, String value) {
        if (value.equalsIgnoreCase("BSprint") &&!player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.betterSprinting")) {
            player.sendPluginMessage(Main.getInstance(), value, new byte[]{1});
        }
    }

    private void blockFabric(Player player, String value) {
        if (value.contains("fabric") &&!player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.fabric")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.fabric.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cFabric usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }

    private void blockForge(Player player, String value) {
        if ((value.contains("fml") || value.contains("forge")) &&!player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.forge")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.forge.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cForge usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }

    private void blockLiteLoader(Player player, String value) {
        if ((value.equalsIgnoreCase("LiteLoader") || value.contains("Lite"))
                &&!player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.liteLoader")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.liteLoader.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cliteLoader usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }

    private void blockRift(Player player, String value) {
        if (value.contains("rift") &&!player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.rift")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.rift.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cRift usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }

    private void blockWDL(Player player, String value) {
        if ((value.equalsIgnoreCase("wdl:init") || value.equalsIgnoreCase("WDL|INIT"))
                &&!player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.worldDownloader")) {
            byte[][] packets = new byte[][]{Utils.createWDLPacket0(), Utils.createWDLPacket1()};
            for (byte[] packet : packets) {
                player.sendPluginMessage(Main.getInstance(), value, packet);
            }
        }
    }

    private void blockWinterware(Player player, String value) {
        if ((value.equalsIgnoreCase("LC|BRAND") || value.contains("lcbrand") || value.contains("lc|brand")) &&
                !player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.winterWare")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.winterWare.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cWinterware usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }

    private void blockVape(Player player, String value) {
        if ((value.equalsIgnoreCase("LOLIMAHCKER") || value.contains("lolimahcker")) &&
                !player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.vape")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.vape.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cVape usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }

    private void blockHyperium(Player player, String value) {
        if (value.contains("hyperium") &&
                !player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.hyperium")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.hyperium.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cHyperium usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }

    private void blockPixelClient(Player player, String value) {
        if ((value.equalsIgnoreCase("MC|Pixel") || value.contains("mc|pixel") || value.contains("mcpixel")) &&
                !player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.pixelClient")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.pixelClient.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cPixelClient usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }

    private void blockLunarClient(Player player, String value) {
        if ((value.equalsIgnoreCase("Lunar-Client") || value.contains("lunar-client") || value.contains("lunarclient")) &&
                !player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.lunarClient")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.lunarClient.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cLunarClient usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }

    private void blockEMC(Player player, String value) {
        if ((value.contains("Subsystem") || value.contains("subsystem")) &&
                !player.hasPermission("serverDef.admin")&&
                !player.hasPermission("serverDef.bypass.modBlocker.emc")) {

            for(String s : Main.getInstance().getFileM().getConfig("config").getStringList("modsBlocker.emc.punishment")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Utils.colorFormat(s.replace("{player}",player.getName())));
            }
            String notifyMessage = LangKeys.PREFIX+"&cEMC usage detected! Player: "+player.getName();
            Utils.broadcastAdmins(notifyMessage);
        }
    }
}
