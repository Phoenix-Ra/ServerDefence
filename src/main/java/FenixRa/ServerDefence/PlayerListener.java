package FenixRa.ServerDefence;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerListener implements Listener {
    private final Main plugin;

    protected PlayerListener(Main plugin) {
        this.plugin = plugin;


    }

    @EventHandler
    public void onPreCommand(final PlayerCommandPreprocessEvent e) {
        if(Utils.isInvalidPlayer(e.getPlayer())) return;

        // ':' symbol check
        if (plugin.fileM.getConfig("config").getBoolean("FixSyntax.active")) {
            Pattern p = Pattern.compile("^/([a-zA-Z0-9_]+):");
            Matcher m = p.matcher(e.getMessage());
            if (m.find()) {
                String pluginRef = m.group(1);
                if (!plugin.fileM.getConfig("config").contains("FixSyntax.whitelist-plugins")||!plugin.fileM.getConfig("config").getStringList("FixSyntax.whitelist-plugins").contains(pluginRef)) {
                    if (pluginRef.equalsIgnoreCase("bukkit") || pluginRef.equalsIgnoreCase("minecraft")) {
                        e.getPlayer().sendMessage(LangKeys.PREFIX+LangKeys.CONSOLE_ONLY_CMD.toString());
                        e.setCancelled(true);
                        return;
                    }
                    for (Plugin plugin : plugin.getServer().getPluginManager().getPlugins()) {
                        if (!plugin.getName().equalsIgnoreCase(pluginRef)) continue;
                        e.getPlayer().sendMessage(LangKeys.PREFIX+LangKeys.CONSOLE_ONLY_CMD.toString());
                        e.setCancelled(true);
                        return;
                    }
                }

            }
        }
        boolean isAdmin=plugin.fileM.getConfig("data").contains("admins."+e.getPlayer().getName());

        //permissions check
        if(!isAdmin) {
            if (e.getPlayer().isOp()) {
                e.setCancelled(true);
                e.getPlayer().kickPlayer(LangKeys.PREFIX.toString() + LangKeys.KICK_OP);
                return;
            }

            if(!plugin.fileM.getConfig("config").getBoolean("PermsChecker.active")) return;
            if(!plugin.fileM.getConfig("config").contains("PermsChecker.permissions")) {
                plugin.getConsole().sendMessage(LangKeys.PREFIX+
                        "§cPermsChecker.permissions list in config.yml is not found! PermsChecker won't work");
                return;
            }
            for (String s : plugin.fileM.getConfig("config").getStringList("PermsChecker.permissions")) {
                if (e.getPlayer().hasPermission(s)) {
                    e.setCancelled(true);
                    e.getPlayer().kickPlayer(LangKeys.PREFIX.toString() + LangKeys.KICK_PERMISSION);
                    return;
                }
            }
        }

        //commands check
        if(!Main.getInstance().fileM.getConfig("config").contains("CommandBlocker")||Main.getInstance().fileM.getConfig("config").getBoolean("CommandBlocker")) {
            String[] split;
            String word = null;
            for (int length = (split = e.getMessage().split(" ")).length, i = 0; i < length; ++i) {
                if (word != null) {
                    word = word + " " + split[i];
                } else {
                    word = split[i];
                }
                //is blockedCmd check

                if(!Main.getInstance().fileM.isAllowedCommandAction(e.getPlayer(), word, false)){
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(LangKeys.PREFIX.toString() + LangKeys.CONSOLE_ONLY_CMD);
                    return;
                }
                /*
                if (plugin.fileM.cmdC.getStringList("console").contains(word)) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(LangKeys.PREFIX.toString() + LangKeys.CONSOLE_ONLY_CMD);
                    return;

                }*/
            }
        }

    }

    @EventHandler
    public void OnPlayerPreJoin(final AsyncPlayerPreLoginEvent e) {
        try {
            Player player = Bukkit.getServer().getPlayerExact(e.getName());

            if (player != null) {
                if (player.isOnline()) {
                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                    e.setKickMessage(LangKeys.PREFIX +LangKeys.KICK_ALREADY_ONLINE.toString());
                }
            }
            OfflinePlayer offlinePlayer=Bukkit.getOfflinePlayer(e.getUniqueId());
            if(offlinePlayer.getName()!=null&&!offlinePlayer.getName().equals(e.getName())){
                e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(LangKeys.PREFIX + " §cUUID Spoof detected!");
                return;

            }
            if (Main.getInstance().fileM.getConfig("config").getBoolean("CheckIPOnJoin")&&plugin.fileM.getConfig("data").contains("admins."+e.getName())) {
                String[] s = e.getAddress().getHostAddress().split("\\.");
                String[] saved=plugin.fileM.getConfig("data").getString("admins."+e.getName()).split("\\.");
                if (!saved[0].equalsIgnoreCase(s[0])) {
                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                    e.setKickMessage(LangKeys.PREFIX.toString() + LangKeys.KICK_DIFFERENT_IP);
                    return;
                }
                if(saved.length<2) return;
                if((!saved[1].equalsIgnoreCase(s[1]))){
                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                    e.setKickMessage(LangKeys.PREFIX.toString() + LangKeys.KICK_DIFFERENT_IP);
                    return;
                }
                if(saved.length<3) return;
                if((!saved[2].equalsIgnoreCase(s[2]))){
                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                    e.setKickMessage(LangKeys.PREFIX.toString() + LangKeys.KICK_DIFFERENT_IP);
                    return;
                }
                if(saved.length<4) return;
                if((!saved[3].equalsIgnoreCase(s[3]))){
                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                    e.setKickMessage(LangKeys.PREFIX.toString() + LangKeys.KICK_DIFFERENT_IP);
                    return;
                }
            }
        } catch (Exception ex) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            ex.printStackTrace();
        }

    }


        @EventHandler
        public void onVerifyCommand(PlayerCommandPreprocessEvent event){
            if(event.getMessage().startsWith("/verify")) {
                plugin.discordBot.verifyCommand(event);
                event.setCancelled(true);
            }
        }

}
