package discordchat;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.config.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import xyz.olivermartin.multichat.bungee.events.PostGlobalChatEvent;

public class DiscordChat extends Plugin implements Listener {
	private JDA jda;
	private TextChannel channel;
	private Configuration config;

	public void onEnable() {
		try {
			if (!getDataFolder().exists()) getDataFolder().mkdir();
			File configFile = new File(getDataFolder(), "config.yml");

			if (!configFile.exists()) {
				try (InputStream stream = getResourceAsStream("config.yml")) {
					Files.copy(stream, configFile.toPath());
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}

			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));

			this.jda = JDABuilder.createDefault(config.getString("token"), Arrays.asList(GatewayIntent.GUILD_MESSAGES)).addEventListeners(new MessageListener()).build();
			this.jda.awaitReady();
			this.channel = (TextChannel) jda.getGuildChannelById(config.getString("channel"));

			this.getProxy().getPluginManager().registerListener(this, this);
			this.getProxy().getPluginManager().registerCommand(this, new DiscordCommand());
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private class MessageListener extends ListenerAdapter {
		@Override
		public void onMessageReceived(MessageReceivedEvent event) {
			if (!(event.getChannel().getId().equals(config.getString("channel"))) || event.getAuthor().isBot()) return;
			DiscordChat.this.getProxy().broadcast(new TextComponent("[Discord] " + event.getMember().getEffectiveName() + ": " + event.getMessage().getContentDisplay()));
		}
	}

	public boolean isHidden(ProxiedPlayer player) {
		return this.config.getList("hidden").contains(player.getServer().getInfo().getName());
	}

	@EventHandler
	public void onLogout(PlayerDisconnectEvent event) {
		if (!isHidden(event.getPlayer())) this.channel.sendMessage(event.getPlayer().getDisplayName() + " left the game.").queue();
	}

	@EventHandler
	public void onJoinServer(ServerSwitchEvent event) {
		if (isHidden(event.getPlayer())) {
			if (event.getFrom() != null) this.channel.sendMessage(event.getPlayer().getDisplayName() + " left the game.").queue();
		} else {
			if (event.getFrom() == null || config.getList("hidden").contains(event.getFrom().getName())) this.channel.sendMessage(event.getPlayer().getDisplayName() + " has joined " + event.getPlayer().getServer().getInfo().getMotd()).queue();
			else this.channel.sendMessage(event.getPlayer().getDisplayName() + " has switched from " + event.getFrom().getMotd() + " to " + event.getPlayer().getServer().getInfo().getMotd()).queue();
		}
	}

	@EventHandler
	public void onPostChatEvent(PostGlobalChatEvent event) {
		if (event.isCancelled() || isHidden(event.getSender())) return;
		String nickname = event.getRawSenderNickname();
		this.channel.sendMessage(event.getRawSenderPrefix() + " " + (nickname.isEmpty() ? event.getRawSenderDisplayName() : nickname) + ": " + event.getRawMessage()).queue();
	}
}
