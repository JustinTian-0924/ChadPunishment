package basementhost.randomchad.history;

import basementhost.randomchad.banmodule.BanManager;
import basementhost.randomchad.banmodule.BanRecord;
import basementhost.randomchad.banmodule.IpBanManager;
import basementhost.randomchad.banmodule.IpBanRecord;
import basementhost.randomchad.manager.ModuleManager;
import basementhost.randomchad.mutemodule.MuteManager;
import basementhost.randomchad.mutemodule.MuteRecord;
import basementhost.randomchad.warnmodule.WarnManager;
import basementhost.randomchad.warnmodule.WarnRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class PunishmentQueryManager {

	private final PunishmentHistoryManager historyManager;
	private final WarnManager warnManager;
	private final MuteManager muteManager;
	private final BanManager banManager;
	private final IpBanManager ipBanManager;
	private final ModuleManager moduleManager;

	public PunishmentQueryManager(PunishmentHistoryManager historyManager, WarnManager warnManager,
			MuteManager muteManager, BanManager banManager, IpBanManager ipBanManager, ModuleManager moduleManager) {
		this.historyManager = historyManager;
		this.warnManager = warnManager;
		this.muteManager = muteManager;
		this.banManager = banManager;
		this.ipBanManager = ipBanManager;
		this.moduleManager = moduleManager;
	}

	public Map<UUID, String> getRecordedPlayers() {
		return historyManager.getRecordedPlayers();
	}

	public List<PunishmentQueryEntry> getHistory(UUID uuid, String filter) {
		return historyManager.getRecords(uuid).stream()
				.filter(record -> matches(filter, record.getType()))
				.map(record -> new PunishmentQueryEntry(record.getId(), record.getType(), record.getIssuerName(),
						record.getReason(), record.getCreatedAt(), record.getDurationMillis(), 0L, record.getExtra(), true))
				.sorted(order())
				.toList();
	}

	public List<PunishmentQueryEntry> getActive(UUID uuid, String filter, String currentIp) {
		List<PunishmentQueryEntry> entries = new ArrayList<>();
		if (matches(filter, PunishmentType.WARN)) {
			for (WarnRecord record : warnManager.getActiveWarns(uuid)) {
				entries.add(new PunishmentQueryEntry(record.getId(), PunishmentType.WARN, record.getIssuerName(),
						record.getReason(), record.getIssuedAt(), record.getExpiresAt() - record.getIssuedAt(),
						record.getExpiresAt(), "", moduleManager.isWarnEnabled()));
			}
		}

		if (matches(filter, PunishmentType.MUTE)) {
			MuteRecord record = muteManager.getActiveMute(uuid);
			if (record != null) {
				entries.add(new PunishmentQueryEntry("mute:" + uuid,
						record.isPermanent() ? PunishmentType.PERMAMUTE : PunishmentType.MUTE, record.getIssuerName(),
						record.getReason(), record.getIssuedAt(),
						record.isPermanent() ? -1L : record.getExpiresAt() - record.getIssuedAt(),
						record.isPermanent() ? -1L : record.getExpiresAt(), "", moduleManager.isMuteEnabled()));
			}
		}

		if (filter.equals("all") || filter.equals("tempban")) {
			BanRecord record = banManager.getActiveBan(uuid);
			if (record != null && (filter.equals("all") || !record.isPermanent())) {
				entries.add(new PunishmentQueryEntry("ban:" + uuid,
						record.isPermanent() ? PunishmentType.BAN : PunishmentType.TEMPBAN, record.getIssuerName(),
						record.getReason(), record.getIssuedAt(),
						record.isPermanent() ? -1L : record.getExpiresAt() - record.getIssuedAt(),
						record.isPermanent() ? -1L : record.getExpiresAt(), "", moduleManager.isBanEnabled()));
			}
		}

		// An offline UUID does not establish the player's current IP address.
		if (filter.equals("all") && currentIp != null) {
			IpBanRecord record = ipBanManager.getActiveIpBan(currentIp);
			if (record != null) {
				entries.add(new PunishmentQueryEntry("ip:" + currentIp,
						record.isPermanent() ? PunishmentType.BANIP : PunishmentType.TEMPBANIP, record.getIssuerName(),
						record.getReason(), record.getIssuedAt(),
						record.isPermanent() ? -1L : record.getExpiresAt() - record.getIssuedAt(),
						record.isPermanent() ? -1L : record.getExpiresAt(), "IP: " + currentIp,
						moduleManager.isBanIpEnabled()));
			}
		}
		return entries.stream().sorted(order()).toList();
	}

	public static boolean matches(String filter, PunishmentType type) {
		return switch (filter.toLowerCase(Locale.ROOT)) {
			case "all" -> true;
			case "warn" -> type == PunishmentType.WARN;
			case "mute" -> type == PunishmentType.MUTE || type == PunishmentType.PERMAMUTE || type == PunishmentType.UNMUTE;
			case "tempban" -> type == PunishmentType.TEMPBAN;
			default -> false;
		};
	}

	private static Comparator<PunishmentQueryEntry> order() {
		return Comparator.comparingLong(PunishmentQueryEntry::createdAt).reversed()
				.thenComparing(PunishmentQueryEntry::id);
	}
}
