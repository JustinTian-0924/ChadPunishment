package basementhost.randomchad.mutemodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.util.DurationUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MuteCommandListener implements Listener {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final MuteManager muteManager;

	public MuteCommandListener(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			MuteManager muteManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.muteManager = muteManager;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		if (!moduleManager.isMuteEnabled()) {
			return;
		}

		if (!plugin.getConfig().getBoolean(
				"mute.command-blacklist.enabled",
				true
		)) {
			return;
		}

		String commandLabel = extractCommandLabel(event.getMessage());

		if (commandLabel.isEmpty()) {
			return;
		}

		if (!isBlockedCommand(commandLabel)) {
			return;
		}

		MuteRecord record = muteManager.getActiveMute(
				event.getPlayer().getUniqueId()
		);

		if (record == null) {
			return;
		}

		event.setCancelled(true);

		long remainingMillis = record.isPermanent()
				? -1L
				: Math.max(
				0L,
				record.getExpiresAt() - System.currentTimeMillis()
		);

		langManager.sendMessageList(
				event.getPlayer(),
				"mute.command-blocked",
				Map.of(
						"%command%", commandLabel,
						"%reason%", record.getReason(),
						"%remaining%", DurationUtil.formatDuration(
								langManager,
								remainingMillis
						)
				)
		);
	}


	private String extractCommandLabel(String commandLine) {
		if (commandLine == null) {
			return "";
		}

		String normalized = commandLine.trim();

		if (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}

		if (normalized.isBlank()) {
			return "";
		}

		int firstWhitespace = findFirstWhitespace(normalized);

		if (firstWhitespace >= 0) {
			normalized = normalized.substring(0, firstWhitespace);
		}

		return normalized.toLowerCase(Locale.ROOT);
	}

	// Judge if the command is blocked
	private boolean isBlockedCommand(String executedCommand) {
		List<String> configuredCommands = plugin.getConfig().getStringList(
				"mute.command-blacklist.commands"
		);

		if (configuredCommands.isEmpty()) {
			return false;
		}

		String executedBaseCommand = removeNamespace(executedCommand);

		for (String configuredCommand : configuredCommands) {
			String blacklistEntry = normalizeConfiguredCommand(
					configuredCommand
			);

			if (blacklistEntry.isEmpty()) {
				continue;
			}

			if (executedCommand.equals(blacklistEntry)) {
				return true;
			}

			if (!blacklistEntry.contains(":")
					&& executedBaseCommand.equals(blacklistEntry)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * msg
	 * /msg
	 * MSG
	 * /msg Steve hello
	 */
	private String normalizeConfiguredCommand(String configuredCommand) {
		if (configuredCommand == null) {
			return "";
		}

		String normalized = configuredCommand.trim();

		if (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}

		if (normalized.isBlank()) {
			return "";
		}

		int firstWhitespace = findFirstWhitespace(normalized);

		if (firstWhitespace >= 0) {
			normalized = normalized.substring(0, firstWhitespace);
		}

		return normalized.toLowerCase(Locale.ROOT);
	}

	/**
	 * Remove namespaces (preventing bypassing)
	 * essentials:msg -> msg
	 * minecraft:tell -> tell
	 * msg            -> msg
	 */
	private String removeNamespace(String commandLabel) {
		int namespaceSeparator = commandLabel.lastIndexOf(':');

		if (namespaceSeparator < 0
				|| namespaceSeparator == commandLabel.length() - 1) {
			return commandLabel;
		}

		return commandLabel.substring(namespaceSeparator + 1);
	}

	private int findFirstWhitespace(String text) {
		for (int index = 0; index < text.length(); index++) {
			if (Character.isWhitespace(text.charAt(index))) {
				return index;
			}
		}

		return -1;
	}
}