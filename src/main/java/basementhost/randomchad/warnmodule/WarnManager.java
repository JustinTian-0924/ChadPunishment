package basementhost.randomchad.warnmodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.util.DurationUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// save, read, clear outdated, calculate valid warns. And execute commands when reach x warns
public class WarnManager {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private File warnsFile;
	private FileConfiguration warnsData;

	public WarnManager(JavaPlugin plugin, LangManager langManager) {
		this.plugin = plugin;
		this.langManager = langManager;
		loadWarnsData();
		startCleanupTask();
	}

	public void loadWarnsData() {
		File dataFolder = new File(plugin.getDataFolder(), "data");

		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		warnsFile = new File(dataFolder, "warns.yml");

		if (!warnsFile.exists()) {
			try {
				warnsFile.createNewFile();
			} catch (IOException exception) {
				plugin.getLogger().severe("Could not create warns.yml");
				exception.printStackTrace();
			}
		}

		warnsData = YamlConfiguration.loadConfiguration(warnsFile);
	}

	public void reload() {
		loadWarnsData();
	}

	public WarnRecord addWarn(
			OfflinePlayer target,
			CommandSender issuer,
			String reason,
			long ttlMillis
	) {
		long now = System.currentTimeMillis();
		long expiresAt = now + ttlMillis;

		String id = String.valueOf(now);

		WarnRecord record = new WarnRecord(
				id,
				target.getUniqueId(),
				target.getName() == null ? target.getUniqueId().toString() : target.getName(),
				issuer.getName(),
				reason,
				now,
				expiresAt
		);

		String path = "players." + target.getUniqueId() + ".warns." + id;

		warnsData.set(path + ".target-name", record.getTargetName());
		warnsData.set(path + ".issuer", record.getIssuerName());
		warnsData.set(path + ".reason", record.getReason());
		warnsData.set(path + ".issued-at", record.getIssuedAt());
		warnsData.set(path + ".expires-at", record.getExpiresAt());

		saveWarnsData();

		return record;
	}

	public List<WarnRecord> getActiveWarns(UUID targetUuid) {
		cleanupExpiredWarns(targetUuid);

		List<WarnRecord> records = new ArrayList<>();
		ConfigurationSection section = warnsData.getConfigurationSection("players." + targetUuid + ".warns");

		if (section == null) {
			return records;
		}

		long now = System.currentTimeMillis();

		for (String id : section.getKeys(false)) {
			String path = "players." + targetUuid + ".warns." + id;

			String targetName = warnsData.getString(path + ".target-name", targetUuid.toString());
			String issuer = warnsData.getString(path + ".issuer", "Console");
			String reason = warnsData.getString(path + ".reason", "No reason provided");
			long issuedAt = warnsData.getLong(path + ".issued-at");
			long expiresAt = warnsData.getLong(path + ".expires-at");

			WarnRecord record = new WarnRecord(
					id,
					targetUuid,
					targetName,
					issuer,
					reason,
					issuedAt,
					expiresAt
			);

			if (!record.isExpired(now)) {
				records.add(record);
			}
		}

		return records;
	}

	public int getActiveWarnCount(UUID targetUuid) {
		return getActiveWarns(targetUuid).size();
	}

	public void runActiveWarnActions(OfflinePlayer target, String reason, CommandSender issuer, int activeWarnCount, long ttlMillis) {
		List<String> commands = plugin.getConfig().getStringList("warn.active-count-actions." + activeWarnCount);

		if (commands.isEmpty()) {
			return;
		}

		String playerName = target.getName() == null ? target.getUniqueId().toString() : target.getName();
		String duration = DurationUtil.formatDuration(langManager, ttlMillis);

		for (String command : commands) {
			command = command
					.replace("%player%", playerName)
					.replace("%uuid%", target.getUniqueId().toString())
					.replace("%reason%", reason)
					.replace("%issuer%", issuer.getName())
					.replace("%active_warns%", String.valueOf(activeWarnCount))
					.replace("%duration%", duration);

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		}
	}

	public void cleanupExpiredWarns(UUID targetUuid) {
		ConfigurationSection section = warnsData.getConfigurationSection("players." + targetUuid + ".warns");

		if (section == null) {
			return;
		}

		long now = System.currentTimeMillis();
		boolean changed = false;

		for (String id : section.getKeys(false)) {
			String path = "players." + targetUuid + ".warns." + id;
			long expiresAt = warnsData.getLong(path + ".expires-at");

			if (expiresAt > 0 && expiresAt <= now) {
				warnsData.set(path, null);
				changed = true;
			}
		}

		if (changed) {
			saveWarnsData();
		}
	}

	public void cleanupAllExpiredWarns() {
		ConfigurationSection playersSection = warnsData.getConfigurationSection("players");

		if (playersSection == null) {
			return;
		}

		for (String uuidText : playersSection.getKeys(false)) {
			try {
				cleanupExpiredWarns(UUID.fromString(uuidText));
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	private void startCleanupTask() {
		Bukkit.getScheduler().runTaskTimer(
				plugin,
				this::cleanupAllExpiredWarns,
				20L * 60L,
				20L * 60L
		);
	}

	private void saveWarnsData() {
		try {
			warnsData.save(warnsFile);
		} catch (IOException exception) {
			plugin.getLogger().severe("Could not save warns.yml");
			exception.printStackTrace();
		}
	}
}