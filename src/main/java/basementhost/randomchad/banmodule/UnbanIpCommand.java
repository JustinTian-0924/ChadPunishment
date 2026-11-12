package basementhost.randomchad.banmodule;

import basementhost.randomchad.history.PunishmentHistoryManager;
import basementhost.randomchad.history.PunishmentType;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
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

public class UnbanIpCommand implements BasicCommand {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final IpBanManager ipBanManager;
	private final PunishmentHistoryManager historyManager;

	public UnbanIpCommand(
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

		if (!sender.hasPermission("chadpunishment.unbanip")) {
			langManager.sendMessage(sender, "ban.no-permission");
			return;
		}

		if (args.length < 1) {
			langManager.sendMessage(sender, "ban.unbanip-usage");
			return;
		}

		String input = args[0];
		String ip = resolveIpFromInput(input, sender);

		if (ip == null) {
			return;
		}

		String reason = plugin.getConfig().getString("settings.default-reason", "No reason provided");

		if (args.length >= 2) {
			reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		}

		boolean removed = ipBanManager.unbanIp(ip);

		if (!removed) {
			langManager.sendMessage(sender, "ban.ip-not-banned", Map.of("%ip%", ip));
			return;
		}

		Player targetPlayer = getOnlinePlayerFromInput(input);

		if (targetPlayer != null) {
			historyManager.addRecord(
					targetPlayer,
					sender,
					PunishmentType.UNBANIP,
					reason,
					0L,
					"IP: " + ip
			);
		}

		langManager.sendMessage(sender, "ban.unbanip-success", Map.of(
				"%ip%", ip,
				"%reason%", reason
		));

		return;
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

		return List.of();
	}
}