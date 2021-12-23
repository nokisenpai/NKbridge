package ovh.lumen.NKbridge.registers;

import ovh.lumen.NKbridge.completers.RootCompleter;
import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.enums.InternalMessages;
import ovh.lumen.NKbridge.utils.MessageParser;
import ovh.lumen.NKbridge.utils.NKLogger;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class CompleterRegister
{
	private static final Map<String, TabCompleter> completers = setCompleters();

	public static void registerAllCompleters(JavaPlugin plugin)
	{
		completers.forEach((commandName, tabCompleter) ->
		{
			PluginCommand command = plugin.getCommand(commandName);
			if(command != null)
			{
				command.setTabCompleter(tabCompleter);
			}
			else
			{
				MessageParser messageParser = new MessageParser(InternalMessages.REGISTER_COMPLETER_FAIL.toString());
				messageParser.addArg(commandName);

				NKLogger.warn(messageParser.parse());
			}
		});
	}

	private static Map<String, TabCompleter> setCompleters()
	{
		Map<String, TabCompleter> completers = new HashMap<>();
		completers.put(NKData.PLUGIN_NAME, new RootCompleter());

		return completers;
	}
}
