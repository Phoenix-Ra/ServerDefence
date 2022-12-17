package me.phoenixra.serverdefence;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class DefenceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!sender.hasPermission("serverDef.admin")) {
            sender.sendMessage("§cYou don't have permission to execute this command");
            return true;
        }
        if(args.length==0){
            sender.sendMessage(getHelp());
            return true;
        }

        try {

            if (args[0].equalsIgnoreCase("reload")) {
                Main.getInstance().getFileM().reloadFiles();
                if (Main.getInstance().getServerVersion() > 12) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.updateCommands();
                    }
                }
                sender.sendMessage(LangKeys.PREFIX + "§aSuccessfully reloaded");
            } else if (args[0].equalsIgnoreCase("setPlayer")) {
                if (args.length < 3) {
                    sender.sendMessage(LangKeys.PREFIX + "§cMore arguments needed");
                    return true;
                }
                String name = args[1];
                String ip = args[2];
                if (ip.split("\\.").length > 4) {
                    sender.sendMessage(LangKeys.PREFIX + "§cIncorrect IP address, contains more than 3 dots");
                    return true;

                }
                if (Main.getInstance().getFileM().getConfig("data").contains("admins." + name)) {
                    sender.sendMessage(LangKeys.PREFIX + "§aPlayer IP successfully §eupdated");
                } else {
                    sender.sendMessage(LangKeys.PREFIX + "§aPlayer successfully §eadded §ato the admin list");
                }
                Main.getInstance().getFileM().setPlayerAdmin(name, ip);

            } else if (args[0].equalsIgnoreCase("removePlayer")) {
                if (args.length < 2) {
                    sender.sendMessage(LangKeys.PREFIX + "§cMore arguments needed");
                    return true;
                }
                String name = args[1];
                if (!Main.getInstance().getFileM().getConfig("data").contains("admins." + name)) {
                    sender.sendMessage(LangKeys.PREFIX + "§cPlayer wasn't added to the admin list");
                    return true;
                }
                Main.getInstance().getFileM().removePlayerAdmin(name);
                sender.sendMessage(LangKeys.PREFIX + "§aPlayer successfully §eremoved §afrom the admin list");

            } else if (args[0].equalsIgnoreCase("adminList")) {
                if (Main.getInstance().getFileM().getConfig("data").getConfigurationSection("admins").getKeys(false).size() == 0) {
                    sender.sendMessage(LangKeys.PREFIX + "§cAdmin list is empty.");
                    return true;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("\n§c-----------------§eAdminList§c-----------------\n");
                for (String p : Main.getInstance().getFileM().getConfig("data").getConfigurationSection("admins").getKeys(false)) {
                    sb.append("§c|  §fPlayer: §e").append(p).append("  §fIP: §e").append(Main.getInstance().getFileM().getConfig("data").getString("admins." + p)).append("\n");
                }
                sb.append("§c-----------------§eAdminList§c-----------------");
                sender.sendMessage(sb.toString());

            } else if (args[0].equalsIgnoreCase("checkIp")) {
                if (args.length < 2) {
                    sender.sendMessage(LangKeys.PREFIX + "§cMore arguments needed");
                    return true;
                }
                Player player = Bukkit.getPlayerExact(args[1]);
                if (player == null || !player.isOnline()) {
                    sender.sendMessage(LangKeys.PREFIX + "§cSpecified player not found");
                    return true;
                }
                sender.sendMessage("§aIP of "+player.getName()+" is: §6" + Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress());


            } else if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(getHelp());

            }
        }catch (Exception e){
            e.printStackTrace();
            sender.sendMessage("§cWhoops...  Something went wrong while trying to execute a command.\n §cContact with a developer  if you need help: https://www.spigotmc.org/members/phoenixra.969595/");
        }

        return true;

    }

    private String getHelp(){
        return "\n§c-----------------§eCommands§c-----------------\n" +
                "§e /sd reload §7>§f reload plugin files" + "\n" +
                "§e /sd setPlayer [nickname] [ip] §7>§f adds player to the admin list, or updates IP if a player is already added to the list" + "\n" +
                "§e /sd removePlayer [nickname] §7>§f removes player from admin list" + "\n" +
                "§e /sd adminList §7>§f shows list of admin players" + "\n" +
                "§e /sd checkIp [nickname]§7 >§f shows an IP address of specified player" + "\n" +
                "§c-----------------§eCommands§c-----------------";
    }
}
