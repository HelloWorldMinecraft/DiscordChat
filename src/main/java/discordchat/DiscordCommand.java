package discordchat;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class DiscordCommand extends Command {
	public DiscordCommand() {
		super("Discord");
	}

	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission("discordchat.invite")) sender.sendMessage(new ComponentBuilder("You can't do that.").color(ChatColor.RED).create());
		sender.sendMessage(new ComponentBuilder("Join us at https://discord.gg/nYKJ43Z").color(ChatColor.GOLD).create());
	}
}
