package basementhost.randomchad.mutemodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.util.DurationUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class MuteChatListener implements Listener {

	private final LangManager langManager;
	private final ModuleManager moduleManager;
	private final MuteManager muteManager;

	public MuteChatListener(
			LangManager langManager,
			ModuleManager moduleManager,
			MuteManager muteManager
	) {
		this.langManager = langManager;
		this.moduleManager = moduleManager;
		this.muteManager = muteManager;
	}

	@EventHandler
	public void onAsyncChat(AsyncChatEvent event) {
		if (!moduleManager.isMuteEnabled()) {
			return;
		}

		MuteRecord record = muteManager.getActiveMute(event.getPlayer().getUniqueId());

		if (record == null) {
			return;
		}

		event.setCancelled(true);

		long remainingMillis = record.isPermanent()
				? -1L
				: record.getExpiresAt() - System.currentTimeMillis();

		langManager.sendMessageList(event.getPlayer(), "mute.chat-blocked", Map.of(
				"%reason%", record.getReason(),
				"%remaining%", DurationUtil.formatDuration(langManager, remainingMillis)
		));
	}
}