package me.phoenixra.serverdefence;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.UnknownDependencyException;

import java.util.ArrayList;
import java.util.List;

public class TabComplete {
    protected TabCompleteOld tabCompleteOld;
    protected TabComplete() {
        if (Main.getInstance().getServerVersion() > 12) {
            Bukkit.getServer().getPluginManager().registerEvents(new newVersion(), Main.getInstance());
        } else {
            try {
                if (Main.getInstance().getServerVersion() < 10) {
                    Main.getInstance().getConsole().sendMessage(Utils.colorFormat("&cMinecraft version is older than 1.10... tabBlocker won't work correctly ( can only block arguments )"));
                    tabCompleteOld=new TabCompleteOld();
                    return;
                }
                tabCompleteOld=new TabCompleteOld();
                Bukkit.getServer().getPluginManager().registerEvents(tabCompleteOld, Main.getInstance());
            } catch (UnknownDependencyException ex) {
                ex.printStackTrace();
                Bukkit.getConsoleSender().sendMessage("§cProbably ProtocolLib plugin is missing?  If you have a version below 1.13, u need ProtocolLib to use tab blocker feature");
            }
        }
    }


    protected class newVersion implements Listener {
        @EventHandler
        public void onCommandSend(PlayerCommandSendEvent event) {
            FileConfiguration config = Main.getInstance().getFileM().getConfig("config");
            if (!config.getBoolean("TabBlocker")) {
                return;
            }
            event.getCommands().removeIf(cmd -> !Main.getInstance().getFileM().isAllowedCommandAction(event.getPlayer(), "/" + cmd, true));
            List<String> l = new ArrayList<>();
            if (config.contains("FixSyntax.whitelist-cmds")) {
                l.addAll(config.getStringList("FixSyntax.whitelist-cmds"));
            }
            event.getCommands().removeIf(cmd -> cmd.contains(":") && !l.contains("/" + cmd.split(":")[0]));
        }
    }


}
