package basementhost.randomchad.lang;

import basementhost.randomchad.manager.CommandFormatMigrationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

// make l18n possible
public class LangManager {

	private static final LegacyComponentSerializer SECTION_COLORS = LegacyComponentSerializer.builder().character('\u00a7').hexColors().build();

	private final JavaPlugin plugin;
	private FileConfiguration lang;

	public LangManager(JavaPlugin plugin) {
		this.plugin = plugin;
		saveDefaultLangFiles();
		loadLang();
	}

	private void saveDefaultLangFiles() {
		plugin.saveResource("lang/zh_cn.yml", false);
		plugin.saveResource("lang/en_us.yml", false);
	}

	private void loadLang() {
		String language = plugin.getConfig().getString("language", "zh_cn");
		File langFile = new File(plugin.getDataFolder(), "lang/" + language + ".yml");

		if (!langFile.exists()) {
			plugin.getLogger().warning("Language file not found: " + language + ".yml, using zh_cn.yml");
			langFile = new File(plugin.getDataFolder(), "lang/zh_cn.yml");
		}

		this.lang = YamlConfiguration.loadConfiguration(langFile);
		loadDefaults(langFile);
	}

	private void loadDefaults(File langFile) {
		String path = "lang/" + langFile.getName();
		try (InputStream resource = plugin.getResource(path)) {
			if (resource == null) {
				return;
			}
			FileConfiguration defaults = YamlConfiguration.loadConfiguration(
					new InputStreamReader(resource, StandardCharsets.UTF_8)
			);
			lang.setDefaults(defaults);
			CommandFormatMigrationManager.migrateLanguage(plugin, langFile, lang, defaults);
		} catch (IOException exception) {
			plugin.getLogger().warning("Could not load default messages from " + path);
		}
	}

	public void reload() {
		loadLang();
	}

	public Component getComponent(String path) {
		return getComponent(path, Map.of());
	}

	public Component getComponent(String path, Map<String, ?> placeholders) {
		return MessageFormatter.render(lang.getString("prefix", "") + lang.getString(path, path), placeholders);
	}

	public Component getRawComponent(String path) {
		return getRawComponent(path, Map.of());
	}

	public Component getRawComponent(String path, Map<String, ?> placeholders) {
		return MessageFormatter.render(lang.getString(path, path), placeholders);
	}

	public List<Component> getRawComponentList(String path, Map<String, ?> placeholders) {
		return lang.getStringList(path).stream()
				.map(message -> MessageFormatter.render(message, placeholders))
				.toList();
	}

	public Component getRawMultilineComponent(String path, Map<String, ?> placeholders) {
		var result = Component.text();
		boolean first = true;
		for (Component line : getRawComponentList(path, placeholders)) {
			if (!first) {
				result.append(Component.newline());
			}
			result.append(line);
			first = false;
		}
		return result.build();
	}

	// Keep legacy string getters for callers that explicitly need text other than rich messages.
	public String getMessage(String path) {
		return SECTION_COLORS.serialize(getComponent(path));
	}

	public String getMessage(String path, Map<String, ?> placeholders) {
		return SECTION_COLORS.serialize(getComponent(path, placeholders));
	}

	public String getRawMessage(String path) {
		return SECTION_COLORS.serialize(getRawComponent(path));
	}

	public String getRawMessage(String path, Map<String, ?> placeholders) {
		return SECTION_COLORS.serialize(getRawComponent(path, placeholders));
	}

	public List<String> getRawMessageList(String path, Map<String, ?> placeholders) {
		return getRawComponentList(path, placeholders).stream().map(SECTION_COLORS::serialize).toList();
	}

	public void sendMessage(CommandSender sender, String path) {
		sender.sendMessage(getComponent(path));
	}

	public void sendMessage(CommandSender sender, String path, Map<String, ?> placeholders) {
		sender.sendMessage(getComponent(path, placeholders));
	}

	public void sendRawMessage(CommandSender sender, String path) {
		sender.sendMessage(getRawComponent(path));
	}

	public void sendRawMessage(CommandSender sender, String path, Map<String, ?> placeholders) {
		sender.sendMessage(getRawComponent(path, placeholders));
	}

	public void sendMessageList(CommandSender sender, String path) {
		sendMessageList(sender, path, Map.of());
	}

	public void sendMessageList(CommandSender sender, String path, Map<String, ?> placeholders) {
		for (Component message : getRawComponentList(path, placeholders)) {
			sender.sendMessage(message);
		}
	}

	public Component toComponent(String message) {
		return MessageFormatter.render(message, Map.of());
	}
}
