package FenixRa.ServerDefence.other;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public class Metrics {
    public static final int B_STATS_VERSION = 1;
    private static final String URL = "https://bStats.org/submitData/bukkit";
    private boolean enabled;
    private static boolean logFailedRequests;
    private static boolean logSentData;
    private static boolean logResponseStatusText;
    private static String serverUUID;
    private final Plugin plugin;
    private final int pluginId;

    public Metrics(Plugin plugin, int pluginId) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        this.plugin = plugin;
        this.pluginId = pluginId;
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.addDefault("logSentData", false);
            config.addDefault("logResponseStatusText", false);
            config.options().header("bStats collects some data for plugin authors like how many servers are using their plugins.\nTo honor their work, you should not disable it.\nThis has nearly no effect on the server performance!\nCheck out https://bStats.org/ to learn more :)").copyDefaults(true);
            try {
                config.save(configFile);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        serverUUID = config.getString("serverUuid");
        logFailedRequests = config.getBoolean("logFailedRequests", false);
        this.enabled = config.getBoolean("enabled", true);
        logSentData = config.getBoolean("logSentData", false);
        logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        if (this.enabled) {
            boolean found = false;
            for (Class service : Bukkit.getServicesManager().getKnownServices()) {
                try {
                    service.getField("B_STATS_VERSION");
                    found = true;
                    break;
                }
                catch (NoSuchFieldException noSuchFieldException) {
                }
            }
            Bukkit.getServicesManager().register(Metrics.class,this, plugin, ServicePriority.Normal);
            if (!found) {
                this.startSubmitting();
            }
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    private void startSubmitting() {
        final Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask(){

            @Override
            public void run() {
                if (!Metrics.this.plugin.isEnabled()) {
                    timer.cancel();
                    return;
                }
                Bukkit.getScheduler().runTask(Metrics.this.plugin, Metrics.this::submitData);
            }
        }, 300000L, 1800000L);
    }

    public JsonObject getPluginData() {
        JsonObject data = new JsonObject();
        String pluginName = this.plugin.getDescription().getName();
        String pluginVersion = this.plugin.getDescription().getVersion();
        data.addProperty("pluginName", pluginName);
        data.addProperty("id", this.pluginId);
        data.addProperty("pluginVersion", pluginVersion);
        data.add("customCharts", new JsonArray());
        return data;
    }

    private JsonObject getServerData() {
        int playerAmount;
        try {
            Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
            playerAmount = onlinePlayersMethod.getReturnType().equals(Collection.class) ? ((Collection)onlinePlayersMethod.invoke(Bukkit.getServer(), new Object[0])).size() : ((Player[])onlinePlayersMethod.invoke(Bukkit.getServer(), new Object[0])).length;
        }
        catch (Exception e) {
            playerAmount = Bukkit.getOnlinePlayers().size();
        }
        int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
        String bukkitVersion = Bukkit.getVersion();
        String bukkitName = Bukkit.getName();
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        int coreCount = Runtime.getRuntime().availableProcessors();
        JsonObject data = new JsonObject();
        data.addProperty("serverUUID", serverUUID);
        data.addProperty("playerAmount", playerAmount);
        data.addProperty("onlineMode", onlineMode);
        data.addProperty("bukkitVersion", bukkitVersion);
        data.addProperty("bukkitName", bukkitName);
        data.addProperty("javaVersion", javaVersion);
        data.addProperty("osName", osName);
        data.addProperty("osArch", osArch);
        data.addProperty("osVersion", osVersion);
        data.addProperty("coreCount", coreCount);
        return data;
    }

    private void submitData() {
        JsonObject data = this.getServerData();
        JsonArray pluginData = new JsonArray();
        for (Class service : Bukkit.getServicesManager().getKnownServices()) {
            try {
                service.getField("B_STATS_VERSION");
                for (Object obj : Bukkit.getServicesManager().getRegistrations(service)) {
                    RegisteredServiceProvider provider=(RegisteredServiceProvider)obj;
                    try {
                        Object plugin = provider.getService().getMethod("getPluginData", new Class[0]).invoke(provider.getProvider());
                        if (plugin instanceof JsonObject) {
                            pluginData.add((JsonElement) plugin);
                            continue;
                        }
                        try {
                            Class<?> jsonObjectJsonSimple = Class.forName("org.json.simple.JSONObject");
                            if (!plugin.getClass().isAssignableFrom(jsonObjectJsonSimple)) continue;
                            Method jsonStringGetter = jsonObjectJsonSimple.getDeclaredMethod("toJSONString");
                            jsonStringGetter.setAccessible(true);
                            String jsonString = (String)jsonStringGetter.invoke(plugin, new Object[0]);
                            JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();
                            pluginData.add(object);
                        }
                        catch (ClassNotFoundException e) {
                            if (!logFailedRequests) continue;
                            this.plugin.getLogger().log(Level.SEVERE, "Encountered unexpected exception ", e);
                        }
                    }
                    catch (IllegalAccessException | NoSuchMethodException | NullPointerException | InvocationTargetException exception) {}
                }
            }
            catch (NoSuchFieldException noSuchFieldException) {
            }
        }
        data.add("plugins", pluginData);
        new Thread(() -> {
            block2: {
                try {
                    Metrics.sendData(this.plugin, data);
                }
                catch (Exception e) {
                    if (!logFailedRequests) break block2;
                    this.plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + this.plugin.getName(), e);
                }
            }
        }).start();
    }

    private static void sendData(Plugin plugin, JsonObject data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null!");
        }
        if (Bukkit.isPrimaryThread()) {
            throw new IllegalAccessException("This method must not be called from the main thread!");
        }
        if (logSentData) {
            plugin.getLogger().info("Sending data to bStats: " + data);
        }
        HttpsURLConnection connection = (HttpsURLConnection)new URL(URL).openConnection();
        byte[] compressedData = Metrics.compress(data.toString());
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "MC-Server/1");
        connection.setDoOutput(true);
        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())){
            outputStream.write(compressedData);
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        }
        if (logResponseStatusText) {
            plugin.getLogger().info("Sent data to bStats and received response: " + builder);
        }
    }

    private static byte[] compress(String str) throws IOException {
        if (str == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)){
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
        }
        return outputStream.toByteArray();
    }

    static {
        if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
            String defaultPackage = new String(new byte[]{111, 114, 103, 46, 98, 115, 116, 97, 116, 115, 46, 98, 117, 107, 107, 105, 116});
            String examplePackage = new String(new byte[]{121, 111, 117, 114, 46, 112, 97, 99, 107, 97, 103, 101});
            if (Metrics.class.getPackage().getName().equals(defaultPackage) || Metrics.class.getPackage().getName().equals(examplePackage)) {
                throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
            }
        }
    }
}