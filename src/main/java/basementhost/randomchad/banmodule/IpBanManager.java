package basementhost.randomchad.banmodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.util.DurationUtil;
import basementhost.randomchad.util.IpUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class IpBanManager {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private File ipBansFile;
	private FileConfiguration ipBansData;

	public IpBanManager(JavaPlugin plugin, LangManager langManager) {
		this.plugin = plugin;
		this.langManager = langManager;
		loadIpBansData();
		startCleanupTask();
	}

	public void loadIpBansData() {
		File dataFolder = new File(plugin.getDataFolder(), "data");

		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		ipBansFile = new File(dataFolder, "ipbans.yml");

		if (!ipBansFile.exists()) {
			try {
				ipBansFile.createNewFile();
			} catch (IOException exception) {
				plugin.getLogger().severe("Could not create ipbans.yml");
				exception.printStackTrace();
			}
		}

		ipBansData = YamlConfiguration.loadConfiguration(ipBansFile);
	}

	public void reload() {
		loadIpBansData();
	}

	public IpBanRecord tempBanIp(String ip, String issuerName, String reason, long durationMillis) {
		long now = System.currentTimeMillis();
		long expiresAt = now + durationMillis;

		IpBanRecord record = new IpBanRecord(
				ip,
				issuerName,
				reason,
				now,
				expiresAt,
				false
		);

		saveIpBanRecord(record);
		return record;
	}

	public IpBanRecord permanentBanIp(String ip, String issuerName, String reason) {
		long now = System.currentTimeMillis();

		IpBanRecord record = new IpBanRecord(
				ip,
				issuerName,
				reason,
				now,
				-1L,
				true
		);

		saveIpBanRecord(record);
		return record;
	}

	private void saveIpBanRecord(IpBanRecord record) {
		String path = "ips." + sanitizeIp(record.getIp());

		ipBansData.set(path + ".ip", record.getIp());
		ipBansData.set(path + ".issuer", record.getIssuerName());
		ipBansData.set(path + ".reason", record.getReason());
		ipBansData.set(path + ".issued-at", record.getIssuedAt());
		ipBansData.set(path + ".expires-at", record.getExpiresAt());
		ipBansData.set(path + ".permanent", record.isPermanent());

		saveIpBansData();
	}

	public boolean unbanIp(String ip) {
		String path = "ips." + sanitizeIp(ip);

		if (!ipBansData.contains(path)) {
			return false;
		}

		ipBansData.set(path, null);
		saveIpBansData();
		return true;
	}

	public IpBanRecord getActiveIpBan(String ip) {
		String path = "ips." + sanitizeIp(ip);

		if (!ipBansData.contains(path)) {
			return null;
		}

		String storedIp = ipBansData.getString(path + ".ip", ip);
		String issuer = ipBansData.getString(path + ".issuer", "Console");
		String reason = ipBansData.getString(path + ".reason", "No reason provided");
		long issuedAt = ipBansData.getLong(path + ".issued-at");
		long expiresAt = ipBansData.getLong(path + ".expires-at");
		boolean permanent = ipBansData.getBoolean(path + ".permanent", false);

		IpBanRecord record = new IpBanRecord(
				storedIp,
				issuer,
				reason,
				issuedAt,
				expiresAt,
				permanent
		);

		long now = System.currentTimeMillis();

		if (record.isExpired(now)) {
			unbanIp(ip);
			return null;
		}

		return record;
	}

	public boolean isIpBanned(String ip) {
		return getActiveIpBan(ip) != null;
	}

	public String buildKickMessage(IpBanRecord record) {
		long remainingMillis = record.isPermanent()
				? -1L
				: record.getExpiresAt() - System.currentTimeMillis();

		return String.join("\n", langManager.getRawMessageList("ban.ip-kick-message", Map.of(
				"%ip%", record.getIp(),
				"%reason%", record.getReason(),
				"%issuer%", record.getIssuerName(),
				"%remaining%", DurationUtil.formatDuration(langManager, remainingMillis)
		)));
	}

	public void kickOnlinePlayersWithIp(String ip) {
		IpBanRecord record = getActiveIpBan(ip);

		if (record == null) {
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			String playerIp = IpUtil.getPlayerIp(player);

			if (playerIp == null) {
				continue;
			}

			if (playerIp.equals(ip)) {
				player.kickPlayer(buildKickMessage(record));
			}
		}
	}

	public void cleanupExpiredIpBans() {
		ConfigurationSection ipsSection = ipBansData.getConfigurationSection("ips");

		if (ipsSection == null) {
			return;
		}

		for (String sanitizedIp : ipsSection.getKeys(false)) {
			String ip = ipBansData.getString("ips." + sanitizedIp + ".ip");

			if (ip != null) {
				getActiveIpBan(ip);
			}
		}
	}

	private void startCleanupTask() {
		Bukkit.getScheduler().runTaskTimer(
				plugin,
				this::cleanupExpiredIpBans,
				20L * 60L,
				20L * 60L
		);
	}

	private void saveIpBansData() {
		try {
			ipBansData.save(ipBansFile);
		} catch (IOException exception) {
			plugin.getLogger().severe("Could not save ipbans.yml");
			exception.printStackTrace();
		}
	}

	private String sanitizeIp(String ip) {
		return ip.replace(".", "_");
	}
}