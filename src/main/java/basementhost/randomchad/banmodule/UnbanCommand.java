package basementhost.randomchad.banmodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
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

public class UnbanCommand implements TabExecutor {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final BanManager banManager;

	public UnbanCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			BanManager banManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.banManager = banManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!moduleManager.isBanEnabled()) {
			langManager.sendMessage(sender, "ban.module-disabled");
			return true;
		}

		if (!sender.hasPermission("chadpunishment.unban")) {
			langManager.sendMessage(sender, "ban.no-permission");
			return true;
		}

		if (args.length < 1) {
			langManager.sendMessage(sender, "ban.unban-usage");
			return true;
		}

		String targetName = args[0];
		OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

		if (!target.hasPlayedBefore() && !target.isOnline()) {
			langManager.sendMessage(sender, "ban.player-not-found", Map.of("%player%", targetName));
			return true;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 2) {
			reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		}

		boolean removed = banManager.unban(target.getUniqueId());

		if (!removed) {
			langManager.sendMessage(sender, "ban.not-banned", Map.of(
					"%player%", target.getName() == null ? targetName : target.getName()
			));
			return true;
		}

		String playerName = target.getName() == null ? targetName : target.getName();

		langManager.sendMessage(sender, "ban.unban-success", Map.of(
				"%player%", playerName,
				"%reason%", reason
		));

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