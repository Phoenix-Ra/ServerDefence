package FenixRa.ServerDefence;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TabComplete {

    protected TabComplete() {
        if (Main.getInstance().getServerVersion() > 12) {
            Bukkit.getServer().getPluginManager().registerEvents(new newVersion(), Main.getInstance());
        } else {

            if (Main.getInstance().getServerVersion() < 10) {
                Main.getInstance().getConsole().sendMessage(Utils.colorFormat("&cMinecraft version is older than 1.10... tabBlocker won't work correctly ( can only block arguments )"));
                new oldVersion();
            } else {
                Bukkit.getServer().getPluginManager().registerEvents(new oldVersion(), Main.getInstance());
            }
        }
    }


    protected static class newVersion implements Listener {
        @EventHandler
        public void onCommandSend(PlayerCommandSendEvent event) {
            if (!Main.getInstance().fileM.getConfig("config").getBoolean("TabBlocker")) {
                return;
            }
            event.getCommands().removeIf(cmd -> !Main.getInstance().fileM.isAllowedCommandAction(event.getPlayer(), "/"+cmd, true));
            List<String> l = new ArrayList<>();
            if (Main.getInstance().fileM.getConfig("config").contains("FixSyntax.whitelist-cmds")) {
                l.addAll(Main.getInstance().fileM.getConfig("config").getStringList("FixSyntax.whitelist-cmds"));
            }
            event.getCommands().removeIf(cmd -> cmd.contains(":") && !l.contains("/" + cmd.split(":")[0]));
        }
    }

    protected static class oldVersion implements Listener {
        protected oldVersion() {
            initialize();
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onTabComplete(TabCompleteEvent event) {
            try {
                if (!Main.getInstance().fileM.getConfig("config").getBoolean("TabBlocker")) {
                    return;
                }
                List<String> completions = event.getCompletions();
                if (completions.size() == 0) return;
                completions.removeIf(cmd -> !Main.getInstance().fileM.isAllowedCommandAction(event.getSender(), cmd, true));
                List<String> l = new ArrayList<>();
                if (Main.getInstance().fileM.getConfig("config").contains("FixSyntax.whitelist-cmds")) {
                    l.addAll(Main.getInstance().fileM.getConfig("config").getStringList("FixSyntax.whitelist-cmds"));
                }
                completions.removeIf(cmd -> cmd.contains(":") && !l.contains(cmd.split(":")[0]));
                event.setCompletions(completions);
            } catch (Exception e) {
                //prevents warnings spamming
            }
        }

        public void initialize() {

            Main.getInstance().protocolManager.addPacketListener(new PacketAdapter(Main.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.TAB_COMPLETE) {

                public void onPacketReceiving(PacketEvent event) {
                    if (!Main.getInstance().fileM.getConfig("config").getBoolean("TabBlocker")) {
                        return;
                    }
                    if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
                        try {
                            PacketContainer packet = event.getPacket();
                            String message = (packet.getSpecificModifier(String.class).read(0)).toLowerCase();
                            if (message.contains(":")) {
                                if (!Main.getInstance().fileM.getConfig("config").contains("FixSyntax.whitelist-cmds")
                                        || !Main.getInstance().fileM.getConfig("config").getStringList("FixSyntax.whitelist-cmds").contains(message)) {
                                    event.setCancelled(true);
                                    return;
                                }
                            }

                            if(!Main.getInstance().fileM.isAllowedCommandAction(event.getPlayer(),message,true)){
                                event.setCancelled(true);
                            }
                        } catch (FieldAccessException e) {
                            Main.getInstance().getLogger().log(Level.SEVERE, "Couldn't access field.", e);
                        }
                    }
                }
            });
        }
    }

}
