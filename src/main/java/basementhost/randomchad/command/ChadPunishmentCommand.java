package basementhost.randomchad.command;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.MuteManager;
import basementhost.randomchad.warnmodule.WarnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class ChadPunishmentCommand implements TabExecutor {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final WarnManager warnManager;
	private final MuteManager muteManager;


	public ChadPunishmentCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			WarnManager warnManager,
			MuteManager muteManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.warnManager = warnManager;
		this.muteManager = muteManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			langManager.sendMessageList(sender, "usage.main");
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("chadpunishment.reload")) {
				langManager.sendMessage(sender, "command.no-permission");
				return true;
			}

			plugin.reloadConfig();
			langManager.reload();
			warnManager.reload();
			muteManager.reload();
			langManager.sendMessage(sender, "command.reload-success");
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
			if (!sender.hasPermission("chadpunishment.status")) {
				langManager.sendMessage(sender, "command.no-permission");
				return true;
			}

			sendModuleStatus(sender);
			return true;
		}

		langManager.sendMessage(sender, "command.unknown-command");
		langManager.sendMessageList(sender, "usage.main");
		return true;
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
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			return List.of("reload", "status");
		}

		return List.of();
	}
}