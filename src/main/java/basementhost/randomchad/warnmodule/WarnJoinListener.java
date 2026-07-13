package basementhost.randomchad.warnmodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.util.DurationUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class WarnJoinListener implements Listener {
    private final ModuleManager moduleManager;
    private final WarnManager warnManager;
    private final LangManager langManager;

    public WarnJoinListener(
            ModuleManager moduleManager,
            WarnManager warnManager,
            LangManager langManager
    ) {
        this.moduleManager = moduleManager;
        this.warnManager = warnManager;
        this.langManager = langManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!moduleManager.isWarnEnabled()) return;
        if (!warnManager.shouldShowOnJoin()) return;

        UUID uuid = event.getPlayer().getUniqueId();
        Collection<WarnRecord> records = warnManager.getActiveWarns(uuid);

        for (WarnRecord record : records) {
            long remainingMillis = record.getExpiresAt() - System.currentTimeMillis();
            String formattedDuration = DurationUtil.formatDuration(langManager, remainingMillis);
            langManager.sendMessage(event.getPlayer(), "warn.received", Map.of(
                    "%reason%", record.getReason(),
                    "%duration%", formattedDuration
            ));
        }
    }
}
