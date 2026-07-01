package basementhost.randomchad.warnmodule;

import java.util.UUID;

public class WarnRecord {

	private final String id;
	private final UUID targetUuid;
	private final String targetName;
	private final String issuerName;
	private final String reason;
	private final long issuedAt;
	private final long expiresAt;

	public WarnRecord(
			String id,
			UUID targetUuid,
			String targetName,
			String issuerName,
			String reason,
			long issuedAt,
			long expiresAt
	) {
		this.id = id;
		this.targetUuid = targetUuid;
		this.targetName = targetName;
		this.issuerName = issuerName;
		this.reason = reason;
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
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

	public String getIssuerName() {
		return issuerName;
	}

	public String getReason() {
		return reason;
	}

	public long getIssuedAt() {
		return issuedAt;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

	public boolean isExpired(long now) {
		return expiresAt > 0 && expiresAt <= now;
	}
}