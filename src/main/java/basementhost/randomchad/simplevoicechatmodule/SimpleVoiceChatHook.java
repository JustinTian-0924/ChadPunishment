package basementhost.randomchad.simplevoicechatmodule;

import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.MuteManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

// This entry point must NOT refer to any Simple Voice Chat API types.
// I'm too lazy to i18n debug console messages lol
public class SimpleVoiceChatHook {

	private final JavaPlugin plugin;
	private final ModuleManager moduleManager;
	private final MuteManager muteManager;

	public SimpleVoiceChatHook(
			JavaPlugin plugin,
			ModuleManager moduleManager,
			MuteManager muteManager
	) {
		this.plugin = plugin;
		this.moduleManager = moduleManager;
		this.muteManager = muteManager;
	}

	public void hook() {
		if (!plugin.getConfig().getBoolean("compatibility.simple-voice-chat.enabled", true)) {
			plugin.getLogger().info("Simple Voice Chat compatibility is disabled in config.");
			return;
		}

		if (!Bukkit.getPluginManager().isPluginEnabled("voicechat")) {
			plugin.getLogger().info("Simple Voice Chat not found. Voice compatibility disabled.");
			return;
		}

		try {
			// Load the API-dependent class only after the optional plugin is enabled.
			Class<?> integration = Class.forName(
					"basementhost.randomchad.simplevoicechatmodule.SimpleVoiceChatIntegration",
					true,
					plugin.getClass().getClassLoader()
			);
			integration.getMethod("register", JavaPlugin.class, ModuleManager.class, MuteManager.class)
					.invoke(null, plugin, moduleManager, muteManager);
		} catch (InvocationTargetException exception) {
			logFailure(exception.getCause());
		} catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
			logFailure(exception);
		}
	}

	private void logFailure(Throwable cause) {
		plugin.getLogger().log(Level.WARNING,
				"Could not enable Simple Voice Chat compatibility. Other punishment features remain enabled.", cause);
	}
}
