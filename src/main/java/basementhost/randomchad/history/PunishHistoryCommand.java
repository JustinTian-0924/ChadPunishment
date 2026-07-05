package basementhost.randomchad.history;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.util.DateUtil;
import basementhost.randomchad.util.DurationUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class PunishHistoryCommand implements TabExecutor {

	private final LangManager langManager;
	private final PunishmentHistoryManager historyManager;

	public PunishHistoryCommand(
			LangManager langManager,
			PunishmentHistoryManager historyManager
	) {
		this.langManager = langManager;
		this.historyManager = historyManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("chadpunishment.history")) {
			langManager.sendMessage(sender, "history.no-permission");
			return true;
		}

		if (args.length != 1) {
			langManager.sendMessage(sender, "history.usage");
			return true;
		}

		String targetName = args[0];
		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "history.player-not-found", Map.of("%player%", targetName));
			return true;
		}

		List<PunishmentHistoryRecord> records = historyManager.getRecords(target.getUniqueId());
		String displayName = target.getName() == null ? targetName : target.getName();

		if (records.isEmpty()) {
			langManager.sendMessage(sender, "history.empty", Map.of("%player%", displayName));
			return true;
		}

		langManager.sendRawMessage(sender, "history.header", Map.of("%player%", displayName));

		for (int index = 0; index < records.size(); index++) {
			PunishmentHistoryRecord record = records.get(index);

			String durationText = formatDuration(record.getDurationMillis());
			String extra = record.getExtra();

			Map<String, String> placeholders = Map.of(
					"%number%", String.valueOf(index + 1),
					"%type%", record.getType().name(),
					"%issuer%", record.getIssuerName(),
					"%reason%", record.getReason(),
					"%time%", DateUtil.formatMillis(record.getCreatedAt()),
					"%duration%", durationText,
					"%extra%", extra == null ? "" : extra
			);

			if (extra != null && !extra.isBlank()) {
				langManager.sendRawMessage(sender, "history.line-extra", placeholders);
			} else {
				langManager.sendRawMessage(sender, "history.line", placeholders);
			}
		}

		return true;
	}

	private String formatDuration(long durationMillis) {
		if (durationMillis == 0L) {
			return "-";
		}

		return DurationUtil.formatDuration(langManager, durationMillis);
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