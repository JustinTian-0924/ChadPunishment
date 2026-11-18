package basementhost.randomchad.command;

import basementhost.randomchad.history.PunishmentQueryEntry;
import basementhost.randomchad.history.PunishmentQueryManager;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.util.DateUtil;
import basementhost.randomchad.util.DurationUtil;
import basementhost.randomchad.util.IpUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

public final class PunishmentQueryCommand implements BasicCommand {

	private static final int PAGE_SIZE = 5;
	private static final List<String> FILTERS = List.of("all", "warn", "mute", "tempban");
	private final LangManager langManager;
	private final PunishmentQueryManager queryManager;

	public PunishmentQueryCommand(LangManager langManager, PunishmentQueryManager queryManager) {
		this.langManager = langManager;
		this.queryManager = queryManager;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!sender.hasPermission("chadpunishment.history")) {
			langManager.sendMessage(sender, "history.no-permission");
			return;
		}
		if (args.length < 3 || args.length > 4) {
			langManager.sendMessage(sender, "query.usage");
			return;
		}
		String filter = args[1].toLowerCase(Locale.ROOT);
		if (!FILTERS.contains(filter)) {
			langManager.sendMessage(sender, "query.invalid-filter");
			return;
		}
		int page = 1;
		try {
			if (args.length == 4) {
				page = Integer.parseInt(args[3]);
			}
			if (page < 1) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException exception) {
			langManager.sendMessage(sender, "query.invalid-page");
			return;
		}

		Target target = resolveTarget(sender, args[2]);
		if (target == null) {
			return;
		}
		boolean active = args[0].equalsIgnoreCase("check");
		String ip = target.online() == null ? null : IpUtil.getPlayerIp(target.online());
		List<PunishmentQueryEntry> entries = active
				? queryManager.getActive(target.uuid(), filter, ip) : queryManager.getHistory(target.uuid(), filter);
		int pages = Math.max(1, (int) ((entries.size() + (long) PAGE_SIZE - 1) / PAGE_SIZE));
		if (page > pages) {
			langManager.sendMessage(sender, "query.page-out-of-range", Map.of("%pages%", String.valueOf(pages)));
			return;
		}

		langManager.sendRawMessage(sender, "query.header", Map.of(
				"%player%", target.name(), "%uuid%", target.uuid().toString(),
				"%view%", langManager.getRawComponent(active ? "query.views.active" : "query.views.history")));
		sender.sendMessage(button("query.views.history", !active, command(false, filter, target.uuid(), 1))
				.append(Component.space()).append(button("query.views.active", active, command(true, filter, target.uuid(), 1))));
		Component filters = Component.empty();
		for (String choice : FILTERS) {
			filters = filters.append(button("query.filters." + choice, choice.equals(filter), command(active, choice, target.uuid(), 1)))
					.append(Component.space());
		}
		sender.sendMessage(filters);

		if (entries.isEmpty()) {
			langManager.sendRawMessage(sender, active ? "query.empty-active" : "query.empty-history");
		} else {
			int start = (page - 1) * PAGE_SIZE;
			for (int index = start; index < Math.min(start + PAGE_SIZE, entries.size()); index++) {
				sender.sendMessage(row(entries.get(index), index + 1, active, target.uuid()));
			}
		}
		if (active && filter.equals("all") && ip == null) {
			langManager.sendRawMessage(sender, "query.ip-not-checked");
		}
		Component previous = langManager.getRawComponent(page > 1 ? "query.previous" : "query.previous-disabled");
		Component next = langManager.getRawComponent(page < pages ? "query.next" : "query.next-disabled");
		if (page > 1) {
			previous = previous.clickEvent(ClickEvent.runCommand(command(active, filter, target.uuid(), page - 1)));
		}
		if (page < pages) {
			next = next.clickEvent(ClickEvent.runCommand(command(active, filter, target.uuid(), page + 1)));
		}
		Component summary = langManager.getRawComponent("query.page", Map.of(
				"%page%", String.valueOf(page), "%pages%", String.valueOf(pages), "%total%", String.valueOf(entries.size())));
		sender.sendMessage(previous.append(Component.space()).append(summary).append(Component.space()).append(next));
		langManager.sendRawMessage(sender, "query.hover-tip");
	}

	private Component row(PunishmentQueryEntry entry, int number, boolean active, UUID uuid) {
		Map<String, Object> details = Map.ofEntries(
				Map.entry("%number%", String.valueOf(number)), Map.entry("%id%", entry.id()),
				Map.entry("%uuid%", uuid.toString()),
				Map.entry("%type%", langManager.getRawComponent("query.types." + entry.type().name().toLowerCase(Locale.ROOT))),
				Map.entry("%issuer%", entry.issuer()), Map.entry("%reason%", entry.reason()),
				Map.entry("%time%", DateUtil.formatMillis(entry.createdAt())),
				Map.entry("%duration%", duration(entry.durationMillis())),
				Map.entry("%remaining%", active ? DurationUtil.formatDurationComponent(langManager, entry.expiresAt() < 0 ? -1L
						: Math.max(0L, entry.expiresAt() - System.currentTimeMillis())) : Component.text("-")),
				Map.entry("%expires%", !active ? Component.text("-") : entry.expiresAt() < 0
						? langManager.getRawComponent("time.permanent") : Component.text(DateUtil.formatMillis(entry.expiresAt()))),
				Map.entry("%state%", langManager.getRawComponent(!active ? "query.states.history"
						: entry.moduleEnabled() ? "query.states.active" : "query.states.disabled")),
				Map.entry("%extra%", entry.extra() == null || entry.extra().isBlank() ? "-" : entry.extra())
		);
		Map<String, Object> line = new java.util.HashMap<>(details);
		line.put("%reason%", shorten(entry.reason(), 36));
		Component result = langManager.getRawComponent(active ? "query.active-line" : "query.history-line", line);
		if (active && !entry.moduleEnabled()) {
			result = result.append(langManager.getRawComponent("query.module-disabled"));
		}
		return result.hoverEvent(langManager.getRawMultilineComponent("query.details", details));
	}

	private Component duration(long millis) {
		return millis == 0L ? Component.text("-") : DurationUtil.formatDurationComponent(langManager, millis);
	}

	private Component button(String label, boolean selected, String command) {
		return langManager.getRawComponent(selected ? "query.button-selected" : "query.button",
				Map.of("%label%", langManager.getRawComponent(label)))
				.clickEvent(ClickEvent.runCommand(command));
	}

	private static String command(boolean active, String filter, UUID uuid, int page) {
		// UUID navigation keeps pagination attached to the same player even after a rename.
		return "/chadpunishment " + (active ? "check" : "query") + " " + filter + " " + uuid + " " + page;
	}

	private Target resolveTarget(CommandSender sender, String input) {
		Map<UUID, String> recorded = queryManager.getRecordedPlayers();
		UUID uuid = null;
		if (input.matches("(?i)[0-9a-f]{32}")) {
			input = input.replaceFirst("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
		}
		if (input.matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
			uuid = UUID.fromString(input);
		} else if (input.matches("[a-zA-Z0-9_]{1,16}")) {
			Player online = Bukkit.getPlayerExact(input);
			OfflinePlayer cached = online == null ? Bukkit.getOfflinePlayerIfCached(input) : online;
			if (cached != null) {
				uuid = cached.getUniqueId();
			} else {
				String name = input;
				List<UUID> matches = recorded.entrySet().stream()
						.filter(entry -> entry.getValue().equalsIgnoreCase(name)).map(Map.Entry::getKey).toList();
				if (matches.size() > 1) {
					langManager.sendMessage(sender, "query.ambiguous-player");
					return null;
				}
				if (!matches.isEmpty()) {
					uuid = matches.getFirst();
				}
			}
		} else {
			langManager.sendMessage(sender, "query.invalid-target");
			return null;
		}
		if (uuid == null) {
			langManager.sendMessage(sender, "history.player-not-found", Map.of("%player%", input));
			return null;
		}
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		String name = player.getName() == null ? recorded.getOrDefault(uuid, uuid.toString()) : player.getName();
		return new Target(uuid, name, player.getPlayer());
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		if (!source.getSender().hasPermission("chadpunishment.history")) {
			return List.of();
		}
		if (args.length == 2) {
			String prefix = args[1].toLowerCase(Locale.ROOT);
			return FILTERS.stream().filter(value -> value.startsWith(prefix)).toList();
		}
		if (args.length == 3) {
			TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			names.addAll(queryManager.getRecordedPlayers().values());
			Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
			String prefix = args[2].toLowerCase(Locale.ROOT);
			return names.stream().filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix)).limit(50).toList();
		}
		return List.of();
	}

	private static String shorten(String text, int length) {
		return text.codePointCount(0, text.length()) <= length ? text
				: text.substring(0, text.offsetByCodePoints(0, length)) + "...";
	}

	private record Target(UUID uuid, String name, Player online) {
	}
}
