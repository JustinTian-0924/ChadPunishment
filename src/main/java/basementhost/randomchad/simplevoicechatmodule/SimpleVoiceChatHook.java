package basementhost.randomchad.simplevoicechatmodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.MuteManager;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleVoiceChatHook {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final MuteManager muteManager;

	public SimpleVoiceChatHook(
			JavaPlugin plugin,
			LangManager langManager,
			ModuleManager moduleManager,
			MuteManager muteManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
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

		RegisteredServiceProvider<BukkitVoicechatService> serviceProvider =
				Bukkit.getServicesManager().getRegistration(BukkitVoicechatService.class);

		if (serviceProvider == null) {
			plugin.getLogger().warning("Simple Voice Chat plugin found, but BukkitVoicechatService is not available.");
			return;
		}

		BukkitVoicechatService service = serviceProvider.getProvider();

		service.registerPlugin(new ChadVoicechatPlugin(
				plugin,
				moduleManager,
				muteManager
		));

		plugin.getLogger().info("Simple Voice Chat compatibility enabled.");
	}
}