package basementhost.randomchad.command;

import basementhost.randomchad.banmodule.BanManager;
import basementhost.randomchad.banmodule.IpBanManager;
import basementhost.randomchad.history.PunishmentHistoryManager;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.MuteManager;
import basementhost.randomchad.warnmodule.WarnManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class ChadPunishmentCommand implements BasicCommand {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final WarnManager warnManager;
	private final MuteManager muteManager;
	private final BanManager banManager;
	private final IpBanManager ipBanManager;
	private final PunishmentHistoryManager historyManager;


	public ChadPunishmentCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			WarnManager warnManager,
			MuteManager muteManager,
			BanManager banManager,
			IpBanManager ipBanManager,
			PunishmentHistoryManager historyManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.warnManager = warnManager;
		this.muteManager = muteManager;
		this.banManager = banManager;
		this.ipBanManager = ipBanManager;
		this.historyManager =historyManager;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (args.length == 0) {
			langManager.sendMessageList(sender, "usage.main");
			return;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("chadpunishment.reload")) {
				langManager.sendMessage(sender, "command.no-permission");
				return;
			}

			plugin.reloadConfig();
			langManager.reload();
			warnManager.reload();
			muteManager.reload();
			banManager.reload();
			ipBanManager.reload();
			historyManager.reload();
			langManager.sendMessage(sender, "command.reload-success");
			return;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
			if (!sender.hasPermission("chadpunishment.status")) {
				langManager.sendMessage(sender, "command.no-permission");
				return;
			}

			sendModuleStatus(sender);
			return;
		}

		langManager.sendMessage(sender, "command.unknown-command");
		langManager.sendMessageList(sender, "usage.main");
		return;
	}

	private void sendModuleStatus(CommandSender sender) {
		langManager.sendRawMessage(sender, "module.status-header");

		sendModuleLine(sender, "warn", moduleManager.isWarnEnabled());
		sendModuleLine(sender, "mute", moduleManager.isMuteEnabled());
		sendModuleLine(sender, "ban", moduleManager.isBanEnabled());
		sendModuleLine(sender, "ban-ip", moduleManager.isBanIpEnabled());
	}

	private void sendModuleLine(CommandSender sender, String moduleName, boolean enabled) {
		String status = enabled
				? langManager.getRawMessage("module.enabled")
				: langManager.getRawMessage("module.disabled");

		langManager.sendRawMessage(
				sender,
				"module.status-line",
				Map.of(
						"%module%", moduleName,
						"%status%", status
				)
		);
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		if (args.length == 1) {
			return List.of("reload", "status");
		}

		return List.of();
	}
}