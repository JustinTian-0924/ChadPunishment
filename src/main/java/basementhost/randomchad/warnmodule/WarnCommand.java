package basementhost.randomchad.warnmodule;

import basementhost.randomchad.history.PunishmentHistoryManager;
import basementhost.randomchad.history.PunishmentType;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.util.DurationUtil;
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

// handle apply warn related commands
public class WarnCommand implements BasicCommand {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final WarnManager warnManager;
	private final PunishmentHistoryManager historyManager;

	public WarnCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			WarnManager warnManager,
			PunishmentHistoryManager historyManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.warnManager = warnManager;
		this.historyManager = historyManager;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!moduleManager.isWarnEnabled()) {
			langManager.sendMessage(sender, "warn.module-disabled");
			return;
		}

		if (!sender.hasPermission("chadpunishment.warn")) {
			langManager.sendMessage(sender, "warn.no-permission");
			return;
		}

		if (args.length < 2) {
			langManager.sendMessage(sender, "warn.usage");
			return;
		}

		// The duration always occupies the second argument; the remaining text is the reason.
		String durationArgument = args[1];
		String requestedDuration = durationArgument.equalsIgnoreCase("default")
				? plugin.getConfig().getString("warn.default-ttl", "30d")
				: durationArgument;
		long ttlMillis = DurationUtil.parseDurationToMillis(requestedDuration);

		if (ttlMillis <= 0) {
			langManager.sendMessage(sender, "warn.invalid-duration", Map.of("%duration%", requestedDuration));
			langManager.sendMessage(sender, "warn.usage");
			return;
		}

		String targetName = args[0];
		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "warn.player-not-found", Map.of("%player%", targetName));
			return;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 3) {
			reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		}

		WarnRecord record = warnManager.addWarn(target, sender, reason, ttlMillis);
		historyManager.addRecord(
				target,
				sender,
				PunishmentType.WARN,
				reason,
				ttlMillis,
				""
		);
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

	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		if (args.length == 1) {
			return Bukkit.getOnlinePlayers()
					.stream()
					.map(Player::getName)
					.toList();
		}

		if (args.length == 2) {
			return List.of("30s", "10m", "2h", "7d", "default");
		}

		return List.of();
	}
}
