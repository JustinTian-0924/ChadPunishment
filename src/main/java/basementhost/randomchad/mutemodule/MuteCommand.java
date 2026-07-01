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

public class MuteCommand implements TabExecutor {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final MuteManager muteManager;

	public MuteCommand(
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

		if (!sender.hasPermission("chadpunishment.mute")) {
			langManager.sendMessage(sender, "mute.no-permission");
			return true;
		}

		if (args.length < 2) {
			langManager.sendMessage(sender, "mute.usage");
			return true;
		}

		String targetName = args[0];
		String durationText = args[1];

		long durationMillis = DurationUtil.parseDurationToMillis(durationText);

		if (durationMillis <= 0) {
			langManager.sendMessage(sender, "mute.invalid-duration", Map.of("%duration%", durationText));
			return true;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "mute.player-not-found", Map.of("%player%", targetName));
			return true;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 3) {
			reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		}

		MuteRecord record = muteManager.mute(target, sender, reason, durationMillis);
		String formattedDuration = DurationUtil.formatDuration(langManager, durationMillis);

		langManager.sendMessage(sender, "mute.success", Map.of(
				"%player%", record.getTargetName(),
				"%reason%", reason,
				"%duration%", formattedDuration
		));

		if (target.isOnline() && target.getPlayer() != null) {
			langManager.sendMessage(target.getPlayer(), "mute.received", Map.of(
					"%reason%", reason,
					"%duration%", formattedDuration
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

		if (args.length == 2) {
			return List.of("30s", "10m", "1h", "1d", "7d");
		}

		return List.of();
	}
}