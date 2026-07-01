package basementhost.randomchad.manager;

import org.bukkit.plugin.java.JavaPlugin;

// Manage enable & disable modules, with default setting.
public class ModuleManager {

	private final JavaPlugin plugin;

	public ModuleManager(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean isWarnEnabled() {
		return plugin.getConfig().getBoolean("modules.warn", true);
	}

	public boolean isMuteEnabled() {
		return plugin.getConfig().getBoolean("modules.mute", true);
	}

	public boolean isBanEnabled() {
		return plugin.getConfig().getBoolean("modules.ban", true);
	}

	public boolean isBanIpEnabled() {
		return plugin.getConfig().getBoolean("modules.ban-ip", true);
	}
}