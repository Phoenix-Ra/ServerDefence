package me.phoenixra.serverdefence;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FileManager {
    private final HashMap<String, File> filesMap = new HashMap<>();
    private final HashMap<String, FileConfiguration> configs = new HashMap<>();


    protected void LoadFiles() {
        try {
            List<String> files = Arrays.asList("config.yml", "lang.yml", "blocked-cmds.yml", "tab_blocked-cmds.yml", "data.yml");

            new File(Main.getInstance().getDataFolder().getPath()).mkdir();

            File file;
            for (String fileName : files) {
                file = new File(Main.getInstance().getDataFolder(), fileName);
                if (file.exists()) {
                    configs.put(fileName.split("\\.")[0], YamlConfiguration.loadConfiguration(file));
                    filesMap.put(fileName.split("\\.")[0],file);
                    continue;
                }
                InputStream is = Main.getInstance().getResource(fileName);
                if (is == null) {
                    file.createNewFile();
                    configs.put(fileName.split("\\.")[0], YamlConfiguration.loadConfiguration(file));
                    filesMap.put(fileName.split("\\.")[0],file);
                    continue;
                }
                try {
                    Files.copy(is, Path.of(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                configs.put(fileName.split("\\.")[0], YamlConfiguration.loadConfiguration(file));
                filesMap.put(fileName.split("\\.")[0],file);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("§cWhoops...  Something went wrong while trying to load config files.\n §cContact with a developer  if you need help: https://www.spigotmc.org/members/phoenixra.969595/");

        }
    }

    protected void reloadFiles() {
        for (String key : configs.keySet()) {
            configs.replace(key, YamlConfiguration.loadConfiguration(filesMap.get(key)));
        }
    }


    protected void setPlayerAdmin(String name, String ip) {
        getConfig("data").set("admins." + name + ".ip", ip);
        try {
            getConfig("data").save(getFile("data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void removePlayerAdmin(String name) {
        getConfig("data").set("admins." + name, null);
        try {
            getConfig("data").save(getFile("data"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void setPlayerDiscord(String name, String dsID) {
        getConfig("data").set("admins." + name + ".dsId", dsID);
        try {
            getConfig("data").save(getFile("data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean isAllowedCommandAction(CommandSender sender, String cmd, boolean suggestedCmd) {
        if (sender instanceof ConsoleCommandSender) return true;

        if (suggestedCmd) {
            if (getConfig("tab").getStringList("cmds").contains(cmd)) return false;
            if (getConfig("tab").getConfigurationSection("permission-only") == null) return true;
            for (String permission : getConfig("tab").getConfigurationSection("permission-only").getKeys(false)) {
                if (getConfig("tab").getStringList("permission-only." + permission).contains(cmd)) {
                    return sender.hasPermission(permission);
                }
            }

        } else {
            if (getConfig("cmd").getStringList("cmds").contains(cmd)) return false;
            if (getConfig("cmd").getConfigurationSection("permission-only") == null) return true;
            for (String permission : getConfig("cmd").getConfigurationSection("permission-only").getKeys(false)) {
                if (getConfig("cmd").getStringList("permission-only." + permission).contains(cmd)) {
                    return sender.hasPermission(permission);
                }
            }
        }


        return true;
    }

    protected FileConfiguration getConfig(String type) {
        if(type.contains("cmd")){
            type="blocked-cmds";
        }else if(type.contains("tab")){
            type="tab_blocked-cmds";
        }
        return configs.get(type);
    }

    private File getFile(String type) {
        if(type.contains("cmd")){
            type="blocked-cmds";
        }else if(type.contains("tab")){
            type="tab_blocked-cmds";
        }
        return filesMap.get(type);
    }

}
