package basementhost.randomchad.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneId.systemDefault());

	private DateUtil() {
	}

	public static String formatMillis(long millis) {
		return FORMATTER.format(Instant.ofEpochMilli(millis));
	}
}