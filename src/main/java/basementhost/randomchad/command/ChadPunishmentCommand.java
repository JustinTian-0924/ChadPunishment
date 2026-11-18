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
import net.kyori.adventure.text.Component;
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
	private final PunishmentQueryCommand queryCommand;
	private final HelpCommand helpCommand;


	public ChadPunishmentCommand(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			WarnManager warnManager,
			MuteManager muteManager,
			BanManager banManager,
			IpBanManager ipBanManager,
			PunishmentHistoryManager historyManager,
			PunishmentQueryCommand queryCommand
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.warnManager = warnManager;
		this.muteManager = muteManager;
		this.banManager = banManager;
		this.ipBanManager = ipBanManager;
		this.historyManager =historyManager;
		this.queryCommand = queryCommand;
		this.helpCommand = new HelpCommand(langManager);
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (args.length == 0) {
			helpCommand.show(sender, 1);
			return;
		}

		if (args[0].equalsIgnoreCase("help")) {
			try {
				if (args.length > 2) {
					throw new NumberFormatException();
				}
				helpCommand.show(sender, args.length == 2 ? Integer.parseInt(args[1]) : 1);
			} catch (NumberFormatException exception) {
				langManager.sendMessage(sender, "help.invalid-page");
			}
			return;
		}

		if (isQuery(args[0])) {
			queryCommand.execute(source, args);
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
		helpCommand.show(sender, 1);
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
		Component status = enabled
				? langManager.getRawComponent("module.enabled")
				: langManager.getRawComponent("module.disabled");

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
			String prefix = args[0].toLowerCase(java.util.Locale.ROOT);
			return List.of("help", "reload", "status", "query", "check", "browse").stream()
					.filter(value -> value.startsWith(prefix)).toList();
		}
		if (args.length >= 2 && isQuery(args[0])) {
			return queryCommand.suggest(source, args);
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("help")) {
			return List.of("1", "2").stream().filter(value -> value.startsWith(args[1])).toList();
		}

		return List.of();
	}

	private static boolean isQuery(String value) {
		return value.equalsIgnoreCase("query") || value.equalsIgnoreCase("check") || value.equalsIgnoreCase("browse");
	}
}
