package basementhost.randomchad.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

// Upgrade only known bundled defaults; keep custom messages and commands unchanged.
public final class CommandFormatMigrationManager {

	private static final String OLD_BAN_ACTION = "ban %player% You had violated the server rules for way too many times, hereby you are permabanned from this server.";
	private static final String NEW_BAN_ACTION = "ban %player% permanent You had violated the server rules for way too many times, hereby you are permabanned from this server.";

	private CommandFormatMigrationManager() {
	}

	public static void migrateConfig(JavaPlugin plugin) {
		String path = "warn.active-count-actions.10";
		List<String> actions = plugin.getConfig().getStringList(path);
		if (!actions.contains(OLD_BAN_ACTION)) {
			return;
		}

		File configFile = new File(plugin.getDataFolder(), "config.yml");
		try {
			backup(configFile);
			actions.replaceAll(action -> OLD_BAN_ACTION.equals(action) ? NEW_BAN_ACTION : action);
			plugin.getConfig().set(path, actions);
			plugin.getConfig().save(configFile);
			plugin.getLogger().info("Updated the default warning ban action to use an explicit permanent duration.");
		} catch (IOException exception) {
			plugin.getLogger().log(Level.WARNING, "Could not migrate the default warning ban action.", exception);
		}
	}

	public static void migrateLanguage(JavaPlugin plugin, File file, FileConfiguration messages, FileConfiguration defaults) {
		try (InputStream resource = plugin.getResource("lang/command-format-v1.yml")) {
			if (resource == null) {
				return;
			}

			YamlConfiguration previousDefaults = YamlConfiguration.loadConfiguration(
					new InputStreamReader(resource, StandardCharsets.UTF_8)
			);
			String language = file.getName().replaceFirst("\\.yml$", "");
			ConfigurationSection previous = previousDefaults.getConfigurationSection(language);
			if (previous == null) {
				return;
			}

			Map<String, Object> updates = new LinkedHashMap<>();
			for (String path : previous.getKeys(true)) {
				if (previous.isConfigurationSection(path)) {
					continue;
				}
				Object oldValue = previous.get(path);
				Object newValue = defaults.get(path);
				if (newValue != null && Objects.equals(messages.get(path), oldValue)
						&& !Objects.equals(oldValue, newValue)) {
					updates.put(path, newValue);
				}
			}

			if (updates.isEmpty()) {
				return;
			}

			backup(file);
			updates.forEach(messages::set);
			messages.save(file);
			plugin.getLogger().info("Updated " + updates.size() + " default messages in " + file.getName()
					+ ". Custom messages were preserved.");
		} catch (IOException exception) {
			plugin.getLogger().log(Level.WARNING, "Could not migrate command messages in " + file.getName(), exception);
		}
	}

	private static void backup(File file) throws IOException {
		File backup = new File(file.getParentFile(), file.getName() + ".before-command-format-"
				+ System.currentTimeMillis() + ".bak");
		Files.copy(file.toPath(), backup.toPath());
	}
}
