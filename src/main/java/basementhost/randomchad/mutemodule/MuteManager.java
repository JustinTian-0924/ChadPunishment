package basementhost.randomchad.mutemodule;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MuteManager {

	private final JavaPlugin plugin;
	private File mutesFile;
	private FileConfiguration mutesData;

	public MuteManager(JavaPlugin plugin) {
		this.plugin = plugin;
		loadMutesData();
		startCleanupTask();
	}

	public void loadMutesData() {
		File dataFolder = new File(plugin.getDataFolder(), "data");

		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		mutesFile = new File(dataFolder, "mutes.yml");

		if (!mutesFile.exists()) {
			try {
				mutesFile.createNewFile();
			} catch (IOException exception) {
				plugin.getLogger().severe("Could not create mutes.yml");
				exception.printStackTrace();
			}
		}

		mutesData = YamlConfiguration.loadConfiguration(mutesFile);
	}

	public void reload() {
		loadMutesData();
	}

	public MuteRecord mute(
			OfflinePlayer target,
			CommandSender issuer,
			String reason,
			long durationMillis
	) {
		long now = System.currentTimeMillis();
		long expiresAt = now + durationMillis;

		String targetName = target.getName() == null
				? target.getUniqueId().toString()
				: target.getName();

		MuteRecord record = new MuteRecord(
				target.getUniqueId(),
				targetName,
				issuer.getName(),
				reason,
				now,
				expiresAt,
				false
		);

		saveMuteRecord(record);
		return record;
	}

	public MuteRecord permaMute(
			OfflinePlayer target,
			CommandSender issuer,
			String reason
	) {
		long now = System.currentTimeMillis();

		String targetName = target.getName() == null
				? target.getUniqueId().toString()
				: target.getName();

		MuteRecord record = new MuteRecord(
				target.getUniqueId(),
				targetName,
				issuer.getName(),
				reason,
				now,
				-1L,
				true
		);

		saveMuteRecord(record);
		return record;
	}

	private void saveMuteRecord(MuteRecord record) {
		String path = "players." + record.getTargetUuid();

		mutesData.set(path + ".target-name", record.getTargetName());
		mutesData.set(path + ".issuer", record.getIssuerName());
		mutesData.set(path + ".reason", record.getReason());
		mutesData.set(path + ".issued-at", record.getIssuedAt());
		mutesData.set(path + ".expires-at", record.getExpiresAt());
		mutesData.set(path + ".permanent", record.isPermanent());

		saveMutesData();
	}

	public boolean unmute(UUID targetUuid) {
		String path = "players." + targetUuid;

		if (!mutesData.contains(path)) {
			return false;
		}

		mutesData.set(path, null);
		saveMutesData();
		return true;
	}

	public MuteRecord getActiveMute(UUID targetUuid) {
		String path = "players." + targetUuid;

		if (!mutesData.contains(path)) {
			return null;
		}

		String targetName = mutesData.getString(path + ".target-name", targetUuid.toString());
		String issuer = mutesData.getString(path + ".issuer", "Console");
		String reason = mutesData.getString(path + ".reason", "No reason provided");
		long issuedAt = mutesData.getLong(path + ".issued-at");
		long expiresAt = mutesData.getLong(path + ".expires-at");
		boolean permanent = mutesData.getBoolean(path + ".permanent", false);

		MuteRecord record = new MuteRecord(
				targetUuid,
				targetName,
				issuer,
				reason,
				issuedAt,
				expiresAt,
				permanent
		);

		long now = System.currentTimeMillis();

		if (record.isExpired(now)) {
			unmute(targetUuid);
			return null;
		}

		return record;
	}

	public boolean isMuted(UUID targetUuid) {
		return getActiveMute(targetUuid) != null;
	}

	public void cleanupExpiredMutes() {
		ConfigurationSection playersSection = mutesData.getConfigurationSection("players");

		if (playersSection == null) {
			return;
		}

		for (String uuidText : playersSection.getKeys(false)) {
			try {
				UUID uuid = UUID.fromString(uuidText);
				getActiveMute(uuid);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	private void startCleanupTask() {
		Bukkit.getScheduler().runTaskTimer(
				plugin,
				this::cleanupExpiredMutes,
				20L * 60L,
				20L * 60L
		);
	}

	private void saveMutesData() {
		try {
			mutesData.save(mutesFile);
		} catch (IOException exception) {
			plugin.getLogger().severe("Could not save mutes.yml");
			exception.printStackTrace();
		}
	}
}