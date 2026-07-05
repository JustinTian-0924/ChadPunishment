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

public class BanCommand implements TabExecutor {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final BanManager banManager;
	private final PunishmentHistoryManager historyManager;

	public BanCommand(
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

		if (!sender.hasPermission("chadpunishment.ban")) {
			langManager.sendMessage(sender, "ban.no-permission");
			return true;
		}

		if (args.length < 1) {
			langManager.sendMessage(sender, "ban.ban-usage");
			return true;
		}

		String targetName = args[0];
		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "ban.player-not-found", Map.of("%player%", targetName));
			return true;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 2 && DurationUtil.isDurationText(args[1])) {
			handleTemporaryBan(sender, target, args);
			return true;
		}

		if (args.length >= 2) {
			reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		}

		BanRecord record = banManager.permanentBan(target, sender, reason);
		historyManager.addRecord(
				target,
				sender,
				PunishmentType.BAN,
				reason,
				-1L,
				""
		);
		langManager.sendMessage(sender, "ban.ban-success", Map.of(
				"%player%", record.getTargetName(),
				"%reason%", reason
		));

		banManager.kickIfOnline(target);

		return true;
	}

	private void handleTemporaryBan(CommandSender sender, OfflinePlayer target, String[] args) {
		String durationText = args[1];
		long durationMillis = DurationUtil.parseDurationToMillis(durationText);

		if (durationMillis <= 0) {
			langManager.sendMessage(sender, "ban.invalid-duration", Map.of("%duration%", durationText));
			return;
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