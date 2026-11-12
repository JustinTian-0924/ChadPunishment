package basementhost.randomchad;

import basementhost.randomchad.banmodule.*;
import basementhost.randomchad.command.ChadPunishmentCommand;
import basementhost.randomchad.history.PunishHistoryCommand;
import basementhost.randomchad.history.PunishmentHistoryManager;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.*;
import basementhost.randomchad.simplevoicechatmodule.SimpleVoiceChatHook;
import basementhost.randomchad.warnmodule.CheckWarnCommand;
import basementhost.randomchad.warnmodule.WarnCommand;
import basementhost.randomchad.warnmodule.WarnJoinListener;
import basementhost.randomchad.warnmodule.WarnManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
		// Register again whenever Paper rebuilds the command list.
		getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
			Commands commands = event.registrar();

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

			commands.register("chadpunishment", "Main command for ChadPunishment", List.of("cpunish", "chadpunish", "punishment"), command);

			WarnCommand warnCommand = new WarnCommand(
					this,
					langManager,
					moduleManager,
					warnManager,
					historyManager
			);

			commands.register("warn", "Warn a player", List.of(), warnCommand);

			CheckWarnCommand checkWarnCommand = new CheckWarnCommand(
					langManager,
					moduleManager,
					warnManager
			);

			commands.register("checkwarn", "Check active warnings of a player", List.of(), checkWarnCommand);

			MuteCommand muteCommand = new MuteCommand(
					this,
					langManager,
					moduleManager,
					muteManager,
					historyManager
			);

			commands.register("mute", "Mute a player", List.of(), muteCommand);

			PermaMuteCommand permaMuteCommand = new PermaMuteCommand(
					this,
					langManager,
					moduleManager,
					muteManager,
					historyManager
			);

			commands.register("permamute", "Permanently mute a player", List.of(), permaMuteCommand);

			UnmuteCommand unmuteCommand = new UnmuteCommand(
					this,
					langManager,
					moduleManager,
					muteManager
			);

			commands.register("unmute", "Unmute a player", List.of(), unmuteCommand);

			TempBanCommand tempBanCommand = new TempBanCommand(
					this,
					langManager,
					moduleManager,
					banManager,
					historyManager
			);

			commands.register("tempban", "Temporarily ban a player", List.of(), tempBanCommand);

			BanCommand banCommand = new BanCommand(
					this,
					langManager,
					moduleManager,
					banManager,
					historyManager
			);

			commands.register("ban", "Ban a player", List.of(), banCommand);

			UnbanCommand unbanCommand = new UnbanCommand(
					this,
					langManager,
					moduleManager,
					banManager,
					historyManager
			);

			commands.register("unban", "Unban a player", List.of(), unbanCommand);

			BanIpCommand banIpCommand = new BanIpCommand(
					this,
					langManager,
					moduleManager,
					ipBanManager,
					historyManager
			);

			commands.register("banip", "IP-ban a player or IP address", List.of(), banIpCommand);

			UnbanIpCommand unbanIpCommand = new UnbanIpCommand(
					this,
					langManager,
					moduleManager,
					ipBanManager,
					historyManager
			);

			commands.register("unbanip", "Unban an IP address", List.of(), unbanIpCommand);

			PunishHistoryCommand punishHistoryCommand = new PunishHistoryCommand(
					langManager,
					historyManager
			);
			commands.register("punishhistory", "View punishment history of a player", List.of("phistory", "punishhist"), punishHistoryCommand);
		});
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new MuteChatListener(
						langManager,
						moduleManager,
						muteManager
				),
				this
		);

		getServer().getPluginManager().registerEvents(
				new MuteCommandListener(
						this,
						langManager,
						moduleManager,
						muteManager
				),
				this
		);

		getServer().getPluginManager().registerEvents(
				new BanLoginListener(
						moduleManager,
						banManager
				),
				this
		);

		getServer().getPluginManager().registerEvents(
				new IpBanLoginListener(
						moduleManager,
						ipBanManager
				),
				this
		);

		getServer().getPluginManager().registerEvents(
				new WarnJoinListener(
						moduleManager,
						warnManager,
						langManager
				),
				this
		);
	}

	private void hookSimpleVoiceChat() {
		SimpleVoiceChatHook hook = new SimpleVoiceChatHook(
				this,
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
