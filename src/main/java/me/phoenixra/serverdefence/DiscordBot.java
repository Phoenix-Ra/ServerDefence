package me.phoenixra.serverdefence;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiscordBot {
    protected HashMap<UUID, String> uuidCodeMap = new HashMap<>();
    protected HashMap<UUID, String> uuidIdMap = new HashMap<>();
    protected List<String> not_verified = new ArrayList<>();
    private Main plugin;
    private Guild guild;
    private JDA jda;

    private CommandReceivedListener listener1;
    private ButtonClickedListener listener2;
    private ReadyListener listener3;

    public DiscordBot(Main main) {
        not_verified = Collections.synchronizedList(not_verified);
        initialize(main);
    }


    private void initialize(Main main) {
        Main.getInstance().getConsole().sendMessage(LangKeys.PREFIX + "§7Initializing DiscordBot");
        this.plugin = main;
        if (!startBot()) {
            Main.getInstance().getConsole().sendMessage(LangKeys.PREFIX + "§cDiscordBot: Failed to start");
            return;
        }
        listener1=new CommandReceivedListener();
        listener2=new ButtonClickedListener();
        listener3=new ReadyListener();
        jda.addEventListener(listener1, listener2, listener3);

        Main.getInstance().getConsole().sendMessage(LangKeys.PREFIX + "§DiscordBot has been initialized");
    }

    private boolean startBot() {
        try {
            jda = JDABuilder.createDefault(plugin.getConfig().getString("DiscordBot.bot_token")).build();
            jda.getPresence().setActivity(Activity.playing("Protecting you now..."));
        } catch (LoginException e) {
            e.printStackTrace();
            plugin.getLogger().warning("------JDA is null, smth went wrong!-----");
            return false;
        }
        if (jda != null) {

            return true;
        }
        plugin.getLogger().warning("------JDA is null, smth went wrong!-----");
        return false;
    }

    protected void verifyCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String code = event.getMessage().replace("/verify ", "");
        if (plugin.getFileM().getConfig("data").contains("admins." + player.getName() + ".dsId")) {
            player.sendMessage("§cYour account is already verified");
            return;
        }
        if (!uuidCodeMap.containsKey(player.getUniqueId())) {
            player.sendMessage("§cVerification process is not started!");
            return;
        }
        String discordid = uuidIdMap.get(player.getUniqueId());
        final Member[] usr = new Member[1];

        guild.retrieveMemberById(discordid).queue(user -> usr[0] = user);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Member target = usr[0];
            String actualcode = uuidCodeMap.get(player.getUniqueId());
            if (!actualcode.equals(code)) {
                player.sendMessage("§cVerification code is wrong! Try again");
                return;
            }

            if (target == null) {
                player.sendMessage("§cError! Can't find you in guild. Please write something in guild and try again");
                return;
            }
            plugin.getFileM().setPlayerDiscord(player.getName(), discordid);
            uuidCodeMap.remove(player.getUniqueId());
            uuidIdMap.remove(player.getUniqueId());
            plugin.getDefenceTask().removeVerify(player);

            for (String s : plugin.getFileM().getConfig("config").getStringList("DiscordBot.cmds-on-verify")) {
                s = s.replace("{player}", player.getName());
                String finalS = s;
                Main.doSync(() ->Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalS));
            }

            not_verified.remove(event.getPlayer().getName());
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Verification");
            eb.setColor(Color.GREEN);
            eb.addField("", ":white_check_mark: **|** Successfully verified: " + player.getName(), true);
            eb.setThumbnail("https://img.pngio.com/steve-face-minecraft-faces-minecraft-costumes-minecraft-face-minecraft-head-png-600_600.png");

            target.getUser().openPrivateChannel().complete().sendMessage(eb.build()).queue();
            player.sendMessage("§aSuccessfully verified with: " + target.getUser().getName() + "#" + target.getUser().getDiscriminator());
        }, 10L);
    }

    protected void sendApproveMessage(Player player, String dsId) {
        try {
            if(not_verified.contains(player.getName())) return;
            not_verified.add(player.getName());
            final User[] usr = new User[1];

            jda.retrieveUserById(dsId).queue(user -> usr[0] = user);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (usr[0] == null) {
                    Bukkit.getConsoleSender().sendMessage(LangKeys.PREFIX + "§cCan't get discord user of player " + player);
                    return;
                }

                usr[0].openPrivateChannel().complete().sendMessage("Someone logged in to your account! Is it you? (you have 1min to answer)").
                        setActionRow(net.dv8tion.jda.api.interactions.components.Button.success("Approve", "Yes, give me admin privileges!"), // Button with only a label
                                Button.danger("Disprove", "No, kick him!")) // Button with only an emoji
                        .queue(button -> {
                            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,()->{
                                try {
                                    if(not_verified.contains(player.getName())) {
                                        not_verified.remove(player.getName());
                                        Player p = Bukkit.getPlayerExact(player.getName());
                                        if (p != null && p.isOnline()) {
                                            Main.doSync(() -> p.kickPlayer("§cThis account is under protection"));
                                        }
                                    }
                                    button.delete().queue();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            },1200);
                        });

                uuidIdMap.put(player.getUniqueId(), dsId);
            }, 20L);

        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(LangKeys.PREFIX + "§cSomething went wrong while trying to send buttons confirmation " + player);
        }

    }

    protected String createVerificationCode(Player target, String userID) {
        String code = "SD_VER-" + new Random(System.nanoTime()).nextInt(800000) + 200000 + "kghcfj";

        uuidCodeMap.put(target.getUniqueId(), code);
        uuidIdMap.put(target.getUniqueId(), userID);
        plugin.getDefenceTask().addVerify(target, 3600L);
        return code;
    }

    protected void VerifyFailed(Player player, String cause) {
        if (!uuidCodeMap.containsKey(player.getUniqueId()) || !uuidIdMap.containsKey(player.getUniqueId())) {
            Bukkit.getConsoleSender().sendMessage(LangKeys.PREFIX + "§cTried to send ds verifyFail message for user which is not verifying");
            return;
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setTitle("Verification");
        eb.addField("", ":x: **|** " + cause, false);

        User target = jda.getUserById(uuidIdMap.get(player.getUniqueId()));
        if (target == null) {
            Bukkit.getConsoleSender().sendMessage(LangKeys.PREFIX + "§cTried to send ds verifyFail message to null user...");
            return;
        }
        target.openPrivateChannel().complete().sendMessage(eb.build()).queue();
        uuidIdMap.remove(player.getUniqueId());
        uuidCodeMap.remove(player.getUniqueId());

    }

    protected boolean isAlreadyVerified(String nick, String dsId) {
        FileConfiguration data = Main.getInstance().getFileM().getConfig("data");
        if (data.contains("admins." + nick + ".dsId")) return true;
        for (String s : data.getConfigurationSection("admins").getKeys(false)) {
            if (!data.contains("admins." + s + ".dsId")) continue;
            if (data.getString("admins." + s + ".dsId").equalsIgnoreCase(dsId)) {
                return true;
            }
        }
        return false;
    }

    protected String getNickByUserId(String dsId) {
        String nick = null;
        FileConfiguration data = Main.getInstance().getFileM().getConfig("data");
        for (String s : data.getConfigurationSection("admins").getKeys(false)) {
            if (!data.contains("admins." + s + ".dsId")) continue;
            if (data.getString("admins." + s + ".dsId").equalsIgnoreCase(dsId)) {
                nick = s;
            }
        }
        return nick;
    }
    public void disable(){
        jda.removeEventListener(listener1,listener2,listener3);
    }

    public class ReadyListener extends ListenerAdapter {
        @Override
        public void onReady(ReadyEvent readyEvent) {
            try {
                guild = readyEvent.getJDA().getGuildById(Objects.requireNonNull(plugin.getFileM().getConfig("config").getString("DiscordBot.guildID")));
                if (guild == null) {
                    Main.getInstance().getConsole().sendMessage(LangKeys.PREFIX + "§cDiscordBot: Guild not found");
                    return;
                }
                guild.upsertCommand("verify", "verify your admin account").
                        addOption(OptionType.STRING, "nickname", "Your nickname in minecraft", true).queue();


                guild.upsertCommand("unverify", "unlink your admin account").queue();

                guild.upsertCommand("set_ip", "sets ip, which will be allowed to login to your linked account").
                        addOption(OptionType.STRING, "ip", "IP address", true).queue();

                guild.upsertCommand("current_ip", "get currently saved IP, which is able to login to your linked account").queue();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private class CommandReceivedListener extends ListenerAdapter {

        @Override
        public void onSlashCommand(SlashCommandEvent event) {
            try {
                if (event.getUser().isBot()) {
                    return;
                }
                String cmd = event.getName();
                EmbedBuilder eb = new EmbedBuilder();
                final Member[] usr = new Member[1];

                guild.retrieveMemberById(event.getUser().getId()).queue(user ->
                {
                    usr[0] = user;
                    if (cmd.equalsIgnoreCase("verify")) {
                        if (plugin.getFileM().getConfig("config").contains("DiscordBot.admin_ds-role") &&
                                usr[0].getRoles().stream().filter(role ->
                                        role.getId().equalsIgnoreCase(plugin.getFileM().getConfig("config").
                                                getString("DiscordBot.admin_ds-role"))).findAny().orElse(null) == null) {

                            event.reply("You don't have permission").queue();
                            return;
                        }

                        String nick = Objects.requireNonNull(event.getOption("nickname")).getAsString();
                        if (plugin.getFileM().getConfig("data").contains("admins." + nick)) {
                            if (isAlreadyVerified(nick, event.getUser().getId())) {
                                eb.clear();
                                eb.setColor(Color.RED);
                                eb.addField("", ":x: **|** You are already verified, enter /unverify to remove verification", false);
                                event.replyEmbeds(eb.build()).queue();
                                return;
                            }
                            Player player = Bukkit.getPlayerExact(nick);
                            if (player == null) {
                                eb.clear();
                                eb.setColor(Color.RED);
                                eb.addField("", ":x: **|** You have to be in game to verify your account", false);
                                event.replyEmbeds(eb.build()).queue();
                                return;
                            }
                            event.getUser().openPrivateChannel().complete().sendMessage("You are the admin! Generating verification code...").queue();

                            String randomcode = createVerificationCode(player, event.getUser().getId());
                            eb.setColor(Color.GREEN);
                            eb.addField("", "Verification code successfully generated!\n" +
                                    "Paste it in game: /verify " + randomcode + "\n :warning: Code will be valid for 3 minutes", false);
                            event.getUser().openPrivateChannel().complete().sendMessage(eb.build()).queue();
                            event.reply("Check private messages :)").queue();
                        } else {
                            event.reply(":x: **|** This account is NOT an admin!").queue();
                        }

                    } else if (cmd.equalsIgnoreCase("unverify")) {
                        if (plugin.getFileM().getConfig("config").contains("DiscordBot.admin_ds-role") &&
                                usr[0].getRoles().stream().filter(role ->
                                        role.getId().equalsIgnoreCase(plugin.getFileM().getConfig("config").
                                                getString("DiscordBot.admin_ds-role"))).findAny().orElse(null) == null) {

                            event.reply("You don't have permission").queue();
                            return;
                        }

                        String nick = getNickByUserId(event.getUser().getId());
                        if (nick == null) {
                            eb.clear();
                            eb.setColor(Color.RED);
                            eb.addField("", ":x: **|** You don't have linked admin account yet", false);
                            event.replyEmbeds(eb.build()).queue();
                            return;
                        }
                        plugin.getFileM().setPlayerDiscord(nick, null);

                        eb.clear();
                        eb.setColor(Color.GREEN);
                        eb.addField("", "Successfully unlinked account with name: " + nick, false);
                        event.replyEmbeds(eb.build()).queue();


                    } else if (cmd.equalsIgnoreCase("set_ip")) {
                        if (plugin.getFileM().getConfig("config").contains("DiscordBot.admin_ds-role") &&
                                usr[0].getRoles().stream().filter(role ->
                                        role.getId().equalsIgnoreCase(plugin.getFileM().getConfig("config").
                                                getString("DiscordBot.admin_ds-role"))).findAny().orElse(null) == null) {

                            event.reply("You don't have permission").queue();
                            return;
                        }

                        String nick = getNickByUserId(event.getUser().getId());
                        if (nick == null) {
                            eb.clear();
                            eb.setColor(Color.RED);
                            eb.addField("", ":x: **|** You don't have linked admin account yet", false);
                            event.replyEmbeds(eb.build()).queue();
                            return;
                        }
                        String ip = Objects.requireNonNull(event.getOption("ip")).getAsString();
                        plugin.getFileM().setPlayerAdmin(nick, ip);

                        eb.clear();
                        eb.setColor(Color.GREEN);
                        eb.addField("", "Allowed IP successfully changed.\n\n :warning: Plugin doesn't check if you entered IP correctly, so, please be attentive:)", false);

                        event.replyEmbeds(eb.build()).queue();
                    } else if (cmd.equalsIgnoreCase("current_ip")) {
                        if (plugin.getFileM().getConfig("config").contains("DiscordBot.admin_ds-role") &&
                                usr[0].getRoles().stream().filter(role ->
                                        role.getId().equalsIgnoreCase(plugin.getFileM().getConfig("config").
                                                getString("DiscordBot.admin_ds-role"))).findAny().orElse(null) == null) {

                            event.reply("You don't have permission").queue();
                            return;
                        }

                        String nick = getNickByUserId(event.getUser().getId());
                        if (nick == null) {
                            eb.clear();
                            eb.setColor(Color.RED);
                            eb.addField("", ":x: **|** You don't have linked admin account yet", false);
                            event.replyEmbeds(eb.build()).queue();
                            return;
                        }
                        FileConfiguration data = plugin.getFileM().getConfig("data");
                        if (!data.contains("admins." + nick + ".ip")) {
                            eb.clear();
                            eb.setColor(Color.RED);
                            eb.addField("", ":x: **|** Can't find your available IP", false);
                            event.replyEmbeds(eb.build()).queue();
                            return;
                        }
                        eb.clear();
                        eb.setColor(Color.GREEN);
                        eb.addField("", "Your current allowed IP: " + data.getString("admins." + nick + ".ip"), false);

                        event.replyEmbeds(eb.build()).queue();
                    }

                }
                );

            } catch (Exception e) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Color.RED);
                eb.setTitle("Critical error!");
                eb.addField("", ":boom: :scream: **|** Whoops... something went wrong," +
                        " please contact with FenixRa if you can't find the problem", false);
                event.replyEmbeds(eb.build()).queue();

                e.printStackTrace();
            }

        }
    }

    private class ButtonClickedListener extends ListenerAdapter {

        @Override
        public void onButtonClick(ButtonClickEvent event) {
            if (event.getComponentId().equals("Approve")) {
                Player player = null;
                for (Map.Entry<UUID, String> entry : uuidIdMap.entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(event.getUser().getId())) {
                        player = Bukkit.getPlayer(entry.getKey());
                        uuidIdMap.remove(entry.getKey());
                        uuidCodeMap.remove(entry.getKey());
                    }
                }
                if (player == null || !player.isOnline()) {
                    if(player!=null) not_verified.remove(player.getName());
                    event.reply("You have to be online!").queue(msg -> msg.deleteOriginal().queueAfter(1, TimeUnit.MINUTES));
                    return;
                }

                for (String s : plugin.getFileM().getConfig("config").getStringList("DiscordBot.cmds-on-verify")) {
                    s = s.replace("{player}", player.getName());
                    String finalS = s;
                    Main.doSync(() ->Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalS));
                }
                not_verified.remove(player.getName());
                event.reply("[TRUE] Your admin privileges has been activated").queue(msg -> msg.deleteOriginal().queueAfter(1, TimeUnit.MINUTES));
            } else if (event.getComponentId().equals("Disprove")) {
                Player player = null;
                for (Map.Entry<UUID, String> entry : uuidIdMap.entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(event.getUser().getId())) {
                        player = Bukkit.getPlayer(entry.getKey());
                        uuidIdMap.remove(entry.getKey());
                        uuidCodeMap.remove(entry.getKey());
                    }
                }
                if (player == null || !player.isOnline()) {
                    if(player!=null) not_verified.remove(player.getName());
                    event.reply("Player is already left a server").queue(msg -> msg.deleteOriginal().queueAfter(1, TimeUnit.MINUTES));
                    return;
                }

                Player p = player;
                Main.doSync(() -> p.kickPlayer("§cThis account is under protection"));

                not_verified.remove(player.getName());
                event.reply("[FALSE] Player has been successfully kicked").queue(msg -> msg.deleteOriginal().queueAfter(1, TimeUnit.MINUTES));

            }
        }


    }
}
