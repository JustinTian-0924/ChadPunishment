package basementhost.randomchad.banmodule;

import basementhost.randomchad.history.PunishmentHistoryManager;
import basementhost.randomchad.history.PunishmentType;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.util.DurationUtil;
import basementhost.randomchad.util.IpUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BanIpCommand implements BasicCommand {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final IpBanManager ipBanManager;
	private final PunishmentHistoryManager historyManager;

	public BanIpCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			IpBanManager ipBanManager,
			PunishmentHistoryManager historyManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.ipBanManager = ipBanManager;
		this.historyManager = historyManager;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!moduleManager.isBanIpEnabled()) {
			langManager.sendMessage(sender, "ban.module-disabled");
			return;
		}

		if (!sender.hasPermission("chadpunishment.banip")) {
			langManager.sendMessage(sender, "ban.no-permission");
			return;
		}

		if (args.length < 2) {
			langManager.sendMessage(sender, "ban.banip-usage");
			return;
		}

		String durationText = args[1];
		boolean permanent = DurationUtil.isPermanentDuration(durationText);
		long durationMillis = permanent ? -1L : DurationUtil.parseDurationToMillis(durationText);

		if (!permanent && durationMillis <= 0) {
			langManager.sendMessage(sender, "ban.invalid-ban-duration", Map.of("%duration%", durationText));
			langManager.sendMessage(sender, "ban.banip-usage");
			return;
		}

		String input = args[0];
		String ip = resolveIpFromInput(input, sender);
		if (ip == null) {
			return;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");
		if (args.length >= 3) {
			reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		}

		IpBanRecord record = permanent
				? ipBanManager.permanentBanIp(ip, sender.getName(), reason)
				: ipBanManager.tempBanIp(ip, sender.getName(), reason, durationMillis);
		Player targetPlayer = getOnlinePlayerFromInput(input);
		if (targetPlayer != null) {
			historyManager.addRecord(
					targetPlayer,
					sender,
					PunishmentType.BANIP,
					reason,
					durationMillis,
					"IP: " + ip
			);
		}

		langManager.sendMessage(sender, permanent ? "ban.banip-success" : "ban.tempbanip-success", Map.of(
				"%ip%", record.getIp(),
				"%duration%", DurationUtil.formatDurationComponent(langManager, durationMillis),
				"%reason%", reason
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

	private Player getOnlinePlayerFromInput(String input) {
		if (IpUtil.isIpAddress(input)) {
			return null;
		}

		return Bukkit.getPlayerExact(input);
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
			return List.of("30s", "10m", "1h", "1d", "7d", "30d", "permanent");
		}

		return List.of();
	}
}