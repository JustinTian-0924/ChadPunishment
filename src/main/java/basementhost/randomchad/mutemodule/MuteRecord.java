package basementhost.randomchad.mutemodule;

import java.util.UUID;

public class MuteRecord {

	private final UUID targetUuid;
	private final String targetName;
	private final String issuerName;
	private final String reason;
	private final long issuedAt;
	private final long expiresAt;
	private final boolean permanent;

	public MuteRecord(
			UUID targetUuid,
			String targetName,
			String issuerName,
			String reason,
			long issuedAt,
			long expiresAt,
			boolean permanent
	) {
		this.targetUuid = targetUuid;
		this.targetName = targetName;
		this.issuerName = issuerName;
		this.reason = reason;
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
		this.permanent = permanent;
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

	public boolean isPermanent() {
		return permanent;
	}

	public boolean isExpired(long now) {
		return !permanent && expiresAt > 0 && expiresAt <= now;
	}
}