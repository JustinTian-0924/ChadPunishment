package basementhost.randomchad.banmodule;

import basementhost.randomchad.manager.ModuleManager;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.net.InetAddress;

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
	public void onPlayerLogin(PlayerConnectionValidateLoginEvent event) {
		if (!moduleManager.isBanIpEnabled() || !event.isAllowed()) {
			return;
		}

		// Use the forwarded client IP, so proxy users are checked individually.
		InetAddress address = event.getConnection().getClientAddress().getAddress();
		if (address == null) {
			return;
		}

		String ip = address.getHostAddress();
		IpBanRecord record = ipBanManager.getActiveIpBan(ip);

		if (record == null) {
			return;
		}

		event.kickMessage(ipBanManager.buildKickMessage(record));
	}
}
