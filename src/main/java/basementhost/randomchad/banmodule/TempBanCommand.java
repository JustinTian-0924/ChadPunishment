package basementhost.randomchad.banmodule;

import basementhost.randomchad.history.PunishmentHistoryManager;
import basementhost.randomchad.history.PunishmentType;
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

public class TempBanCommand implements TabExecutor {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final BanManager banManager;
	private final PunishmentHistoryManager historyManager;

	public TempBanCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			BanManager banManager,
			PunishmentHistoryManager historyManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.banManager = banManager;
		this.historyManager = historyManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!moduleManager.isBanEnabled()) {
			langManager.sendMessage(sender, "ban.module-disabled");
			return true;
		}

		if (!sender.hasPermission("chadpunishment.tempban")) {
			langManager.sendMessage(sender, "ban.no-permission");
			return true;
		}

		if (args.length < 2) {
			langManager.sendMessage(sender, "ban.tempban-usage");
			return true;
		}

		String targetName = args[0];
		String durationText = args[1];

		long durationMillis = DurationUtil.parseDurationToMillis(durationText);

		if (durationMillis <= 0) {
			langManager.sendMessage(sender, "ban.invalid-duration", Map.of("%duration%", durationText));
			return true;
		}

		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "ban.player-not-found", Map.of("%player%", targetName));
			return true;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 3) {
			reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		}

		BanRecord record = banManager.tempBan(target, sender, reason, durationMillis);
		historyManager.addRecord(
				target,
				sender,
				PunishmentType.TEMPBAN,
				reason,
				durationMillis,
				""
		);
		String formattedDuration = DurationUtil.formatDuration(langManager, durationMillis);

		langManager.sendMessage(sender, "ban.tempban-success", Map.of(
				"%player%", record.getTargetName(),
				"%reason%", reason,
				"%duration%", formattedDuration
		));

		banManager.kickIfOnline(target);

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
			return List.of("30s", "10m", "1h", "1d", "7d", "30d");
		}

		return List.of();
	}
}