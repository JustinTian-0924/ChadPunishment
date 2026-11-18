package basementhost.randomchad.history;

// A read-only display snapshot; historical entries do not imply an active restriction.
public record PunishmentQueryEntry(
		String id,
		PunishmentType type,
		String issuer,
		String reason,
		long createdAt,
		long durationMillis,
		long expiresAt,
		String extra,
		boolean moduleEnabled
) {
}
