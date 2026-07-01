package basementhost.randomchad.mutemodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.util.DurationUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PermaMuteCommand implements TabExecutor {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final MuteManager muteManager;

	public PermaMuteCommand(
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
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!moduleManager.isMuteEnabled()) {
			langManager.sendMessage(sender, "mute.module-disabled");
			return true;
		}

		if (!sender.hasPermission("chadpunishment.permamute")) {
			langManager.sendMessage(sender, "mute.no-permission");
			return true;
		}

		if (args.length < 1) {
			langManager.sendMessage(sender, "mute.perma-usage");
			return true;
		}

		String targetName = args[0];
		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "mute.player-not-found", Map.of("%player%", targetName));
			return true;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 2) {
			reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		}

		MuteRecord record = muteManager.permaMute(target, sender, reason);

		langManager.sendMessage(sender, "mute.perma-success", Map.of(
				"%player%", record.getTargetName(),
				"%reason%", reason
		));

		if (target.isOnline() && target.getPlayer() != null) {
			langManager.sendMessage(target.getPlayer(), "mute.perma-received", Map.of(
					"%reason%", reason
			));
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			return Bukkit.getOnlinePlayers()
					.stream()
					.map(Player::getName)
					.toList();
		}

		return List.of();
	}
}