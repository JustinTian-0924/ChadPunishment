package basementhost.randomchad.simplevoicechatmodule;

import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.MuteManager;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ChadVoicechatPlugin implements VoicechatPlugin {

	private final JavaPlugin plugin;
	private final ModuleManager moduleManager;
	private final MuteManager muteManager;

	public ChadVoicechatPlugin(
			JavaPlugin plugin,
			ModuleManager moduleManager,
			MuteManager muteManager
	) {
		this.plugin = plugin;
		this.moduleManager = moduleManager;
		this.muteManager = muteManager;
	}

	@Override
	public String getPluginId() {
		return "chadpunishment";
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
	}

	private void onMicrophonePacket(MicrophonePacketEvent event) {
		if (!plugin.getConfig().getBoolean("compatibility.simple-voice-chat.enabled", true)) {
			return;
		}

		if (!plugin.getConfig().getBoolean("compatibility.simple-voice-chat.block-muted-players", true)) {
			return;
		}

		if (!moduleManager.isMuteEnabled()) {
			return;
		}

		if (event.getSenderConnection() == null) {
			return;
		}

		if (event.getSenderConnection().getPlayer() == null) {
			return;
		}

		UUID senderUuid = event.getSenderConnection().getPlayer().getUuid();

		if (muteManager.isMuted(senderUuid)) {
			event.cancel();
		}
	}
}