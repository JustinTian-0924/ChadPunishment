package basementhost.randomchad.command;

import basementhost.randomchad.lang.LangManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class HelpCommand {

	private final LangManager langManager;

	public HelpCommand(LangManager langManager) {
		this.langManager = langManager;
	}

	public void show(CommandSender sender, int page) {
		if (page < 1 || page > 2) {
			langManager.sendMessage(sender, "help.invalid-page");
			return;
		}
		langManager.sendRawMessage(sender, "help.header");
		Component commands = langManager.getRawComponent(page == 1 ? "help.commands-selected" : "help.commands-tab")
				.clickEvent(ClickEvent.runCommand("/chadpunishment help 1"));
		Component examples = langManager.getRawComponent(page == 2 ? "help.examples-selected" : "help.examples-tab")
				.clickEvent(ClickEvent.runCommand("/chadpunishment help 2"));
		sender.sendMessage(commands.append(Component.space()).append(examples));
		langManager.sendMessageList(sender, page == 1 ? "help.commands" : "help.examples");
		langManager.sendRawMessage(sender, "help.footer", Map.of("%page%", String.valueOf(page)));
	}
}
