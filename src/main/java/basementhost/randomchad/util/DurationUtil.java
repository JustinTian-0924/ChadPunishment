package basementhost.randomchad.util;

import basementhost.randomchad.lang.LangManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// transfer time to xxSeconds, xxMinutes, xxHours, xxDayss
public class DurationUtil {

	private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)(s|m|h|d)$", Pattern.CASE_INSENSITIVE);

	private DurationUtil() {
	}

	public static boolean isDurationText(String text) {
		if (text == null) {
			return false;
		}

		return DURATION_PATTERN.matcher(text).matches();
	}

	public static boolean isPermanentDuration(String text) {
		return "permanent".equalsIgnoreCase(text);
	}

	public static long parseDurationToMillis(String text) {
		if (text == null) {
			return -1;
		}

		Matcher matcher = DURATION_PATTERN.matcher(text);

		if (!matcher.matches()) {
			return -1;
		}

		long amount;

		try {
			amount = Long.parseLong(matcher.group(1));
		} catch (NumberFormatException exception) {
			return -1;
		}

		String unit = matcher.group(2).toLowerCase(Locale.ROOT);

		long multiplier = switch (unit) {
			case "s" -> 1000L;
			case "m" -> 60L * 1000L;
			case "h" -> 60L * 60L * 1000L;
			case "d" -> 24L * 60L * 60L * 1000L;
			default -> 0L;
		};

		try {
			long durationMillis = Math.multiplyExact(amount, multiplier);
			// Also leave enough room for the expiry timestamp to be added safely.
			return durationMillis > 0 && durationMillis <= Long.MAX_VALUE - System.currentTimeMillis()
					? durationMillis : -1L;
		} catch (ArithmeticException exception) {
			return -1L;
		}
	}

	// Console command substitutions still require plain text; display messages retain their formatting.
	public static String formatDuration(LangManager langManager, long millis) {
		return PlainTextComponentSerializer.plainText().serialize(formatDurationComponent(langManager, millis));
	}

	public static Component formatDurationComponent(LangManager langManager, long millis) {
		if (millis < 0) {
			return langManager.getRawComponent("time.permanent");
		}

		long totalSeconds = millis / 1000L;

		long days = totalSeconds / 86400L;
		long hours = (totalSeconds % 86400L) / 3600L;
		long minutes = (totalSeconds % 3600L) / 60L;
		long seconds = totalSeconds % 60L;

		if (days > 0) {
			return langManager.getRawComponent("time.days-hours", Map.of(
					"%days%", String.valueOf(days),
					"%hours%", String.valueOf(hours)
			));
		}

		if (hours > 0) {
			return langManager.getRawComponent("time.hours-minutes", Map.of(
					"%hours%", String.valueOf(hours),
					"%minutes%", String.valueOf(minutes)
			));
		}

		if (minutes > 0) {
			return langManager.getRawComponent("time.minutes-seconds", Map.of(
					"%minutes%", String.valueOf(minutes),
					"%seconds%", String.valueOf(seconds)
			));
		}

		return langManager.getRawComponent("time.seconds", Map.of(
				"%seconds%", String.valueOf(seconds)
		));
	}
}
