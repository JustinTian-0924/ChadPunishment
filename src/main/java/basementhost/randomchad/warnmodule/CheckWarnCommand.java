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

import java.util.List;
import java.util.Map;

// handle check warn related commands
public class CheckWarnCommand implements TabExecutor {

	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final WarnManager warnManager;

	public CheckWarnCommand(
			LangManager langManager,
			ModuleManager moduleManager,
			WarnManager warnManager
	) {
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

		if (!sender.hasPermission("chadpunishment.checkwarn")) {
			langManager.sendMessage(sender, "warn.no-permission");
			return true;
		}

		if (args.length != 1) {
			langManager.sendMessage(sender, "warn.check-usage");
			return true;
		}

		String targetName = args[0];
		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "warn.player-not-found", Map.of("%player%", targetName));
			return true;
		}

		List<WarnRecord> activeWarns = warnManager.getActiveWarns(target.getUniqueId());

		langManager.sendRawMessage(sender, "warn.check-header", Map.of(
				"%player%", target.getName() == null ? targetName : target.getName()
		));

		if (activeWarns.isEmpty()) {
			langManager.sendMessage(sender, "warn.check-empty");
			return true;
		}

		long now = System.currentTimeMillis();

		for (int index = 0; index < activeWarns.size(); index++) {
			WarnRecord record = activeWarns.get(index);
			long remainingMillis = record.getExpiresAt() - now;

			langManager.sendRawMessage(sender, "warn.check-line", Map.of(
					"%number%", String.valueOf(index + 1),
					"%reason%", record.getReason(),
					"%issuer%", record.getIssuerName(),
					"%remaining%", DurationUtil.formatDuration(langManager, remainingMillis)
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