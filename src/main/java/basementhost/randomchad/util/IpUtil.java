package basementhost.randomchad.util;

import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

// Judge if a string is ipv (only ipv4)
public class IpUtil {

	private static final Pattern IPV4_PATTERN = Pattern.compile(
			"^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
					+ "(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$"
	);

	private IpUtil() {
	}

	public static boolean isIpAddress(String input) {
		if (input == null) {
			return false;
		}

		return IPV4_PATTERN.matcher(input).matches();
	}

	public static String getPlayerIp(Player player) {
		InetSocketAddress address = player.getAddress();

		if (address == null || address.getAddress() == null) {
			return null;
		}

		return address.getAddress().getHostAddress();
	}
}