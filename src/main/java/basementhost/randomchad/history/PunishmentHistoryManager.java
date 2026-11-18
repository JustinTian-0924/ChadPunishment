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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class PunishmentHistoryManager {

	private final JavaPlugin plugin;
	private File historyFile;
	private FileConfiguration historyData;
	private volatile Map<UUID, String> recordedPlayers = Map.of();

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
		refreshRecordedPlayers();
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
		Map<UUID, String> players = new HashMap<>(recordedPlayers);
		players.put(targetUuid, targetName == null ? targetUuid.toString() : targetName);
		recordedPlayers = Map.copyOf(players);
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

	public Map<UUID, String> getRecordedPlayers() {
		return recordedPlayers;
	}

	private void refreshRecordedPlayers() {
		Map<UUID, String> players = new LinkedHashMap<>();
		ConfigurationSection section = historyData.getConfigurationSection("players");
		if (section == null) {
			recordedPlayers = Map.of();
			return;
		}
		for (String key : section.getKeys(false)) {
			try {
				UUID uuid = UUID.fromString(key);
				ConfigurationSection records = section.getConfigurationSection(key + ".records");
				if (records == null) {
					continue;
				}
				long newest = Long.MIN_VALUE;
				for (String id : records.getKeys(false)) {
					long created = records.getLong(id + ".created-at");
					if (created >= newest) {
						newest = created;
						players.put(uuid, records.getString(id + ".target-name", uuid.toString()));
					}
				}
			} catch (IllegalArgumentException ignored) {
				// A malformed legacy key must not prevent other players from being queried.
			}
		}
		recordedPlayers = Map.copyOf(players);
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
