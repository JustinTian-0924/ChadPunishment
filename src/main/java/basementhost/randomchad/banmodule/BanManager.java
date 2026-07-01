package basementhost.randomchad.banmodule;

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
import java.util.Map;
import java.util.UUID;

public class BanManager {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private File bansFile;
	private FileConfiguration bansData;

	public BanManager(JavaPlugin plugin, LangManager langManager) {
		this.plugin = plugin;
		this.langManager = langManager;
		loadBansData();
		startCleanupTask();
	}

	public void loadBansData() {
		File dataFolder = new File(plugin.getDataFolder(), "data");

		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		bansFile = new File(dataFolder, "bans.yml");

		if (!bansFile.exists()) {
			try {
				bansFile.createNewFile();
			} catch (IOException exception) {
				plugin.getLogger().severe("Could not create bans.yml");
				exception.printStackTrace();
			}
		}

		bansData = YamlConfiguration.loadConfiguration(bansFile);
	}

	public void reload() {
		loadBansData();
	}

	public BanRecord tempBan(
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

		BanRecord record = new BanRecord(
				target.getUniqueId(),
				targetName,
				issuer.getName(),
				reason,
				now,
				expiresAt,
				false
		);

		saveBanRecord(record);
		return record;
	}

	public BanRecord permanentBan(
			OfflinePlayer target,
			CommandSender issuer,
			String reason
	) {
		long now = System.currentTimeMillis();

		String targetName = target.getName() == null
				? target.getUniqueId().toString()
				: target.getName();

		BanRecord record = new BanRecord(
				target.getUniqueId(),
				targetName,
				issuer.getName(),
				reason,
				now,
				-1L,
				true
		);

		saveBanRecord(record);
		return record;
	}

	private void saveBanRecord(BanRecord record) {
		String path = "players." + record.getTargetUuid();

		bansData.set(path + ".target-name", record.getTargetName());
		bansData.set(path + ".issuer", record.getIssuerName());
		bansData.set(path + ".reason", record.getReason());
		bansData.set(path + ".issued-at", record.getIssuedAt());
		bansData.set(path + ".expires-at", record.getExpiresAt());
		bansData.set(path + ".permanent", record.isPermanent());

		saveBansData();
	}

	public boolean unban(UUID targetUuid) {
		String path = "players." + targetUuid;

		if (!bansData.contains(path)) {
			return false;
		}

		bansData.set(path, null);
		saveBansData();
		return true;
	}

	public BanRecord getActiveBan(UUID targetUuid) {
		String path = "players." + targetUuid;

		if (!bansData.contains(path)) {
			return null;
		}

		String targetName = bansData.getString(path + ".target-name", targetUuid.toString());
		String issuer = bansData.getString(path + ".issuer", "Console");
		String reason = bansData.getString(path + ".reason", "No reason provided");
		long issuedAt = bansData.getLong(path + ".issued-at");
		long expiresAt = bansData.getLong(path + ".expires-at");
		boolean permanent = bansData.getBoolean(path + ".permanent", false);

		BanRecord record = new BanRecord(
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
			unban(targetUuid);
			return null;
		}

		return record;
	}

	public boolean isBanned(UUID targetUuid) {
		return getActiveBan(targetUuid) != null;
	}

	public String buildKickMessage(BanRecord record) {
		long remainingMillis = record.isPermanent()
				? -1L
				: record.getExpiresAt() - System.currentTimeMillis();

		return String.join("\n", langManager.getRawMessageList("ban.kick-message", Map.of(
				"%reason%", record.getReason(),
				"%issuer%", record.getIssuerName(),
				"%remaining%", DurationUtil.formatDuration(langManager, remainingMillis)
		)));
	}

	public void kickIfOnline(OfflinePlayer target) {
		if (!target.isOnline() || target.getPlayer() == null) {
			return;
		}

		BanRecord record = getActiveBan(target.getUniqueId());

		if (record == null) {
			return;
		}

		target.getPlayer().kickPlayer(buildKickMessage(record));
	}

	public void cleanupExpiredBans() {
		ConfigurationSection playersSection = bansData.getConfigurationSection("players");

		if (playersSection == null) {
			return;
		}

		for (String uuidText : playersSection.getKeys(false)) {
			try {
				UUID uuid = UUID.fromString(uuidText);
				getActiveBan(uuid);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	private void startCleanupTask() {
		Bukkit.getScheduler().runTaskTimer(
				plugin,
				this::cleanupExpiredBans,
				20L * 60L,
				20L * 60L
		);
	}

	private void saveBansData() {
		try {
			bansData.save(bansFile);
		} catch (IOException exception) {
			plugin.getLogger().severe("Could not save bans.yml");
			exception.printStackTrace();
		}
	}
}