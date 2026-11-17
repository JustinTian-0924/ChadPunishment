package basementhost.randomchad.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parse the trusted language template first and insert player-supplied values as text.
public final class MessageFormatter {

	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
	private static final Pattern LEGACY_CODE = Pattern.compile(
			"(?i)(?:[&\u00a7]x(?:[&\u00a7][0-9a-f]){6}|[&\u00a7]#[0-9a-f]{6}|[&\u00a7][0-9a-fk-or])"
	);
	private static final String CLEAR_DECORATIONS = "<bold:false><italic:false><underlined:false>"
			+ "<strikethrough:false><obfuscated:false>";
	private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
	private static final Pattern PLACEHOLDER = Pattern.compile("%[a-zA-Z0-9_-]+%");

	private MessageFormatter() {
	}

	public static Component render(String template, Map<String, ?> values) {
		String bindingPrefix = uniquePrefix(template, "chad_value_");
		TagResolver.Builder resolvers = TagResolver.builder();
		String source = bind(template, values, bindingPrefix, resolvers);
		return MINI_MESSAGE.deserialize(convertLegacy(source), resolvers.build());
	}

	private static String bind(String template, Map<String, ?> values, String prefix, TagResolver.Builder resolvers) {
		StringBuilder result = new StringBuilder();
		int position = 0;
		while (position < template.length()) {
			int tagStart = template.indexOf('<', position);
			if (tagStart < 0) {
				result.append(bindText(template.substring(position), values, prefix, resolvers));
				break;
			}
			int tagEnd = findTagEnd(template, tagStart);
			if (tagEnd < 0) {
				result.append(bindText(template.substring(position), values, prefix, resolvers));
				break;
			}
			result.append(bindText(template.substring(position, tagStart), values, prefix, resolvers));
			String tag = template.substring(tagStart, tagEnd + 1);
			// Click arguments are parsed as strings, while hover text is parsed as a nested message.
			if (!isEscaped(template, tagStart) && tag.regionMatches(true, 0, "<click:", 0, 7)) {
				result.append(bindClickArgument(tag, values));
			} else {
				result.append(bindText(tag, values, prefix, resolvers));
			}
			position = tagEnd + 1;
		}
		return result.toString();
	}

	private static String bindText(String text, Map<String, ?> values, String prefix, TagResolver.Builder resolvers) {
		Matcher matcher = PLACEHOLDER.matcher(text);
		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			String key = matcher.group();
			if (!values.containsKey(key)) {
				continue;
			}
			String name = prefix + key.substring(1, key.length() - 1).toLowerCase(java.util.Locale.ROOT);
			Object value = values.get(key);
			if (value instanceof ComponentLike component) {
				resolvers.resolver(Placeholder.component(name, component.asComponent()));
			} else {
				resolvers.resolver(Placeholder.unparsed(name, value == null ? "" : value.toString()));
			}
			matcher.appendReplacement(result, Matcher.quoteReplacement("<" + name + ">"));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private static String bindClickArgument(String tag, Map<String, ?> values) {
		int separator = tag.indexOf(':', 7);
		if (separator < 0) {
			return tag;
		}
		String argument = tag.substring(separator + 1, tag.length() - 1);
		if (argument.length() >= 2 && (argument.charAt(0) == '\'' || argument.charAt(0) == '"')
				&& argument.charAt(0) == argument.charAt(argument.length() - 1)) {
			char quote = argument.charAt(0);
			StringBuilder unquoted = new StringBuilder();
			for (int index = 1; index < argument.length() - 1; index++) {
				char character = argument.charAt(index);
				if (character == '\\' && index + 1 < argument.length() - 1
						&& (argument.charAt(index + 1) == quote || argument.charAt(index + 1) == '\\')) {
					character = argument.charAt(++index);
				}
				unquoted.append(character);
			}
			argument = unquoted.toString();
		}
		Matcher matcher = PLACEHOLDER.matcher(argument);
		StringBuilder expanded = new StringBuilder();
		boolean changed = false;
		while (matcher.find()) {
			if (!values.containsKey(matcher.group())) {
				continue;
			}
			Object value = values.get(matcher.group());
			String literal = value instanceof ComponentLike component
					? PLAIN.serialize(component.asComponent()) : value == null ? "" : value.toString();
			matcher.appendReplacement(expanded, Matcher.quoteReplacement(literal));
			changed = true;
		}
		if (!changed) {
			return tag;
		}
		matcher.appendTail(expanded);
		String quoted = expanded.toString().replace("\\", "\\\\").replace("'", "\\'");
		return tag.substring(0, separator + 1) + "'" + quoted + "'>";
	}

	private static String convertLegacy(String source) {
		StringBuilder result = new StringBuilder();
		Matcher matcher = LEGACY_CODE.matcher(source);
		for (int position = 0; position < source.length();) {
			char character = source.charAt(position);
			if (character == '<') {
				int end = findTagEnd(source, position);
				if (end >= 0) {
					// Tag arguments including URLs and nested hover messages retain MiniMessage syntax.
					result.append(source, position, end + 1);
					position = end + 1;
					continue;
				}
			}
			matcher.region(position, source.length());
			if ((character == '&' || character == '§') && matcher.lookingAt()) {
				result.append(legacyTag(matcher.group()));
				position = matcher.end();
			} else {
				result.append(character);
				position++;
			}
		}
		return result.toString();
	}

	// support legacy ways of changing text color
	private static String legacyTag(String code) {
		char value = Character.toLowerCase(code.charAt(1));
		String color;
		if (value == '#' || value == 'x') {
			String hex = code.substring(2).replace("&", "").replace("§", "");
			color = "#" + hex;
		} else {
			color = switch (value) {
				case '0' -> "black";
				case '1' -> "dark_blue";
				case '2' -> "dark_green";
				case '3' -> "dark_aqua";
				case '4' -> "dark_red";
				case '5' -> "dark_purple";
				case '6' -> "gold";
				case '7' -> "gray";
				case '8' -> "dark_gray";
				case '9' -> "blue";
				case 'a' -> "green";
				case 'b' -> "aqua";
				case 'c' -> "red";
				case 'd' -> "light_purple";
				case 'e' -> "yellow";
				case 'f' -> "white";
				default -> null;
			};
		}
		if (color != null) {
			return "<color:" + color + ">" + CLEAR_DECORATIONS;
		}
		return switch (value) {
			case 'k' -> "<obfuscated>";
			case 'l' -> "<bold>";
			case 'm' -> "<strikethrough>";
			case 'n' -> "<underlined>";
			case 'o' -> "<italic>";
			case 'r' -> "<reset>";
			default -> code;
		};
	}

	private static int findTagEnd(String source, int start) {
		char quote = 0;
		for (int index = start + 1; index < source.length(); index++) {
			char character = source.charAt(index);
			if (character == '\\') {
				index++;
			} else if (quote != 0) {
				if (character == quote) {
					quote = 0;
				}
			} else if (character == '\'' || character == '"') {
				quote = character;
			} else if (character == '>') {
				return index;
			}
		}
		return -1;
	}

	private static boolean isEscaped(String source, int position) {
		int backslashes = 0;
		while (position > 0 && source.charAt(--position) == '\\') {
			backslashes++;
		}
		return backslashes % 2 != 0;
	}

	private static String uniquePrefix(String source, String prefix) {
		while (source.contains(prefix)) {
			prefix += "_";
		}
		return prefix;
	}
}
