package FenixRa.ServerDefence;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DefenceTask extends BukkitRunnable {
    private final Main plugin;
    private final long period;

    private Map<Player,Long> ds_verify_timer=Collections.synchronizedMap(new HashMap<>());
    private final List<String> ds_onRemove=Collections.synchronizedList(new ArrayList<>());

    protected DefenceTask(Main plugin, long period) {
        this.period=period;
        this.plugin = plugin;
        this.runTaskTimerAsynchronously(plugin, 0, period);
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
                if (player.isOp() && plugin.fileM.getConfig("config").getBoolean("KickUnauthorizedOp")) {
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

            if(plugin.fileM.getConfig("config").getBoolean("DiscordBot.active")) {
                HashMap<Player, Long> map = new HashMap<>();
                for (Map.Entry<Player, Long> entry : ds_verify_timer.entrySet()) {
                    if (ds_onRemove.contains(entry.getKey().getName())) {
                        ds_onRemove.remove(entry.getKey().getName());
                        continue;
                    }

                    if (entry.getValue() > 0) {
                        map.put(entry.getKey(), entry.getValue() - period);
                    } else {
                        plugin.discordBot.VerifyFailed(entry.getKey(), "Timed out!");
                    }
                }

                ds_verify_timer = map;
            }
        }
    }

    public void addVerify(Player p, long time){
        ds_verify_timer.put(p,time);
    }
    public void removeVerify(Player p){
        ds_onRemove.add(p.getName());
    }


}
