package basementhost.randomchad.history;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PunishmentHistoryManager {

	private final JavaPlugin plugin;
	private File historyFile;
	private FileConfiguration historyData;

	public PunishmentHistoryManager(JavaPlugin plugin) {
		this.plugin = plugin;
		loadHistoryData();
	}

	public void loadHistoryData() {
		File dataFolder = new File(plugin.getDataFolder(), "data");

		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		historyFile = new File(dataFolder, "history.yml");

		if (!historyFile.exists()) {
			try {
				historyFile.createNewFile();
			} catch (IOException exception) {
				plugin.getLogger().severe("Could not create history.yml");
				exception.printStackTrace();
			}
		}

		historyData = YamlConfiguration.loadConfiguration(historyFile);
	}

	public void reload() {
		loadHistoryData();
	}

	public void addRecord(
			OfflinePlayer target,
			CommandSender issuer,
			PunishmentType type,
			String reason,
			long durationMillis,
			String extra
	) {
		String targetName = target.getName() == null
				? target.getUniqueId().toString()
				: target.getName();

		addRecord(
				target.getUniqueId(),
				targetName,
				issuer.getName(),
				type,
				reason,
				durationMillis,
				extra
		);
	}

	public void addRecord(
			UUID targetUuid,
			String targetName,
			String issuerName,
			PunishmentType type,
			String reason,
			long durationMillis,
			String extra
	) {
		long now = System.currentTimeMillis();
		String id = now + "-" + type.name().toLowerCase();

		String path = "players." + targetUuid + ".records." + id;

		historyData.set(path + ".target-name", targetName);
		historyData.set(path + ".type", type.name());
		historyData.set(path + ".issuer", issuerName);
		historyData.set(path + ".reason", reason);
		historyData.set(path + ".created-at", now);
		historyData.set(path + ".duration", durationMillis);
		historyData.set(path + ".extra", extra == null ? "" : extra);

		saveHistoryData();
	}

	public List<PunishmentHistoryRecord> getRecords(UUID targetUuid) {
		List<PunishmentHistoryRecord> records = new ArrayList<>();

		ConfigurationSection section = historyData.getConfigurationSection("players." + targetUuid + ".records");

		if (section == null) {
			return records;
		}

		for (String id : section.getKeys(false)) {
			String path = "players." + targetUuid + ".records." + id;

			String targetName = historyData.getString(path + ".target-name", targetUuid.toString());
			String typeText = historyData.getString(path + ".type", "WARN");
			String issuer = historyData.getString(path + ".issuer", "Console");
			String reason = historyData.getString(path + ".reason", "No reason provided");
			long createdAt = historyData.getLong(path + ".created-at");
			long duration = historyData.getLong(path + ".duration");
			String extra = historyData.getString(path + ".extra", "");

			PunishmentType type;

			try {
				type = PunishmentType.valueOf(typeText);
			} catch (IllegalArgumentException exception) {
				type = PunishmentType.WARN;
			}

			records.add(new PunishmentHistoryRecord(
					id,
					targetUuid,
					targetName,
					type,
					issuer,
					reason,
					createdAt,
					duration,
					extra
			));
		}

		records.sort(Comparator.comparingLong(PunishmentHistoryRecord::getCreatedAt).reversed());

		return records;
	}

	private void saveHistoryData() {
		try {
			historyData.save(historyFile);
		} catch (IOException exception) {
			plugin.getLogger().severe("Could not save history.yml");
			exception.printStackTrace();
		}
	}
}