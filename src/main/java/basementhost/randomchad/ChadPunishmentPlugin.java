package basementhost.randomchad;

import basementhost.randomchad.banmodule.*;
import basementhost.randomchad.command.ChadPunishmentCommand;
import basementhost.randomchad.history.PunishHistoryCommand;
import basementhost.randomchad.history.PunishmentHistoryManager;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.MuteChatListener;
import basementhost.randomchad.mutemodule.MuteCommand;
import basementhost.randomchad.mutemodule.MuteManager;
import basementhost.randomchad.mutemodule.PermaMuteCommand;
import basementhost.randomchad.mutemodule.UnmuteCommand;
import basementhost.randomchad.simplevoicechatmodule.SimpleVoiceChatHook;
import basementhost.randomchad.warnmodule.CheckWarnCommand;
import basementhost.randomchad.warnmodule.WarnCommand;
import basementhost.randomchad.warnmodule.WarnManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChadPunishmentPlugin extends JavaPlugin {

	private LangManager langManager;
	private ModuleManager moduleManager;
	private WarnManager warnManager;
	private MuteManager muteManager;
	private BanManager banManager;
	private IpBanManager ipBanManager;
	private PunishmentHistoryManager historyManager;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		this.langManager = new LangManager(this);
		this.moduleManager = new ModuleManager(this);
		this.historyManager = new PunishmentHistoryManager(this);
		this.warnManager = new WarnManager(this, langManager);
		this.muteManager = new MuteManager(this);
		this.banManager = new BanManager(this, langManager);
		this.ipBanManager = new IpBanManager(this, langManager);

		registerCommands();
		registerListeners();
		hookSimpleVoiceChat();
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
				muteManager,
				banManager,
				ipBanManager,
				historyManager
		);

		if (getCommand("chadpunishment") != null) {
			getCommand("chadpunishment").setExecutor(command);
			getCommand("chadpunishment").setTabCompleter(command);
		}

		WarnCommand warnCommand = new WarnCommand(
				this,
				langManager,
				moduleManager,
				warnManager,
				historyManager
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
				muteManager,
				historyManager
		);

		if (getCommand("mute") != null) {
			getCommand("mute").setExecutor(muteCommand);
			getCommand("mute").setTabCompleter(muteCommand);
		}

		PermaMuteCommand permaMuteCommand = new PermaMuteCommand(
				this,
				langManager,
				moduleManager,
				muteManager,
				historyManager
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

		TempBanCommand tempBanCommand = new TempBanCommand(
				this,
				langManager,
				moduleManager,
				banManager,
				historyManager
		);

		if (getCommand("tempban") != null) {
			getCommand("tempban").setExecutor(tempBanCommand);
			getCommand("tempban").setTabCompleter(tempBanCommand);
		}

		BanCommand banCommand = new BanCommand(
				this,
				langManager,
				moduleManager,
				banManager,
				historyManager
		);

		if (getCommand("ban") != null) {
			getCommand("ban").setExecutor(banCommand);
			getCommand("ban").setTabCompleter(banCommand);
		}

		UnbanCommand unbanCommand = new UnbanCommand(
				this,
				langManager,
				moduleManager,
				banManager,
				historyManager
		);

		if (getCommand("unban") != null) {
			getCommand("unban").setExecutor(unbanCommand);
			getCommand("unban").setTabCompleter(unbanCommand);
		}

		BanIpCommand banIpCommand = new BanIpCommand(
				this,
				langManager,
				moduleManager,
				ipBanManager,
				historyManager
		);

		if (getCommand("banip") != null) {
			getCommand("banip").setExecutor(banIpCommand);
			getCommand("banip").setTabCompleter(banIpCommand);
		}

		UnbanIpCommand unbanIpCommand = new UnbanIpCommand(
				this,
				langManager,
				moduleManager,
				ipBanManager,
				historyManager
		);

		if (getCommand("unbanip") != null) {
			getCommand("unbanip").setExecutor(unbanIpCommand);
			getCommand("unbanip").setTabCompleter(unbanIpCommand);
		}

		PunishHistoryCommand punishHistoryCommand = new PunishHistoryCommand(
				langManager,
				historyManager
		);
		if (getCommand("punishhistory") != null) {
			getCommand("punishhistory").setExecutor(punishHistoryCommand);
			getCommand("punishhistory").setTabCompleter(punishHistoryCommand);
		}
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new MuteChatListener(langManager, moduleManager, muteManager),
				this
		);

		getServer().getPluginManager().registerEvents(
				new BanLoginListener(moduleManager, banManager),
				this
		);

		getServer().getPluginManager().registerEvents(
				new IpBanLoginListener(moduleManager, ipBanManager),
				this
		);
	}

	private void hookSimpleVoiceChat() {
		SimpleVoiceChatHook hook = new SimpleVoiceChatHook(
				this,
				langManager,
				moduleManager,
				muteManager
		);

		hook.hook();
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

	public BanManager getBanManager() {
		return banManager;
	}

	public IpBanManager getIpBanManager() {
		return ipBanManager;
	}

	public PunishmentHistoryManager getHistoryManager() {
		return historyManager;
	}
}