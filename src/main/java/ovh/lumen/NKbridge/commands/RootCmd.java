package ovh.lumen.NKbridge.commands;

import ovh.lumen.NKbridge.commands.root.Group;
import ovh.lumen.NKbridge.commands.root.Reload;
import ovh.lumen.NKbridge.data.NKData;
import ovh.lumen.NKbridge.enums.Messages;
import ovh.lumen.NKbridge.enums.Permissions;
import ovh.lumen.NKbridge.enums.Usages;
import ovh.lumen.NKbridge.utils.MessageParser;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RootCmd implements CommandExecutor
{
	public RootCmd()
	{

	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, String[] args)
	{
		if(!hasRootPermissions(sender))
		{
			sender.sendMessage(Messages.PERMISSION_MISSING.toString());

			return true;
		}

		if(args.length > 0)
		{
			args[0] = args[0].toLowerCase();
			switch(args[0])
			{
				case "reload":
					return new Reload().execute(sender, args);
				case "group":
					return new Group().execute(sender, args);
				default:
					sender.sendMessage(Usages.ROOT_CMD.toString());
			}

			return true;
		}

		MessageParser messageParser = new MessageParser(Messages.ROOT_PLUGIN_INFO_MSG.toString());
		messageParser.addArg(NKData.PLUGIN_NAME);
		messageParser.addArg(NKData.PLUGIN_VERSION);
		messageParser.addArg(NKData.PLUGIN_AUTHOR);

		sender.sendMessage(messageParser.parse());

		return true;
	}

	private boolean hasRootPermissions(CommandSender sender)
	{
		return sender.hasPermission(Permissions.ROOT_CMD.toString()) ||
				sender.hasPermission(Permissions.ADMIN.toString());
	}
}
