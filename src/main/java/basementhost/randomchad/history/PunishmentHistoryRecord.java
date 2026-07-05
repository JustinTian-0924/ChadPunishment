package basementhost.randomchad.history;

import java.util.UUID;

public class PunishmentHistoryRecord {

	private final String id;
	private final UUID targetUuid;
	private final String targetName;
	private final PunishmentType type;
	private final String issuerName;
	private final String reason;
	private final long createdAt;
	private final long durationMillis;
	private final String extra;

	public PunishmentHistoryRecord(
			String id,
			UUID targetUuid,
			String targetName,
			PunishmentType type,
			String issuerName,
			String reason,
			long createdAt,
			long durationMillis,
			String extra
	) {
		this.id = id;
		this.targetUuid = targetUuid;
		this.targetName = targetName;
		this.type = type;
		this.issuerName = issuerName;
		this.reason = reason;
		this.createdAt = createdAt;
		this.durationMillis = durationMillis;
		this.extra = extra;
	}

	public String getId() {
		return id;
	}

	public UUID getTargetUuid() {
		return targetUuid;
	}

	public String getTargetName() {
		return targetName;
	}

	public PunishmentType getType() {
		return type;
	}

	public String getIssuerName() {
		return issuerName;
	}

	public String getReason() {
		return reason;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public long getDurationMillis() {
		return durationMillis;
	}

	public String getExtra() {
		return extra;
	}
}