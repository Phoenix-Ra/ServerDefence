package me.phoenixra.serverdefence;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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

        FileConfiguration config = plugin.getFileM().getConfig("config");
        // ':' symbol check
        if (config.getBoolean("FixSyntax.active")) {
            Pattern p = Pattern.compile("^/([a-zA-Z0-9_]+):");
            Matcher m = p.matcher(e.getMessage());
            if (m.find()) {
                String pluginRef = m.group(1);
                if (!config.contains("FixSyntax.whitelist-plugins")||!config.getStringList("FixSyntax.whitelist-plugins").contains(pluginRef)) {
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
        boolean isAdmin=plugin.getFileM().getConfig("data").contains("admins."+e.getPlayer().getName());

        //permissions check
        if(!isAdmin) {
            if (e.getPlayer().isOp() && config.getBoolean("KickUnauthorizedOp")) {
                e.setCancelled(true);
                e.getPlayer().kickPlayer(LangKeys.PREFIX.toString() + LangKeys.KICK_OP);
                return;
            }

            if(config.getBoolean("PermsChecker.active")) {
                if(!config.contains("PermsChecker.permissions")) {
                    plugin.getConsole().sendMessage(LangKeys.PREFIX+
                            "§cPermsChecker.permissions list in config.yml is not found! PermsChecker won't work");
                    return;
                }
                for (String s : config.getStringList("PermsChecker.permissions")) {
                    if (e.getPlayer().hasPermission(s)) {
                        e.setCancelled(true);
                        e.getPlayer().kickPlayer(LangKeys.PREFIX.toString() + LangKeys.KICK_PERMISSION);
                        return;
                    }
                }
            }
        }

        //commands check
        if(config.getBoolean("CommandBlocker")) {
            String[] split;
            String word = null;
            for (int length = (split = e.getMessage().split(" ")).length, i = 0; i < length; ++i) {
                if (word != null) {
                    word = word + " " + split[i];
                } else {
                    word = split[i];
                }
                //is blockedCmd check

                if(!Main.getInstance().getFileM().isAllowedCommandAction(e.getPlayer(), word, false)){
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(LangKeys.PREFIX.toString() + LangKeys.CONSOLE_ONLY_CMD);
                    return;
                }
            }
        }

    }

    @EventHandler
    public void OnPlayerPreJoin(final AsyncPlayerPreLoginEvent e) {
        try {
            Player player = Bukkit.getServer().getPlayerExact(e.getName());

            FileConfiguration config = plugin.getFileM().getConfig("config");
            if (player != null && player.isOnline()) {
                e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(LangKeys.PREFIX +LangKeys.KICK_ALREADY_ONLINE.toString());
            }
            if(config.getBoolean("UUIDSpoofFix")) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(e.getUniqueId());
                if (offlinePlayer.getName() != null && !offlinePlayer.getName().equals(e.getName())) {
                    e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                    e.setKickMessage(LangKeys.PREFIX + " §cUUID Spoof detected!");
                    return;

                }
            }
            FileConfiguration data = plugin.getFileM().getConfig("data");

            if (config.getBoolean("CheckIPOnJoin")&&data.contains("admins."+e.getName()+".ip")) {
                String[] s = e.getAddress().getHostAddress().split("\\.");
                String[] saved=data.getString("admins."+e.getName()+".ip").split("\\.");
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
                }
            }
        } catch (Exception ex) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            ex.printStackTrace();
        }

    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e){

        FileConfiguration config = plugin.getFileM().getConfig("config");
        FileConfiguration data = plugin.getFileM().getConfig("data");

        if(!config.getBoolean("DiscordBot.active")) return;
        if(!data.contains("admins."+e.getPlayer().getName())) return;

        for(String s : config.getStringList("DiscordBot.cmds-on-login")){
            s=s.replace("{player}",e.getPlayer().getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),s);
        }

        DiscordBot dsBot= plugin.getDiscordBot();
        if(data.contains("admins."+e.getPlayer().getName()+".dsId")){
            dsBot.sendApproveMessage(e.getPlayer(),data.getString("admins."+e.getPlayer().getName()+".dsId"));

            Bukkit.getScheduler().runTaskLater(plugin,()->{
                if(e.getPlayer().isOnline() && dsBot.not_verified.contains(e.getPlayer().getName())){
                    dsBot.uuidIdMap.remove(e.getPlayer().getUniqueId());
                    dsBot.uuidCodeMap.remove(e.getPlayer().getUniqueId());
                    e.getPlayer().kickPlayer("§cThis account is under protection");
                }

            },1500L);
        }else{
            if(!dsBot.not_verified.contains(e.getPlayer().getName())){
                dsBot.not_verified.add(e.getPlayer().getName());
            }
            Bukkit.getScheduler().runTaskLater(plugin,()->{
                if(e.getPlayer().isOnline() && dsBot.not_verified.contains(e.getPlayer().getName())){
                    e.getPlayer().kickPlayer("§cThis account is under protection");
                }

            },1500L);
        }

    }

        @EventHandler
        public void onVerifyCommand(PlayerCommandPreprocessEvent event){
            if(event.getMessage().startsWith("/verify")) {
                plugin.getDiscordBot().verifyCommand(event);
                event.setCancelled(true);
            }
        }

}
