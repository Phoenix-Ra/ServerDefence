package FenixRa.ServerDefence;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PermsCheckerTask extends BukkitRunnable {
    private final Main plugin;

    protected PermsCheckerTask(Main plugin) {
        this.plugin = plugin;
    }

    public void run() {
        if(plugin.fileM.getConfig("config").getBoolean("PermsChecker.active")&&!plugin.fileM.getConfig("config").contains("PermsChecker.permissions")) {
            plugin.getConsole().sendMessage(LangKeys.PREFIX+
                    "§cPermsChecker.permissions list in config.yml is not found! PermsChecker won't work");
        }
        for (final Player player : Bukkit.getOnlinePlayers()) {
            boolean isAdmin=plugin.fileM.getConfig("data").contains("admins."+player.getName());
            if (!player.isOnline()) {
                continue;
            }
            if(!isAdmin) {
                if (player.isOp()) {
                    Main.doSync(() -> player.kickPlayer(LangKeys.PREFIX.toString() + LangKeys.KICK_OP));
                }
                if(!plugin.fileM.getConfig("config").getBoolean("PermsChecker.active")||!plugin.fileM.getConfig("config").contains("PermsChecker.permissions")){
                    return;
                }
                for (String s : plugin.fileM.getConfig("config").getStringList("PermsChecker.permissions")) {
                    if (player.hasPermission(s)) {
                        Main.doSync(() -> player.kickPlayer(LangKeys.PREFIX.toString() + LangKeys.KICK_PERMISSION));
                    }
                }
            }
        }
    }

}
