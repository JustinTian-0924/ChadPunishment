package basementhost.randomchad.banmodule;

import basementhost.randomchad.manager.ModuleManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class IpBanLoginListener implements Listener {

	private final ModuleManager moduleManager;
	private final IpBanManager ipBanManager;

	public IpBanLoginListener(
			ModuleManager moduleManager,
			IpBanManager ipBanManager
	) {
		this.moduleManager = moduleManager;
		this.ipBanManager = ipBanManager;
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!moduleManager.isBanIpEnabled()) {
			return;
		}

		String ip = event.getAddress().getHostAddress();
		IpBanRecord record = ipBanManager.getActiveIpBan(ip);

		if (record == null) {
			return;
		}

		event.disallow(
				PlayerLoginEvent.Result.KICK_BANNED,
				ipBanManager.buildKickMessage(record)
		);
	}
}