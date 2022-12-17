package me.phoenixra.serverdefence;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    protected static byte[] getSchematicaPayload() {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeByte(0);
        output.writeBoolean(false);
        output.writeBoolean(false);
        output.writeBoolean(false);
        return output.toByteArray();
    }

    protected static byte[] createWDLPacket0() {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeInt(0);
        output.writeBoolean(false);
        return output.toByteArray();
    }

    protected static byte[] createWDLPacket1() {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeInt(1);
        output.writeBoolean(false);
        output.writeInt(0);
        output.writeBoolean(false);
        output.writeBoolean(false);
        output.writeBoolean(false);
        output.writeBoolean(false);
        return output.toByteArray();
    }

    protected static void broadcastAdmins(String message) {
        if (message.trim().isEmpty()) {
            return;
        }
        for (Player p:Main.getInstance().getServer().getOnlinePlayers()){
            if(Main.getInstance().getFileM().getConfig("data").contains("admins."+p.getName())){
                p.sendMessage(colorFormat(message));
            }
        }
    }


    protected static String colorFormat(String s){
        try{
            if(Main.getInstance().getServerVersion()>15) {
                s = translateHexCodes(s);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        s= ChatColor.translateAlternateColorCodes('&',s);
        return s;
    }
    protected static String translateHexCodes (String textToTranslate) {
        final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());

    }

    protected static boolean isInvalidPlayer(HumanEntity human) {
        if(!(human instanceof Player)) return true;
        final Player player = (Player) human;
        return player.hasMetadata("NPC") || !player.isOnline();
    }

}
