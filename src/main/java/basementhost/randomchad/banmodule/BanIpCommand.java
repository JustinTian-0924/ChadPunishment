package basementhost.randomchad.banmodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.util.DurationUtil;
import basementhost.randomchad.util.IpUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BanIpCommand implements TabExecutor {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final IpBanManager ipBanManager;

	public BanIpCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			IpBanManager ipBanManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.ipBanManager = ipBanManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!moduleManager.isBanIpEnabled()) {
			langManager.sendMessage(sender, "ban.module-disabled");
			return true;
		}

		if (!sender.hasPermission("chadpunishment.banip")) {
			langManager.sendMessage(sender, "ban.no-permission");
			return true;
		}

		if (args.length < 1) {
			langManager.sendMessage(sender, "ban.banip-usage");
			return true;
		}

		String input = args[0];
		String ip = resolveIpFromInput(input, sender);

		if (ip == null) {
			return true;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 2 && DurationUtil.isDurationText(args[1])) {
			handleTemporaryIpBan(sender, ip, args);
			return true;
		}

		if (args.length >= 2) {
			reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		}

		IpBanRecord record = ipBanManager.permanentBanIp(ip, sender.getName(), reason);

		langManager.sendMessage(sender, "ban.banip-success", Map.of(
				"%ip%", record.getIp(),
				"%reason%", reason
		));

		ipBanManager.kickOnlinePlayersWithIp(ip);

		return true;
	}

	private void handleTemporaryIpBan(CommandSender sender, String ip, String[] args) {
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

		IpBanRecord record = ipBanManager.tempBanIp(ip, sender.getName(), reason, durationMillis);
		String formattedDuration = DurationUtil.formatDuration(langManager, durationMillis);

		langManager.sendMessage(sender, "ban.tempbanip-success", Map.of(
				"%ip%", record.getIp(),
				"%reason%", reason,
				"%duration%", formattedDuration
		));

		ipBanManager.kickOnlinePlayersWithIp(ip);
	}

	private String resolveIpFromInput(String input, CommandSender sender) {
		if (IpUtil.isIpAddress(input)) {
			return input;
		}

		Player target = Bukkit.getPlayerExact(input);

		if (target == null) {
			langManager.sendMessage(sender, "ban.ip-player-offline", Map.of("%player%", input));
			return null;
		}

		String ip = IpUtil.getPlayerIp(target);

		if (ip == null || !IpUtil.isIpAddress(ip)) {
			langManager.sendMessage(sender, "ban.invalid-ip", Map.of("%input%", input));
			return null;
		}

		return ip;
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