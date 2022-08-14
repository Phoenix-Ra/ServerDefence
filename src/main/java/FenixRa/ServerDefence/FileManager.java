package FenixRa.ServerDefence;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class FileManager {
    private final HashMap<String,File> files=new HashMap<>();
    private final HashMap<String,FileConfiguration> configs=new HashMap<>();



    protected void LoadFiles() {
        try {
            //config
            FileConfiguration configuration;
            File file = files.put("config", new File(Main.getInstance().getDataFolder(), "config.yml"));
            assert file != null;
            if(file.exists() && !file.isDirectory()) {
                configuration = configs.put("config", YamlConfiguration.loadConfiguration(file));
                assert configuration != null;
                if(configuration.contains("adminsOnlyPerms")&&!configuration.contains("PermsChecker.permissions")){
                    Main.getInstance().getConsole().sendMessage("§c[ServerDefence] Found outdated adminsOnlyPerms setting in config.yml, changing it to PermsChecker...");
                    configuration.set("PermsChecker.active",true);
                    configuration.set("PermsChecker.permissions",configuration.getList("adminsOnlyPerms"));
                    configuration.set("adminsOnlyPerms",null);
                    configuration.save(getFile("config"));
                }
            }else {
                configs.put("config", loadFromResource("config.yml", file));
                getConfig("config").save(file);
            }


            //lang
            file = files.put("lang",new File(Main.getInstance().getDataFolder(), "lang.yml"));
            assert file != null;
            if(file.exists() && !file.isDirectory()) {
                configs.put("lang",YamlConfiguration.loadConfiguration(file));
            }else {
                configuration = configs.put("lang",loadFromResource("lang.yml", file));
                assert configuration != null;
                configuration.save(file);
            }

            //blocked-cmds.yml
            file = files.put("cmd",new File(Main.getInstance().getDataFolder(), "blocked-cmds.yml"));
            assert file != null;
            if(file.exists() && !file.isDirectory()) {
                configs.put("cmd", YamlConfiguration.loadConfiguration(file));
            }else {
                configuration=configs.put("cmd", loadFromResource("blocked-cmds.yml", file));
                assert configuration != null;
                configuration.save(file);
            }

            //tab
            file = files.put("tab",new File(Main.getInstance().getDataFolder(), "tab_blocked-cmds.yml"));
            assert file != null;
            if(file.exists() && !file.isDirectory()) {
                configs.put("tab", YamlConfiguration.loadConfiguration(file));
            }else {
                configuration=configs.put("tab",  loadFromResource("tab_blocked-cmds.yml", file));
                assert configuration != null;
                configuration.save(file);
            }


            //data
            file = files.put("data",new File(Main.getInstance().getDataFolder(), "data.yml"));
            assert file != null;
            if(file.exists() && !file.isDirectory()) {
                configs.put("data", YamlConfiguration.loadConfiguration(file));
            }else {
                if(!file.createNewFile()) throw new Exception("Couldn't create file data.yml");
                configuration=configs.put("data", YamlConfiguration.loadConfiguration(file));
                assert configuration != null;
                configuration.save(file);
            }
        }catch (Exception e){
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("§cWhoops...  Something went wrong while trying to load config files.\n §cContact with a developer  if you need help: https://www.spigotmc.org/members/phoenixra.969595/");

        }
    }

    protected FileConfiguration loadFromResource(String name, File out) {
        try {
            InputStream is = Main.getInstance().getResource(name);
            FileConfiguration f = YamlConfiguration.loadConfiguration(out);
            if (is != null) {
                InputStreamReader isReader = new InputStreamReader(is);
                f.setDefaults(YamlConfiguration.loadConfiguration(isReader));
                f.options().copyDefaults(true);
                f.save(out);
            }
            return f;
        } catch (IOException e) {
            return null;
        }
    }

    protected void reloadFiles(){
        for(String key : configs.keySet()){
            configs.replace(key, YamlConfiguration.loadConfiguration(files.get(key)));
        }
    }




    protected void setPlayerAdmin(String name, String ip){
        getConfig("data").set("admins."+name,ip);
        try {
            getConfig("data").save(getFile("data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void removePlayerAdmin(String name){
        getConfig("data").set("admins."+name,null);
        try {
            getConfig("data").save(getFile("data"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected boolean isAllowedCommandAction(CommandSender sender, String cmd, boolean suggestedCmd){
        if(sender instanceof ConsoleCommandSender) return true;

        if(suggestedCmd){
            if(getConfig("tab").getStringList("cmds").contains(cmd)) return false;
            if(getConfig("tab").getConfigurationSection("permission-only")==null) return true;
            for(String permission: getConfig("tab").getConfigurationSection("permission-only").getKeys(false)){
                if(getConfig("tab").getStringList("permission-only."+permission).contains(cmd)){
                    return sender.hasPermission(permission);
                }
            }

        }else {
            if(getConfig("cmd").getStringList("cmds").contains(cmd)) return false;
            if(getConfig("cmd").getConfigurationSection("permission-only")==null) return true;
            for(String permission: getConfig("cmd").getConfigurationSection("permission-only").getKeys(false)){
                if(getConfig("cmd").getStringList("permission-only."+permission).contains(cmd)){
                    return sender.hasPermission(permission);
                }
            }
        }


        return true;
    }

    protected FileConfiguration getConfig(String type){
        return configs.get(type);
    }
    private File getFile(String type){
        return files.get(type);
    }

}
