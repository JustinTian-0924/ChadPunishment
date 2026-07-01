package basementhost.randomchad.banmodule;

import basementhost.randomchad.manager.ModuleManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class BanLoginListener implements Listener {

	private final ModuleManager moduleManager;
	private final BanManager banManager;

	public BanLoginListener(
			ModuleManager moduleManager,
			BanManager banManager
	) {
		this.moduleManager = moduleManager;
		this.banManager = banManager;
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!moduleManager.isBanEnabled()) {
			return;
		}

		BanRecord record = banManager.getActiveBan(event.getPlayer().getUniqueId());

		if (record == null) {
			return;
		}

		event.disallow(
				PlayerLoginEvent.Result.KICK_BANNED,
				banManager.buildKickMessage(record)
		);
	}
}