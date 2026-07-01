package basementhost.randomchad.warnmodule;

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

// handle apply warn related commands
public class WarnCommand implements TabExecutor {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final WarnManager warnManager;

	public WarnCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			WarnManager warnManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.warnManager = warnManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!moduleManager.isWarnEnabled()) {
			langManager.sendMessage(sender, "warn.module-disabled");
			return true;
		}

		if (!sender.hasPermission("chadpunishment.warn")) {
			langManager.sendMessage(sender, "warn.no-permission");
			return true;
		}

		if (args.length < 1) {
			langManager.sendMessage(sender, "warn.usage");
			return true;
		}

		String targetName = args[0];
		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "warn.player-not-found", Map.of("%player%", targetName));
			return true;
		}

		String defaultTtlText = plugin.getConfig().getString("warn.default-ttl", "30d");
		long ttlMillis = DurationUtil.parseDurationToMillis(defaultTtlText);

		if (ttlMillis <= 0) {
			ttlMillis = DurationUtil.parseDurationToMillis("30d");
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 2) {
			String lastArg = args[args.length - 1];

			if (DurationUtil.isDurationText(lastArg)) {
				long customTtlMillis = DurationUtil.parseDurationToMillis(lastArg);

				if (customTtlMillis <= 0) {
					langManager.sendMessage(sender, "warn.invalid-duration", Map.of("%duration%", lastArg));
					return true;
				}

				ttlMillis = customTtlMillis;

				if (args.length >= 3) {
					reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
				}
			} else {
				reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			}
		}

		WarnRecord record = warnManager.addWarn(target, sender, reason, ttlMillis);
		int activeWarnCount = warnManager.getActiveWarnCount(target.getUniqueId());

		String durationText = DurationUtil.formatDuration(langManager, ttlMillis);

		langManager.sendMessage(sender, "warn.success", Map.of(
				"%player%", record.getTargetName(),
				"%reason%", reason,
				"%duration%", durationText,
				"%active_warns%", String.valueOf(activeWarnCount)
		));

		if (target.isOnline() && target.getPlayer() != null) {
			langManager.sendMessage(target.getPlayer(), "warn.received", Map.of(
					"%reason%", reason,
					"%duration%", durationText
			));
		}

		warnManager.runActiveWarnActions(target, reason, sender, activeWarnCount, ttlMillis);

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

		if (args.length >= 2) {
			return List.of("30s", "10m", "2h", "7d");
		}

		return List.of();
	}
}