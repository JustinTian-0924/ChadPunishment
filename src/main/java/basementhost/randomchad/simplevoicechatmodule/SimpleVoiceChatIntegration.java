package basementhost.randomchad.simplevoicechatmodule;

import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.MuteManager;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import org.bukkit.plugin.java.JavaPlugin;

// Only SimpleVoiceChatHook loads this class, after checking the optional dependency.
public final class SimpleVoiceChatIntegration {

	private SimpleVoiceChatIntegration() {
	}

	public static void register(JavaPlugin plugin, ModuleManager moduleManager, MuteManager muteManager) {
		BukkitVoicechatService service = plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);
		if (service == null) {
			plugin.getLogger().warning("Simple Voice Chat plugin found, but BukkitVoicechatService is not available.");
			return;
		}

		service.registerPlugin(new ChadVoicechatPlugin(plugin, moduleManager, muteManager));
		plugin.getLogger().info("Simple Voice Chat compatibility enabled.");
	}
}
