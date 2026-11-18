package basementhost.randomchad.history;

import basementhost.randomchad.command.PunishmentQueryCommand;
import basementhost.randomchad.lang.LangManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

// Keep the original history command and aliases on the same paginated query view.
public final class PunishHistoryCommand implements BasicCommand {

	private final LangManager langManager;
	private final PunishmentQueryCommand queryCommand;

	public PunishHistoryCommand(LangManager langManager, PunishmentQueryCommand queryCommand) {
		this.langManager = langManager;
		this.queryCommand = queryCommand;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		if (!source.getSender().hasPermission("chadpunishment.history")) {
			langManager.sendMessage(source.getSender(), "history.no-permission");
			return;
		}
		if (args.length < 1 || args.length > 2) {
			langManager.sendMessage(source.getSender(), "query.legacy-usage");
			return;
		}
		queryCommand.execute(source, args.length == 1
				? new String[]{"query", "all", args[0]} : new String[]{"query", "all", args[0], args[1]});
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		return args.length == 1 ? queryCommand.suggest(source, new String[]{"query", "all", args[0]}) : List.of();
	}
}
