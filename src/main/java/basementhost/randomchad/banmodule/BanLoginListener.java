package basementhost.randomchad.banmodule;

import basementhost.randomchad.manager.ModuleManager;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerLoginConnection;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
	public void onPlayerLogin(PlayerConnectionValidateLoginEvent event) {
		if (!moduleManager.isBanEnabled() || !event.isAllowed()) {
			return;
		}

		// Paper validates both the login and configuration stages, before a Player exists.
		PlayerProfile profile = switch (event.getConnection()) {
			case PlayerLoginConnection connection -> connection.getAuthenticatedProfile();
			case PlayerConfigurationConnection connection -> connection.getProfile();
			default -> null;
		};

		if (profile == null || profile.getId() == null) {
			return;
		}

		BanRecord record = banManager.getActiveBan(profile.getId());

		if (record == null) {
			return;
		}

		event.kickMessage(banManager.buildKickMessage(record));
	}
}
