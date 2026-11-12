package basementhost.randomchad.warnmodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.util.DurationUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

// handle check warn related commands
public class CheckWarnCommand implements BasicCommand {

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
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!moduleManager.isWarnEnabled()) {
			langManager.sendMessage(sender, "warn.module-disabled");
			return;
		}

		if (!sender.hasPermission("chadpunishment.checkwarn")) {
			langManager.sendMessage(sender, "warn.no-permission");
			return;
		}

		if (args.length != 1) {
			langManager.sendMessage(sender, "warn.check-usage");
			return;
		}

		String targetName = args[0];
		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "warn.player-not-found", Map.of("%player%", targetName));
			return;
		}

		List<WarnRecord> activeWarns = warnManager.getActiveWarns(target.getUniqueId());

		langManager.sendRawMessage(sender, "warn.check-header", Map.of(
				"%player%", target.getName() == null ? targetName : target.getName()
		));

		if (activeWarns.isEmpty()) {
			langManager.sendMessage(sender, "warn.check-empty");
			return;
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