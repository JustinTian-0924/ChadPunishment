package basementhost.randomchad.mutemodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UnmuteCommand implements BasicCommand {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final MuteManager muteManager;

	public UnmuteCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			MuteManager muteManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.muteManager = muteManager;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!moduleManager.isMuteEnabled()) {
			langManager.sendMessage(sender, "mute.module-disabled");
			return;
		}

		if (!sender.hasPermission("chadpunishment.unmute")) {
			langManager.sendMessage(sender, "mute.no-permission");
			return;
		}

		if (args.length < 1) {
			langManager.sendMessage(sender, "mute.unmute-usage");
			return;
		}

		String targetName = args[0];
		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "mute.player-not-found", Map.of("%player%", targetName));
			return;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 2) {
			reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		}

		boolean removed = muteManager.unmute(target.getUniqueId());

		if (!removed) {
			langManager.sendMessage(sender, "mute.not-muted", Map.of(
					"%player%", target.getName() == null ? targetName : target.getName()
			));
			return;
		}

		String playerName = target.getName() == null ? targetName : target.getName();

		langManager.sendMessage(sender, "mute.unmute-success", Map.of(
				"%player%", playerName,
				"%reason%", reason
		));

		if (target.isOnline() && target.getPlayer() != null) {
			langManager.sendMessage(target.getPlayer(), "mute.unmuted-received", Map.of(
					"%reason%", reason
			));
		}

	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		if (args.length == 1) {
			return Bukkit.getOnlinePlayers()
					.stream()
					.map(Player::getName)
					.toList();
		}

		return List.of();
	}
}