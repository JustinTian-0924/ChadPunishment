package basementhost.randomchad;

import basementhost.randomchad.command.ChadPunishmentCommand;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.MuteChatListener;
import basementhost.randomchad.mutemodule.MuteCommand;
import basementhost.randomchad.mutemodule.MuteManager;
import basementhost.randomchad.mutemodule.PermaMuteCommand;
import basementhost.randomchad.mutemodule.UnmuteCommand;
import basementhost.randomchad.warnmodule.CheckWarnCommand;
import basementhost.randomchad.warnmodule.WarnCommand;
import basementhost.randomchad.warnmodule.WarnManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChadPunishmentPlugin extends JavaPlugin {

	private LangManager langManager;
	private ModuleManager moduleManager;
	private WarnManager warnManager;
	private MuteManager muteManager;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		this.langManager = new LangManager(this);
		this.moduleManager = new ModuleManager(this);
		this.warnManager = new WarnManager(this, langManager);
		this.muteManager = new MuteManager(this);

		registerCommands();
		registerListeners();

		getLogger().info("ChadPunishment plugin is enabled");
	}

	@Override
	public void onDisable() {
		getLogger().info("ChadPunishment plugin is disabled");
	}

	private void registerCommands() {
		ChadPunishmentCommand command = new ChadPunishmentCommand(
				this,
				langManager,
				moduleManager,
				warnManager,
				muteManager
		);

		if (getCommand("chadpunishment") != null) {
			getCommand("chadpunishment").setExecutor(command);
			getCommand("chadpunishment").setTabCompleter(command);
		}

		WarnCommand warnCommand = new WarnCommand(
				this,
				langManager,
				moduleManager,
				warnManager
		);

		if (getCommand("warn") != null) {
			getCommand("warn").setExecutor(warnCommand);
			getCommand("warn").setTabCompleter(warnCommand);
		}

		CheckWarnCommand checkWarnCommand = new CheckWarnCommand(
				langManager,
				moduleManager,
				warnManager
		);

		if (getCommand("checkwarn") != null) {
			getCommand("checkwarn").setExecutor(checkWarnCommand);
			getCommand("checkwarn").setTabCompleter(checkWarnCommand);
		}

		MuteCommand muteCommand = new MuteCommand(
				this,
				langManager,
				moduleManager,
				muteManager
		);

		if (getCommand("mute") != null) {
			getCommand("mute").setExecutor(muteCommand);
			getCommand("mute").setTabCompleter(muteCommand);
		}

		PermaMuteCommand permaMuteCommand = new PermaMuteCommand(
				this,
				langManager,
				moduleManager,
				muteManager
		);

		if (getCommand("permamute") != null) {
			getCommand("permamute").setExecutor(permaMuteCommand);
			getCommand("permamute").setTabCompleter(permaMuteCommand);
		}

		UnmuteCommand unmuteCommand = new UnmuteCommand(
				this,
				langManager,
				moduleManager,
				muteManager
		);

		if (getCommand("unmute") != null) {
			getCommand("unmute").setExecutor(unmuteCommand);
			getCommand("unmute").setTabCompleter(unmuteCommand);
		}
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new MuteChatListener(langManager, moduleManager, muteManager),
				this
		);
	}

	public LangManager getLangManager() {
		return langManager;
	}

	public ModuleManager getModuleManager() {
		return moduleManager;
	}

	public WarnManager getWarnManager() {
		return warnManager;
	}

	public MuteManager getMuteManager() {
		return muteManager;
	}
}