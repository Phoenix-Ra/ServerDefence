package me.phoenixra.serverdefence;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.resolver.data.Repository;
import io.github.slimjar.resolver.mirrors.SimpleMirrorSelector;
import lombok.AccessLevel;
import lombok.Getter;
import me.phoenixra.serverdefence.other.Metrics;
import me.phoenixra.serverdefence.other.SlimJarLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Objects;


public class Main extends JavaPlugin implements Listener {
    private static Main plugin;
    @Getter(AccessLevel.PROTECTED) private FileManager fileM;
    @Getter(AccessLevel.PROTECTED) private DefenceTask defenceTask;
    @Getter(AccessLevel.PROTECTED) private DiscordBot discordBot;

    private TabComplete tabComplete;
    @Override
    public void onLoad() {
        getLogger().info("Downloading dependencies...");
        try {
            Path downloadPath = Paths.get(getDataFolder().getPath() + File.separator + "libs");
            ApplicationBuilder.appending("ServerDefence")
                    .logger(new SlimJarLogger(this))
                    .downloadDirectoryPath(downloadPath)
                    .mirrorSelector((a, b) -> a)
                    .internalRepositories(Collections.singleton(new Repository(new URL(SimpleMirrorSelector.ALT_CENTRAL_URL))))
                    .build();
        } catch (IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        getLogger().info("Dependencies successfully downloaded");
    }
    @Override
    public void onEnable() {
        getConsole().sendMessage("§fInitializing §eServerDefence §fversion §e"+ this.getDescription().getVersion());
        plugin = this;
        fileM = new FileManager();
        fileM.LoadFiles();

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(plugin), this);
        defenceTask = new DefenceTask(plugin,5);

        tabComplete = new TabComplete();

        Objects.requireNonNull(plugin.getCommand("serverdefence")).setExecutor(new DefenceCommand());

        if(getServerVersion() > 12) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.updateCommands();
            }
        }
        ByteMessageListener bml = new ByteMessageListener();
        ChannelControl.registerChannels(bml);

        if(getConfig().getBoolean("DiscordBot.active")){
            discordBot = new DiscordBot(this);
        }

        if ((!fileM.getConfig("config").contains("metrics")||fileM.getConfig("config").getBoolean("metrics")) && (new Metrics(this, 14524)).isEnabled()) {
            getConsole().sendMessage("§7Metrics loaded successfully");
        }

        getConsole().sendMessage("§eServerDefence §fhas been enabled");
        doAsync(this::checkVersion);
    }

    @Override
    public void onDisable() {
        if(tabComplete.tabCompleteOld!=null&&tabComplete.tabCompleteOld.protocolManager!=null) {
            tabComplete.tabCompleteOld.protocolManager.removePacketListeners(this);
        }
        if(discordBot!=null){
            discordBot.disable();
        }
        getConsole().sendMessage("§eServerDefence§f has been disabled!");
    }

    private void checkVersion() {
        String currentVersion = Main.getInstance().getDescription().getVersion();
        URL url;
        try {
            url = new URL("https://api.spigotmc.org/legacy/update.php?resource=100438");
        }
        catch (MalformedURLException e) {
            return;
        }
        URLConnection conn;
        try {
            conn = url.openConnection();
        }
        catch (IOException e) {
            return;
        }
        try {
            assert (conn != null);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String newVersion=reader.readLine();
            if (!newVersion.equals(currentVersion)) {
                Main.getInstance().getConsole().sendMessage("§6A new version available! Download §aServerDefence v"
                        +newVersion+" §6at https://www.spigotmc.org/resources/100438/");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getServerVersion(){
        return Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].split("_")[1]);
    }

    public static void doSync(Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }
    public static void doAsync(Runnable runnable) { plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable); }

    public static Main getInstance(){
        return plugin;
    }
    public CommandSender getConsole(){
        return Bukkit.getConsoleSender();
    }




}
