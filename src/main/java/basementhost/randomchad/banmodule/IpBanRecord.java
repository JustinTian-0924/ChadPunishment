package basementhost.randomchad.banmodule;

public class IpBanRecord {

	private final String ip;
	private final String issuerName;
	private final String reason;
	private final long issuedAt;
	private final long expiresAt;
	private final boolean permanent;

	public IpBanRecord(
			String ip,
			String issuerName,
			String reason,
			long issuedAt,
			long expiresAt,
			boolean permanent
	) {
		this.ip = ip;
		this.issuerName = issuerName;
		this.reason = reason;
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
		this.permanent = permanent;
	}

	public String getIp() {
		return ip;
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